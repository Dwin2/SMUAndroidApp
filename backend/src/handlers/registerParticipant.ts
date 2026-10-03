import type { APIGatewayProxyEventV2, APIGatewayProxyHandlerV2 } from "aws-lambda";
import { json, parseBody, requireApiKey } from "../lib/auth";
import { ParticipantMeta } from "../lib/types";
import {
  AllocationExhaustedError,
  EnrollmentInProgressError,
  assignCondition,
} from "../lib/allocation";

export const handler: APIGatewayProxyHandlerV2 = async (event: APIGatewayProxyEventV2) => {
  const authErr = requireApiKey(event);
  if (authErr) return authErr;

  const raw = parseBody<unknown>(event);
  if (!raw) return json(400, { error: "invalid_json" });

  // Format validation happens here, before any slot is touched, so a malformed Prolific ID
  // never consumes an allocation slot.
  const parsed = ParticipantMeta.safeParse(raw);
  if (!parsed.success) {
    return json(400, { error: "validation_failed", issues: parsed.error.issues });
  }

  // The client-sent studyGroup is dropped: the condition comes only from the allocation list.
  const { prolificId, studyGroup: _ignored, ...fields } = parsed.data;

  try {
    const studyGroup = await assignCondition(prolificId, fields);
    return json(200, { ok: true, prolificId, studyGroup });
  } catch (e) {
    // Error bodies never mention the condition — participants are blind.
    if (e instanceof EnrollmentInProgressError) return json(503, { error: "enrollment_in_progress" });
    if (e instanceof AllocationExhaustedError) {
      console.error("ALLOCATION EXHAUSTED", e.message);
      return json(503, { error: "enrollment_closed" });
    }
    console.error("registerParticipant failed", e);
    return json(500, { error: "internal_error" });
  }
};
