import { DynamoDBClient, ExportTableToPointInTimeCommand } from "@aws-sdk/client-dynamodb";

const client = new DynamoDBClient({});

/** Daily: export the allocation table and the assignment log to S3 (outside the app DB). */
export const handler = async () => {
  const bucket = process.env.BACKUP_BUCKET!;
  const day = new Date().toISOString().slice(0, 10);
  for (const [name, arn] of [
    ["allocation", process.env.ALLOCATION_TABLE_ARN!],
    ["assignment-log", process.env.ASSIGNMENT_LOG_TABLE_ARN!],
  ]) {
    const res = await client.send(new ExportTableToPointInTimeCommand({
      TableArn: arn,
      S3Bucket: bucket,
      S3Prefix: `${name}/${day}`,
      ExportFormat: "DYNAMODB_JSON",
    }));
    console.log("export started", name, res.ExportDescription?.ExportArn);
  }
};
