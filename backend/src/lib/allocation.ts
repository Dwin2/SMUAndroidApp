import {
  DeleteCommand,
  GetCommand,
  PutCommand,
  TransactWriteCommand,
  UpdateCommand,
} from "@aws-sdk/lib-dynamodb";
import { TransactionCanceledException } from "@aws-sdk/client-dynamodb";
import { ddb, TABLE, ALLOCATION_TABLE, ASSIGNMENT_LOG_TABLE, participantPk } from "./ddb";

/**
 * Assigns study conditions from the pre-generated allocation list (smu-study-allocation,
 * 700 slots, permuted blocks of 4). See RANDOMIZATION_SPEC.md / the randomization handoff.
 *
 *   1. PID already holds a slot  -> return that same condition (idempotent; reinstall-safe).
 *   2. otherwise claim the next slot atomically, bind it to the PID, return it.
 *
 * Atomicity:
 *  - A per-PID lock item (sk = ALLOC_LOCK) is taken first, so a burst of concurrent requests
 *    for the SAME PID draws only one counter number instead of burning a slot per request.
 *  - The slot number comes from an atomic counter (ADD next_slot), so concurrent requests for
 *    DIFFERENT PIDs each get a distinct slot.
 *  - The META write, the slot claim and the assignment-log entry go in one TransactWriteItems,
 *    each guarded by a condition. Never a read followed by a separate write.
 *
 * Any counter number that is drawn but not bound to a PID is a burned slot. Every burned slot
 * we can see is written to the assignment log (event = "burn"); a crash between the counter
 * increment and the transaction can't be logged here, so scripts/audit_allocation.mjs also
 * reports gaps (slots <= next_slot with no claim).
 */

export const MECHANISM_VERSION = "permuted-block-4/v1";
export const SLOT_COUNT = 700;

const COUNTER_KEY = { pk: "ALLOCATION", sk: "COUNTER" };
const LOCK_SK = "ALLOC_LOCK";
const LOCK_STALE_MS = 30_000;
const WAIT_FOR_PEER_MS = 6_000;
const MAX_CLAIM_ATTEMPTS = 5;

export type StudyGroup = "T" | "C";

export class AllocationExhaustedError extends Error {}
export class EnrollmentInProgressError extends Error {}

const toGroup = (condition: string): StudyGroup => {
  if (condition === "treatment") return "T";
  if (condition === "control") return "C";
  throw new Error(`unknown condition in allocation table: ${condition}`);
};

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

/** Mutable participant fields the app re-sends on every sync. Never includes the condition. */
export type ParticipantFields = Record<string, unknown>;

/**
 * Returns the participant's condition, claiming a slot on first enrollment. `fields` are the
 * app-reported participant fields to store on META.
 */
export async function assignCondition(
  prolificId: string,
  fields: ParticipantFields
): Promise<StudyGroup> {
  const existing = await refreshEnrolled(prolificId, fields);
  if (existing) return existing;

  if (!(await acquireLock(prolificId))) {
    // Another request for this PID is enrolling right now; return what it assigns.
    return waitForPeer(prolificId);
  }
  try {
    return await claimSlot(prolificId, fields);
  } finally {
    await ddb
      .send(new DeleteCommand({ TableName: TABLE, Key: { pk: participantPk(prolificId), sk: LOCK_SK } }))
      .catch((e) => console.warn("lock release failed", prolificId, e));
  }
}

/** Fast path: already enrolled → update the app-reported fields, return the stored condition. */
async function refreshEnrolled(prolificId: string, fields: ParticipantFields): Promise<StudyGroup | null> {
  const names: Record<string, string> = {};
  const values: Record<string, unknown> = { ":now": Date.now() };
  const sets = ["updatedAt = :now"];
  Object.entries(fields).forEach(([k, v], i) => {
    names[`#f${i}`] = k;
    values[`:f${i}`] = v;
    sets.push(`#f${i} = :f${i}`);
  });
  try {
    const res = await ddb.send(new UpdateCommand({
      TableName: TABLE,
      Key: { pk: participantPk(prolificId), sk: "META" },
      UpdateExpression: `SET ${sets.join(", ")}`,
      ConditionExpression: "attribute_exists(allocationSlot)",
      ExpressionAttributeNames: Object.keys(names).length ? names : undefined,
      ExpressionAttributeValues: values,
      ReturnValues: "ALL_NEW",
    }));
    return res.Attributes?.studyGroup as StudyGroup;
  } catch (e: any) {
    if (e?.name === "ConditionalCheckFailedException") return null;
    throw e;
  }
}

async function readEnrolled(prolificId: string): Promise<StudyGroup | null> {
  const res = await ddb.send(new GetCommand({
    TableName: TABLE,
    Key: { pk: participantPk(prolificId), sk: "META" },
    ConsistentRead: true,
  }));
  return res.Item?.allocationSlot ? (res.Item.studyGroup as StudyGroup) : null;
}

async function acquireLock(prolificId: string): Promise<boolean> {
  const now = Date.now();
  try {
    await ddb.send(new PutCommand({
      TableName: TABLE,
      Item: { pk: participantPk(prolificId), sk: LOCK_SK, lockedAt: now },
      ConditionExpression: "attribute_not_exists(pk) OR lockedAt < :stale",
      ExpressionAttributeValues: { ":stale": now - LOCK_STALE_MS },
    }));
    return true;
  } catch (e: any) {
    if (e?.name === "ConditionalCheckFailedException") return false;
    throw e;
  }
}

async function waitForPeer(prolificId: string): Promise<StudyGroup> {
  const deadline = Date.now() + WAIT_FOR_PEER_MS;
  while (Date.now() < deadline) {
    const group = await readEnrolled(prolificId);
    if (group) return group;
    await sleep(150);
  }
  throw new EnrollmentInProgressError(prolificId);
}

