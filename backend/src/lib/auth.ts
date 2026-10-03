import type { APIGatewayProxyEventV2, APIGatewayProxyResultV2 } from "aws-lambda";

export const json = (statusCode: number, body: unknown): APIGatewayProxyResultV2 => ({
  statusCode,
  headers: { "content-type": "application/json" },
  body: JSON.stringify(body),
});

export const requireApiKey = (event: APIGatewayProxyEventV2): APIGatewayProxyResultV2 | null => {
  const expected = process.env.STUDY_API_KEY;
  if (!expected) return json(500, { error: "server_misconfigured" });
  const got = event.headers?.["x-api-key"] ?? event.headers?.["X-Api-Key"];
  if (got !== expected) return json(401, { error: "unauthorized" });
  return null;
};

export const parseBody = <T>(event: APIGatewayProxyEventV2): T | null => {
  if (!event.body) return null;
  try {
    const raw = event.isBase64Encoded
      ? Buffer.from(event.body, "base64").toString("utf8")
      : event.body;
    return JSON.parse(raw) as T;
  } catch {
    return null;
  }
};
