# AGENTS.md

Instructions for any coding agent (Claude, Codex, etc.) working on OTPilot.
Read this, README.md and the top of CHANGELOG.md before changing anything.

## What this is
OTPilot is a fork of [OTP Helper](https://github.com/jd1378/otphelper)
(AGPL-3.0). Keep the "Fork of OTP Helper" credit in the About screen and the
README: it is required by the license and the owner wants it regardless.

## Branches and releases
- `dev`: day-to-day work. Every build installed for testing is published as a
  GitHub pre-release `v<version>-dev.N` with the APK `OTPilot-<version>-dev.N.apk`
  (never delete pre-releases).
- `main`: published stable releases only (`vX.Y.Z`, APK `OTPilot-X.Y.Z.apk`).
- `fdroid`: stable code for F-Droid, created from `main`; only F-Droid-specific
  changes live there. Keep features in all branches unless F-Droid requires
  removing one.
- Version: `baseVersion` and `versionCode` in `app/build.gradle.kts`.
  `versionName` is the base version on `main` or on a stable tag, otherwise
  `<base>-dev.N` (N = commits since the last stable tag).
- Never commit the signing key (it lives outside the repo, `../OTPilot-signing`).

## Structure
- `app/`: the app. OTPilot code is in `dev.tvnclr.otpilot`; upstream code stays
  in `io.github.jd1378.otphelper` (Kotlin namespace) to keep merges simple.
- `app/src/main/res/values*/otpilot.xml`: OTPilot's own strings. Upstream
  strings are in `strings.xml`.
- `tvnclr-ui/`: git submodule with the shared Compose design system
  ([tvnclr/tvnclr-ui](https://github.com/tvnclr/tvnclr-ui), GPL-3.0). Change
  components there, tag a new version, then bump the submodule.
- `fastlane/metadata/android/`: store listing (en-US, pt-BR).

## Build and test (Windows PC, from the repo root)
```
set JAVA_HOME=C:\Claude folder\figurinhas-gato\tools\jdk\jdk-17.0.20.1+1
set GRADLE_USER_HOME=C:\Claude folder\figurinhas-gato\tools\gradle-home
gradlew.bat testNormalReleaseUnitTest assembleNormalRelease --offline
```
- `OtpilotCorpusTest`: real SMS/email corpus in `app/src/test/resources/`;
  add a case for every detection bug.
- `OtpilotTranslationsTest`: every string in `otpilot.xml` and tvnclr-ui must
  exist in every shipped locale, with the same format arguments. A new string
  needs all locales in the same commit.

## Testing on a device
- Emulator first: AVD `otpilot-test` (Android 17, API 37, Google APIs, no Play
  Store) in `C:\Claude folder\android-emulator\avd`. Start it headless:
  ```
  set ANDROID_AVD_HOME=C:\Claude folder\android-emulator\avd
  <sdk>\emulator\emulator.exe -avd otpilot-test -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect -no-snapshot-save
  ```
  `<sdk>` = `C:\Claude folder\figurinhas-gato\tools\android-sdk`.
  App language: `adb shell cmd locale set-app-locales dev.tvnclr.otpilot --user 0 --locales ar`.
  Simulated SMS: `adb emu sms send 30340 "Your code is 123456"`.
  All languages at once: `C:\Claude folder\OTPilot-prints\emu\capture.ps1` takes home,
  permissions, settings and about in every locale listed in `locs.json` next to it
  (screenshots in `emu\locales\`). Look for words split mid-word, clipped text,
  missing glyphs, RTL reordering and strings still in English.
- Owner's phone (POCO X7 Pro, KernelSU fork) only for what the emulator cannot
  show: root grant, sensitive-notification redaction, ROM quirks. Screenshots
  for the README must not show the status bar, notifications or personal data.
