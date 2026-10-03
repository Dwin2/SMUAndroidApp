// Reports the realized allocation from the assignment log (not the CSV):
//  - |T - C| at every enrollment prefix (ordered by slot), max observed
//  - burned slots (logged) and silent gaps (slot <= counter but never assigned or logged)
//
//   node scripts/audit_allocation.mjs [stage]      stage = prod (default) | test
import { DynamoDBClient, ScanCommand, GetItemCommand } from "@aws-sdk/client-dynamodb";

const stage = process.argv[2] ?? "prod";
const sfx = stage === "prod" ? "" : "-test";
const ddb = new DynamoDBClient({ region: process.env.AWS_REGION ?? "us-east-2" });

const scanAll = async (TableName) => {
  const items = [];
  let ExclusiveStartKey;
  do {
    const res = await ddb.send(new ScanCommand({ TableName, ExclusiveStartKey, ConsistentRead: true }));
    items.push(...res.Items);
    ExclusiveStartKey = res.LastEvaluatedKey;
  } while (ExclusiveStartKey);
  return items;
};

export async function audit() {
  const log = await scanAll(`smu-study-assignment-log${sfx}`);
  const assigns = log.filter((e) => e.event.S === "assign")
    .map((e) => ({ pid: e.prolific_id.S, slot: Number(e.slot_index.N), condition: e.condition.S }))
    .sort((a, b) => a.slot - b.slot);
  const burns = log.filter((e) => e.event.S === "burn").map((e) => Number(e.slot_index.N));

  const counter = await ddb.send(new GetItemCommand({
    TableName: `smu-study-data${sfx}`,
    Key: { pk: { S: "ALLOCATION" }, sk: { S: "COUNTER" } },
    ConsistentRead: true,
  }));
  const drawn = Number(counter.Item?.next_slot?.N ?? 0);

  let t = 0, c = 0, maxDiff = 0;
  const violations = [];
  assigns.forEach((a, i) => {
    a.condition === "treatment" ? t++ : c++;
    const d = Math.abs(t - c);
    maxDiff = Math.max(maxDiff, d);
    if (d > 2) violations.push({ n: i + 1, t, c });
  });
  const accounted = new Set([...assigns.map((a) => a.slot), ...burns]);
  const silentGaps = [];
  for (let s = 1; s <= Math.min(drawn, 700); s++) if (!accounted.has(s)) silentGaps.push(s);

  return {
    enrolled: assigns.length, treatment: t, control: c, maxAbsDiff: maxDiff,
    balanceViolations: violations, burnedSlots: burns.sort((a, b) => a - b),
    silentGaps, counter: drawn,
    distinctPids: new Set(assigns.map((a) => a.pid)).size,
    distinctSlots: new Set(assigns.map((a) => a.slot)).size,
  };
}

if (import.meta.url === `file://${process.argv[1]}`) {
  const r = await audit();
  console.log(JSON.stringify(r, null, 2));
  process.exit(r.balanceViolations.length || r.silentGaps.length ? 1 : 0);
}