async function nextSlotNumber(): Promise<number> {
  const res = await ddb.send(new UpdateCommand({
    TableName: TABLE,
    Key: COUNTER_KEY,
    UpdateExpression: "ADD next_slot :one",
    ExpressionAttributeValues: { ":one": 1 },
    ReturnValues: "UPDATED_NEW",
  }));
  return Number(res.Attributes!.next_slot);
}

async function logBurn(slot: number, prolificId: string, reason: string) {
  console.warn("burned allocation slot", { slot, prolificId, reason });
  await ddb.send(new PutCommand({
    TableName: ASSIGNMENT_LOG_TABLE,
    Item: {
      event_id: `BURN#${slot}`,
      event: "burn",
      slot_index: slot,
      attempted_by_pid: prolificId,
      reason,
      timestamp: new Date().toISOString(),
      mechanism_version: MECHANISM_VERSION,
    },
    ConditionExpression: "attribute_not_exists(event_id)",
  })).catch((e) => console.error("failed to log burned slot", slot, e));
}

async function claimSlot(prolificId: string, fields: ParticipantFields): Promise<StudyGroup> {
  // The lock may have been taken over from a stale holder that finished after all.
  const already = await readEnrolled(prolificId);
  if (already) return already;

  for (let attempt = 0; attempt < MAX_CLAIM_ATTEMPTS; attempt++) {
    const slot = await nextSlotNumber();
    if (slot > SLOT_COUNT) throw new AllocationExhaustedError(`slot ${slot} > ${SLOT_COUNT}`);

    // The condition column is fixed by the committed list, so reading it before the claim is
    // safe; the claim itself is still guarded by the transaction's conditions.
    const slotRow = await ddb.send(new GetCommand({
      TableName: ALLOCATION_TABLE,
      Key: { slot_index: slot },
      ConsistentRead: true,
    }));
    if (!slotRow.Item) throw new AllocationExhaustedError(`slot ${slot} missing from allocation table`);
    if (slotRow.Item.claimed_by_pid) {
      await logBurn(slot, prolificId, "already_claimed");
      continue;
    }

    const condition = String(slotRow.Item.condition);
    const group = toGroup(condition);
    const now = Date.now();
    const iso = new Date(now).toISOString();

    const names: Record<string, string> = {};
    const values: Record<string, unknown> = {
      ":group": group,
      ":slot": slot,
      ":iso": iso,
      ":now": now,
      ":mv": MECHANISM_VERSION,
      ":pid": prolificId,
    };
    const sets = [
      "prolificId = :pid",
      "studyGroup = :group",
      "allocationSlot = :slot",
      "allocatedAt = :iso",
      "mechanismVersion = :mv",
      "registeredAt = if_not_exists(registeredAt, :now)",
      "updatedAt = :now",
    ];
    Object.entries(fields).forEach(([k, v], i) => {
      names[`#f${i}`] = k;
      values[`:f${i}`] = v;
      sets.push(`#f${i} = :f${i}`);
    });

    try {
      await ddb.send(new TransactWriteCommand({
        TransactItems: [
          {
            Update: {
              TableName: TABLE,
              Key: { pk: participantPk(prolificId), sk: "META" },
              UpdateExpression: `SET ${sets.join(", ")}`,
              ConditionExpression: "attribute_not_exists(allocationSlot)",
              ExpressionAttributeNames: Object.keys(names).length ? names : undefined,
              ExpressionAttributeValues: values,
            },
          },
          {
            Update: {
              TableName: ALLOCATION_TABLE,
              Key: { slot_index: slot },
              UpdateExpression: "SET claimed_by_pid = :pid, claimed_at = :iso",
              // The seed stores unclaimed slots as claimed_by_pid = NULL (the attribute exists),
              // so "unclaimed" means missing OR of type NULL.
              ConditionExpression:
                "attribute_exists(slot_index) AND (attribute_not_exists(claimed_by_pid) OR attribute_type(claimed_by_pid, :nullType))",
              ExpressionAttributeValues: { ":pid": prolificId, ":iso": iso, ":nullType": "NULL" },
            },
          },
          {
            Put: {
              TableName: ASSIGNMENT_LOG_TABLE,
              Item: {
                event_id: `ASSIGN#${prolificId}`,
                event: "assign",
                prolific_id: prolificId,
                condition,
                slot_index: slot,
                timestamp: iso,
                mechanism_version: MECHANISM_VERSION,
              },
              ConditionExpression: "attribute_not_exists(event_id)",
            },
          },
        ],
      }));
      return group;
    } catch (e) {
      if (!(e instanceof TransactionCanceledException)) {
        await logBurn(slot, prolificId, `error:${(e as Error).name}`);
        throw e;
      }
      const [metaReason, slotReason, logReason] = (e.CancellationReasons ?? []).map((r) => r.Code);
      if (metaReason === "ConditionalCheckFailed" || logReason === "ConditionalCheckFailed") {
        // A concurrent request already enrolled this PID; this slot number is burned.
        await logBurn(slot, prolificId, "pid_already_enrolled");
        const existing = await readEnrolled(prolificId);
        if (existing) return existing;
        throw e;
      }
      if (slotReason === "ConditionalCheckFailed") {
        await logBurn(slot, prolificId, "slot_already_claimed");
        continue;
      }
      await logBurn(slot, prolificId, `txn_cancelled:${[metaReason, slotReason, logReason].join(",")}`);
      throw e;
    }
  }
  throw new Error(`could not claim a slot after ${MAX_CLAIM_ATTEMPTS} attempts`);
}
