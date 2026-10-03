import { z } from "zod";

// Prolific IDs are 24 hex characters. Validated before any allocation slot is claimed so a
// typo can't burn a slot (and then a second one when the participant retypes it).
export const PROLIFIC_ID_REGEX = /^[0-9a-f]{24}$/;
const PROLIFIC_ID = z.string().trim().toLowerCase().pipe(z.string().regex(PROLIFIC_ID_REGEX));

export const ParticipantMeta = z.object({
  prolificId: PROLIFIC_ID,
  // Ignored: the condition comes only from the allocation list (see lib/allocation.ts).
  // Still accepted so older app builds that send it aren't rejected.
  studyGroup: z.string().optional(),
  enrollmentDate: z.number().int().nonnegative(),
  samplingWindowStartMin: z.number().int().min(0).max(1439),
  samplingWindowEndMin: z.number().int().min(0).max(1439),
  selectedApps: z.array(z.string()).default([]),
  currentStudyDay: z.number().int().min(0).max(8).default(0),
  appVersion: z.string().optional(),
});
export type ParticipantMeta = z.infer<typeof ParticipantMeta>;

export const SurveyResponseDto = z.object({
  localId: z.number().int(),
  participantCode: PROLIFIC_ID,
  surveyType: z.enum([
    "BASELINE", "MRP", "NP", "SATISFACTION",
    "EMA_5PM", "EMA_9PM", "ENDLINE", "FOLLOWUP",
  ]),
  appPackage: z.string().default(""),
  sessionId: z.string().default(""),
  studyDay: z.number().int(),
  timestamp: z.number().int(),
  responseJson: z.string(),
});
export type SurveyResponseDto = z.infer<typeof SurveyResponseDto>;

export const AppSessionDto = z.object({
  sessionId: z.string().min(1),
  participantCode: PROLIFIC_ID,
  appPackage: z.string(),
  appName: z.string(),
  openTime: z.number().int(),
  closeTime: z.number().int().default(0),
  studyDay: z.number().int(),
  promptShown: z.boolean(),
  promptType: z.string().default(""),
  satisfactionAnswered: z.boolean(),
  withinSamplingWindow: z.boolean(),
});
export type AppSessionDto = z.infer<typeof AppSessionDto>;

export const SyncRequest = z.object({
  prolificId: PROLIFIC_ID,
  surveys: z.array(SurveyResponseDto).default([]),
  sessions: z.array(AppSessionDto).default([]),
});
export type SyncRequest = z.infer<typeof SyncRequest>;
