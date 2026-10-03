import type { APIGatewayProxyEventV2, APIGatewayProxyHandlerV2 } from "aws-lambda";
import { QueryCommand } from "@aws-sdk/lib-dynamodb";
import { ddb, TABLE, participantPk } from "../lib/ddb";
import { json, requireApiKey } from "../lib/auth";

export const handler: APIGatewayProxyHandlerV2 = async (event: APIGatewayProxyEventV2) => {
  const authErr = requireApiKey(event);
  if (authErr) return authErr;

  const prolificId = event.pathParameters?.prolificId;
  if (!prolificId || !/^[A-Za-z0-9]{24}$/.test(prolificId)) {
    return json(400, { error: "invalid_prolific_id" });
  }

  const items: Record<string, unknown>[] = [];
  let exclusiveStartKey: Record<string, unknown> | undefined;

  do {
    const res: any = await ddb.send(new QueryCommand({
      TableName: TABLE,
      KeyConditionExpression: "pk = :pk",
      ExpressionAttributeValues: { ":pk": participantPk(prolificId) },
      ExclusiveStartKey: exclusiveStartKey,
    }));
    if (res.Items) items.push(...res.Items);
    exclusiveStartKey = res.LastEvaluatedKey;
  } while (exclusiveStartKey);

  const meta = items.find((i) => i.sk === "META") ?? null;
  const surveys = items.filter((i) => typeof i.sk === "string" && (i.sk as string).startsWith("SURVEY#"));
  const sessions = items.filter((i) => typeof i.sk === "string" && (i.sk as string).startsWith("SESSION#"));

  return json(200, {
    prolificId,
    counts: { surveys: surveys.length, sessions: sessions.length },
    meta,
    surveys,
    sessions,
  });
};
