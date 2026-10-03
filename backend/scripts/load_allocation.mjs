// Loads randomization/allocation_dynamodb_seed.jsonl into the allocation table, then verifies it.
//
//   node scripts/load_allocation.mjs <path/to/allocation_dynamodb_seed.jsonl> [table]
//
// table defaults to smu-study-allocation. Run `python generate_allocation.py --verify` first
// and only continue on MATCH. Refuses to run against a non-empty table so it can never
// overwrite claimed slots. Never regenerate the list to "fix" anything.
import { readFileSync } from "node:fs";
import { DynamoDBClient, BatchWriteItemCommand, ScanCommand } from "@aws-sdk/client-dynamodb";

const [, , seedPath, table = "smu-study-allocation"] = process.argv;
if (!seedPath) {
  console.error("usage: node scripts/load_allocation.mjs <allocation_dynamodb_seed.jsonl> [table]");
  process.exit(2);
}
const ddb = new DynamoDBClient({ region: process.env.AWS_REGION ?? "us-east-2" });

const scanAll = async () => {
  const items = [];
  let ExclusiveStartKey;
  do {
    const res = await ddb.send(new ScanCommand({ TableName: table, ExclusiveStartKey, ConsistentRead: true }));
    items.push(...res.Items);
    ExclusiveStartKey = res.LastEvaluatedKey;
  } while (ExclusiveStartKey);
  return items;
};

const rows = readFileSync(seedPath, "utf8").split("\n").filter((l) => l.trim()).map((l) => JSON.parse(l));
if (rows.length !== 700) throw new Error(`seed has ${rows.length} rows, expected 700`);

const before = await ddb.send(new ScanCommand({ TableName: table, Limit: 1, ConsistentRead: true }));
if (before.Count > 0) {
  console.error(`${table} is not empty — refusing to load (would overwrite claimed slots).`);
  process.exit(1);
}

for (let i = 0; i < rows.length; i += 25) {
  let pending = rows.slice(i, i + 25).map((Item) => ({ PutRequest: { Item } }));
  for (let attempt = 0; pending.length; attempt++) {
    if (attempt > 8) throw new Error(`batch at row ${i} kept returning unprocessed items`);
    const res = await ddb.send(new BatchWriteItemCommand({ RequestItems: { [table]: pending } }));
    pending = res.UnprocessedItems?.[table] ?? [];
    if (pending.length) await new Promise((r) => setTimeout(r, 200 * 2 ** attempt));
  }
  process.stdout.write(`\rloaded ${Math.min(i + 25, rows.length)}/700`);
}
console.log();

// Verification required by the handoff before going further.
const items = await scanAll();
const cond = (it) => it.condition.S;
const bySlot = new Map(items.map((it) => [Number(it.slot_index.N), cond(it)]));
const checks = {
  "item count is exactly 700": items.length === 700,
  "350 treatment": items.filter((it) => cond(it) === "treatment").length === 350,
  "350 control": items.filter((it) => cond(it) === "control").length === 350,
  "every claimed_by_pid is null": items.every((it) => it.claimed_by_pid?.NULL === true),
};
const expectedFirst24 = "CTTC CTTC CTCT CTCT TCTC TCCT".replace(/ /g, "");
const first24 = Array.from({ length: 24 }, (_, i) => (bySlot.get(i + 1) === "treatment" ? "T" : "C")).join("");
checks["slots 1-24 match the handoff block table"] = first24 === expectedFirst24;

let ok = true;
for (const [name, pass] of Object.entries(checks)) {
  console.log(`${pass ? "PASS" : "FAIL"}  ${name}`);
  ok &&= pass;
}
console.log(`slots 1-24: ${first24.match(/.{4}/g).join(" ")}`);
process.exit(ok ? 0 : 1);
