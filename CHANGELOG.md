# Changelog

Work log of OTPilot, newest first. Releases and their APKs are on
[GitHub Releases](https://github.com/tvnclr/OTPilot/releases); user-facing
release notes for stores live in `fastlane/metadata/android/*/changelogs/`.

## 1.1.2 (in progress, branch `dev`)

### 2026-11-14 — Emulator check of the other 20 languages
- Screenshots of home, permissions, settings and about in bg, bn, ca, et, fa,
  fr, hi, id, it, he, ko, pl, pt, ro, ru, tr, uk, vi, zh-Hans and zh-Hant
  (80 screens, `OTPilot-prints/emu/capture.ps1`). Layout, glyphs and RTL were
  fine in all of them.
- Fixed in the strings inherited from OTP Helper: Hebrew had about 65 strings
  still in English (settings, history, phrases, permissions); a few left in
  English in Hindi, Italian, Polish, Vietnamese, and "Ignore ID" in 8 locales;
  wrong meanings ("About me" in Hindi, "About us" in Polish and Vietnamese,
  "About" untranslated and "Show toasted bread" in Italian, a "backgroud" typo).
- Not covered by any check: grammar a native speaker would notice.

### 2026-11-14 — Emulator check of the translations
- Android Emulator (API 37, Google APIs, x86_64) set up on the build PC to test
  without the phone; how to use it is in AGENTS.md.
- Checked Arabic (RTL), Japanese, German, Spanish and Tamil screens, plus a
  simulated SMS end to end (detected and masked as expected).
- Fixed: German row labels split mid-word (shorter labels); Tamil status pills
  too long, squeezing the row titles; "Android 15+" and "AGPL-3.0" reordered by
  the bidi algorithm in Arabic, Persian and Hebrew; two upstream Arabic labels.
- tvnclr-ui 1.0.2: prose styles hyphenate long words; code and mono text is
  always left-to-right (the version showed as "dev.4-1.1.2" in Arabic).

### 2026-11-13 — All app languages
- OTPilot's own strings translated into the 25 other languages the app ships
  (not reviewed by native speakers yet); tvnclr-ui 1.0.1 with its two strings.
- The translation test now covers every shipped locale.

### 2026-11-11 — F-Droid readiness
- Store listing rewritten for OTPilot (en-US, pt-BR), own changelogs.
- Upstream leftovers removed: `app_name` in 27 locales, `otphelper://` deep
  links (now `otpilot://`), `io.github.jd1378.otphelper.*` actions/permission,
  `Theme.OtpHelper`.
- Builds from a stable tag report the stable version (F-Droid builds detached).
- No dependency-metadata block and no git commit stamped in the APK, so the
  same code builds byte-identical APKs on any branch (reproducible builds).

## 1.1.1 — 2026-11-09
- Fixed: with root, codes Android marks as sensitive arrived redacted after a
  reboot or a Grant until the app restarted; the listener is now rebound right
  after the grant.
- Fixed: lowercase words after "code"/"código" were taken as codes.

## 1.1.0 — 2026-11-07
- New icon; package renamed to `dev.tvnclr.otpilot`; tvnclr-ui as a submodule.
- Context fallback limited (qualified keyword, 200-character distance).
- "Remain open" opens the system battery dialog; Grant (root) explains the
  KernelSU step and tells "no su" apart from a denied request.

## 1.0.0 — 2026-11-01
- First release, forked from OTP Helper 1.20.7.
