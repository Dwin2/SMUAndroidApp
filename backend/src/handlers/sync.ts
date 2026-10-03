import type { APIGatewayProxyEventV2, APIGatewayProxyHandlerV2 } from "aws-lambda";
import { BatchWriteCommand } from "@aws-sdk/lib-dynamodb";
import { ddb, TABLE, participantPk } from "../lib/ddb";
import { json, parseBody, requireApiKey } from "../lib/auth";
import { SyncRequest, SurveyResponseDto, AppSessionDto } from "../lib/types";

const isoSk = (prefix: string, ts: number, idPart: string | number) =>
  `${prefix}#${new Date(ts).toISOString()}#${idPart}`;

const surveyItem = (pk: string, s: SurveyResponseDto) => ({
  pk,
  sk: isoSk("SURVEY", s.timestamp, s.localId),
  type: "SURVEY",
  ...s,
  ingestedAt: Date.now(),
});

const sessionItem = (pk: string, s: AppSessionDto) => ({
  pk,
  sk: isoSk("SESSION", s.openTime, s.sessionId),
  type: "SESSION",
  ...s,
  ingestedAt: Date.now(),
});

const chunk = <T>(arr: T[], size: number): T[][] => {
  const out: T[][] = [];
  for (let i = 0; i < arr.length; i += size) out.push(arr.slice(i, i + size));
  return out;
};

export const handler: APIGatewayProxyHandlerV2 = async (event: APIGatewayProxyEventV2) => {
  const authErr = requireApiKey(event);
  if (authErr) return authErr;

  const raw = parseBody<unknown>(event);
  if (!raw) return json(400, { error: "invalid_json" });

  const parsed = SyncRequest.safeParse(raw);
  if (!parsed.success) {
    return json(400, { error: "validation_failed", issues: parsed.error.issues });
  }

  const { prolificId, surveys, sessions } = parsed.data;
  const pk = participantPk(prolificId);

  const items = [
    ...surveys.map((s) => surveyItem(pk, s)),
    ...sessions.map((s) => sessionItem(pk, s)),
  ];

  if (items.length === 0) return json(200, { ok: true, written: 0 });

  let written = 0;
  for (const batch of chunk(items, 25)) {
    let unprocessed = {
      [TABLE]: batch.map((Item) => ({ PutRequest: { Item } })),
    } as Record<string, { PutRequest: { Item: unknown } }[]>;

    for (let attempt = 0; attempt < 5 && unprocessed[TABLE]?.length; attempt++) {
      const res: any = await ddb.send(
        new BatchWriteCommand({ RequestItems: unprocessed as any })
      );
      const remaining = res.UnprocessedItems?.[TABLE] ?? [];
      written += unprocessed[TABLE].length - remaining.length;
      if (!remaining.length) break;
      unprocessed = { [TABLE]: remaining };
      await new Promise((r) => setTimeout(r, 50 * 2 ** attempt));
    }
  }

  return json(200, {
    ok: true,
    written,
    surveys: surveys.length,
    sessions: sessions.length,
  });
};
