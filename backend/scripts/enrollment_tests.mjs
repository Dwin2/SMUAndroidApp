// The five enrollment tests from the randomization handoff. Run ONLY against the test stack
// (Stage=test, "-test" tables loaded with the same seed) — these claim real slots.
//
//   API_URL=https://<test-api>.execute-api.us-east-2.amazonaws.com STUDY_API_KEY=<test key> \
//     node scripts/enrollment_tests.mjs
//
// Tests 1 and 2 fire requests genuinely in parallel (Promise.all), not in a loop.
import { randomBytes } from "node:crypto";
import { DynamoDBClient, GetItemCommand } from "@aws-sdk/client-dynamodb";
import { audit } from "./audit_allocation.mjs";

const { API_URL, STUDY_API_KEY } = process.env;
if (!API_URL || !STUDY_API_KEY) {
  console.error("set API_URL and STUDY_API_KEY for the TEST stack");
  process.exit(2);
}
const ddb = new DynamoDBClient({ region: process.env.AWS_REGION ?? "us-east-2" });

const pid = () => randomBytes(12).toString("hex");
const register = async (prolificId) => {
  const res = await fetch(`${API_URL}/v1/participants`, {
    method: "POST",
    headers: { "content-type": "application/json", "x-api-key": STUDY_API_KEY },
    body: JSON.stringify({
      prolificId, enrollmentDate: Date.now(),
      samplingWindowStartMin: 540, samplingWindowEndMin: 1320, selectedApps: [],
    }),
  });
  return { status: res.status, body: await res.json().catch(() => null) };
};
const counter = async () => {
  const r = await ddb.send(new GetItemCommand({
    TableName: "smu-study-data-test",
    Key: { pk: { S: "ALLOCATION" }, sk: { S: "COUNTER" } },
    ConsistentRead: true,
  }));
  return Number(r.Item?.next_slot?.N ?? 0);
};

const results = [];
const report = (n, name, pass, detail) => {
  results.push(pass);
  console.log(`${pass ? "PASS" : "FAIL"}  Test ${n}: ${name}\n      ${JSON.stringify(detail)}`);
};

// Test 1 — 50 simultaneous requests, distinct PIDs
{
  const pids = Array.from({ length: 50 }, pid);
  const before = await audit();
  const res = await Promise.all(pids.map(register));
  const after = await audit();
  const ok200 = res.filter((r) => r.status === 200).length;
  const newAssigns = after.enrolled - before.enrolled;
  report(1, "50 concurrent distinct PIDs", ok200 === 50 && newAssigns === 50 &&
    after.distinctSlots === after.enrolled && after.distinctPids === after.enrolled,
    { ok200, newAssigns, distinctSlots: after.distinctSlots, distinctPids: after.distinctPids });
  globalThis.reinstallPid = pids[0];
  globalThis.reinstallGroup = res[0].body?.studyGroup;
}

// Test 2 — 50 simultaneous requests, same PID
{
  const p = pid();
  const before = await audit();
  const res = await Promise.all(Array.from({ length: 50 }, () => register(p)));
  const after = await audit();
  const bodies = new Set(res.map((r) => `${r.status}:${JSON.stringify(r.body)}`));
  const newAssigns = after.enrolled - before.enrolled;
  const newBurns = after.burnedSlots.length - before.burnedSlots.length;
  report(2, "50 concurrent same PID", bodies.size === 1 && res[0].status === 200 && newAssigns === 1,
    { distinctResponses: bodies.size, sample: [...bodies][0], newAssigns, newBurns });
}

// Test 3 — reinstall: same PID after local wipe (the app just re-registers the same ID)
{
  const r = await register(globalThis.reinstallPid);
  report(3, "reinstall returns same condition", r.status === 200 && r.body?.studyGroup === globalThis.reinstallGroup,
    { first: globalThis.reinstallGroup, again: r.body?.studyGroup });
}

// Test 4 — malformed PIDs rejected, no slot consumed
{
  const before = await counter();
  const bad = ["abc", "", "a".repeat(23), "a".repeat(25), "g".repeat(24)];
  const res = await Promise.all(bad.map(register));
  const after = await counter();
  report(4, "malformed PIDs rejected, no slot consumed",
    res.every((r) => r.status === 400) && after === before,
    { statuses: res.map((r) => r.status), counterBefore: before, counterAfter: after });
}

// Test 5 — balance on the assignment log
{
  const a = await audit();
  report(5, "|T - C| <= 2 at every N (assignment log)", a.balanceViolations.length === 0,
    { enrolled: a.enrolled, T: a.treatment, C: a.control, maxAbsDiff: a.maxAbsDiff,
      burnedSlots: a.burnedSlots, silentGaps: a.silentGaps });
}

process.exit(results.every(Boolean) ? 0 : 1);
