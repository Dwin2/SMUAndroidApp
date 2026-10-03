# Play Store Internal Testing — Release Checklist

This is what you need to do on your end. The codebase is already set up with the
release signing config, manifest declarations, and version metadata.

## 1. Generate the release keystore (one-time, irreversible)

```bash
keytool -genkey -v \
  -keystore ~/smu-study-release.keystore \
  -alias smu-study \
  -keyalg RSA -keysize 2048 -validity 10000
```

It will prompt for:
- Keystore password (write it down)
- Key password (use the same one for simplicity)
- Distinguished name fields — minimum: First/Last Name = "Darwin Li", Org Unit = "SMU Study", Org = "Harvard University", City/State/Country = your choice.

**CRITICAL:** Back this `.keystore` file up to at least two places (1Password vault
+ Harvard Drive). Losing it means you can never push an update to this app on Play
Store again — you'd have to publish under a brand new package name.

## 2. Add keystore credentials to `local.properties`

Append these lines to `local.properties` (already gitignored):

```
RELEASE_KEYSTORE_FILE=/Users/darwinli/smu-study-release.keystore
RELEASE_KEYSTORE_PASSWORD=<the keystore password>
RELEASE_KEY_ALIAS=smu-study
RELEASE_KEY_PASSWORD=<the key password>
```

## 3. Build the release AAB

```bash
./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

If `local.properties` is missing the keystore lines, you'll get an unsigned AAB
that Play Store won't accept — the conditional in `build.gradle` keeps debug
builds working without those vars set.

## 4. Play Console setup

### 4a. Create a developer account
- https://play.google.com/console — $25 one-time fee.
- Check first whether Harvard has an institutional developer account that can
  publish on behalf of the lab. The PI (yuning_liu@g.harvard.edu) likely knows.

### 4b. Create the app
- Name: "SMU Study" (or whatever you want participants to see)
- Default language: English (US)
- App or game: App
- Free or paid: Free
- Declarations: agree to all

### 4c. App content forms (left sidebar → "App content")

| Section | Answer |
|---|---|
| Privacy policy | Required URL — use a Google Doc set to "Anyone with the link" or a Harvard webpage. Must mention: data collected (Prolific ID, app open/close times, survey responses, EMA timestamps), purpose (academic research), retention, contact email. |
| App access | "All functionality is available without restrictions" *unless* setup is gated — your onboarding gates content behind Prolific ID, so check "All or some functionality is restricted" and provide a test Prolific ID + screenshots. |
| Ads | No |
| Content rating | Fill the questionnaire — this is "Everyone" / no objectionable content. |
| Target audience | 18+ |
| News app | No |
| COVID-19 contact tracing | No |
| Data safety | See section 4d. |
| Government app | No |
| Financial features | No |
| Health features | This is a research study about well-being — answer "No" for clinical health features but mention the research nature in the app description. |

### 4d. Data Safety form (most tedious — block out 30 min)

You collect and transmit:
- **Prolific Participant ID** (User IDs → "Other IDs"), purpose: App functionality + Analytics, required, encrypted in transit, user can request deletion
- **App package names + open/close timestamps** (App activity → "Other actions"), purpose: App functionality + Analytics, required
- **Survey responses (text + multiple choice)** (User-generated content → "Other"), purpose: App functionality + Analytics, required
- **Diagnostics: none** unless you wire crash reporting later

For all entries: Encrypted in transit = Yes. User can request deletion = Yes (you delete on request via your backend). Sold to third parties = No.

### 4e. Permission declarations (this is the tricky one)

Play Console will flag these:

**Accessibility (BIND_ACCESSIBILITY_SERVICE)** — answer the prompt with:
> The app is a 7-day IRB-approved Harvard research study (PI: Dr. Yuning Liu, yuning_liu@g.harvard.edu) investigating motivation awareness during social media use. The accessibility service detects when participants open social media apps from their pre-selected list (Instagram, TikTok, etc.) so the app can show a brief in-the-moment survey. Participants opt in via Prolific and provide informed consent before the service activates. The service does not read screen content or interact with other apps; it only responds to TYPE_WINDOW_STATE_CHANGED events to identify the foreground package name. Data is stored on a private Harvard-administered backend and used only for academic research.

**SYSTEM_ALERT_WINDOW** — explain:
> Used to render the brief in-the-moment survey prompt as an overlay on top of the participant's social media app. Required because Android 13+ blocks background activity launches for non-accessibility activities; the overlay is the only reliable way to show the survey at the moment of app open. Overlay only appears when a study app is opened during the participant's chosen sampling window, max 15 times per day per the study protocol.

**FOREGROUND_SERVICE_SPECIAL_USE** — already declared in manifest with a `<property>` element. Play Console will also ask for a written explanation; reuse the property text:
> Keeps the SMU research study's accessibility service alive for the 7-day study window so in-the-moment EMA prompts can fire when participants open selected social media apps.

**SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM** — explain:
> The study protocol requires EMA (Ecological Momentary Assessment) survey notifications to fire at participant-scheduled times (5pm and 9pm local time). Inexact alarms are not acceptable for time-sensitive research data collection.

### 4f. Store listing
- App name: "SMU Study"
- Short description (80 chars): "Research study on social media use motivation. Invite-only via Prolific."
- Full description (4000 chars): describe study, mention IRB, list Harvard PI contact, note that the app only works for Prolific participants with a valid study code.
- App icon: 512x512 PNG. Your current launcher icon should work — export from `res/mipmap-xxxhdpi`.
- Feature graphic: 1024x500 PNG. Required.
- Screenshots: minimum 2 phone screenshots. Take from Android Studio emulator.
- Category: Health & Fitness or Lifestyle

### 4g. App release → Internal testing
1. Left sidebar → Testing → Internal testing → Create new release.
2. Drag & drop `app-release.aab`.
3. Release name: "1.0 (1)" — auto-fills from versionName/versionCode.
4. Release notes: "Initial internal testing release."
5. Save → Review release → Start rollout.

First upload triggers a ~1–24 hour review even on Internal Testing. Subsequent
updates push within minutes.

### 4h. Add testers
- Internal testing → Testers → Create email list.
- Add tester emails (you, your PI, a couple of pilot participants).
- Copy the "Opt-in URL" — testers must open it and accept before they can see
  the app in Play Store on their device.

For your real Prolific cohort, send them the same opt-in URL via Prolific's
participant instructions.

## 5. Firebase note

`google-services.json` is checked in but Firebase isn't actually wired up in
code. If you add Firebase later (for FCM push, Crashlytics, etc.), you'll need
to register the **release signing certificate's SHA-1** in the Firebase
console — get it with:

```bash
keytool -list -v -keystore ~/smu-study-release.keystore -alias smu-study
```

## What I changed in the codebase for you

- `app/build.gradle` — added `signingConfigs.release` reading from `local.properties` (gracefully omitted if not configured).
- `AndroidManifest.xml` — removed unused `PACKAGE_USAGE_STATS`, added `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE">` to `MonitorForegroundService` (Android 14 compliance).
- The `BuildConfig.DEBUG` Chrome whitelist in `SamplingManager` will be stripped automatically in `bundleRelease`.

`versionCode 1`, `versionName "1.0"`, `targetSdk 34`, `minSdk 26` — already
correct for first Play Store submission.
