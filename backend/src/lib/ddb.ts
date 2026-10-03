import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import { DynamoDBDocumentClient } from "@aws-sdk/lib-dynamodb";

const client = new DynamoDBClient({});
export const ddb = DynamoDBDocumentClient.from(client, {
  marshallOptions: { removeUndefinedValues: true },
});

export const TABLE = process.env.TABLE_NAME!;
export const ALLOCATION_TABLE = process.env.ALLOCATION_TABLE_NAME!;
export const ASSIGNMENT_LOG_TABLE = process.env.ASSIGNMENT_LOG_TABLE_NAME!;

export const participantPk = (prolificId: string) => `PARTICIPANT#${prolificId}`;
