# HRT Pill Tracker

HRT Pill Tracker is an early-stage medication tracking project for gender-affirming hormone therapy routines.

The goal is to provide a shared core scheduling and tracking engine that can be reused across native Android, native iOS, and web clients without duplicating business logic.

## Project goals

- Track medication plans with independent schedules per medication.
- Support estrogen, testosterone blockers, and future medication categories without hardcoding one fixed regimen.
- Support different forms and routes, such as tablets, fractional tablets, sublingual use, patches, gels, and injections.
- Calculate treatment start, taken dose counts, next dose times, and reminder requests from a common core library.
- Keep storage, notification scheduling, and account sync platform-specific behind interfaces.
- Build native UI wrappers around the shared core for Android and iOS.
- Support local-first mobile usage, with optional Google/Apple account sync later.
- Keep web support as a bridge/client target that can use the same core model later; web is expected to require login for persistent tracking.

## Architecture direction

Planned high-level structure:

```text
src/
  lib/      shared core SDK
  android/  native Android wrapper
  ios/      native iOS wrapper
  web/      web bridge/client

tests/      shared fixtures and integration tests
research/   domain and architecture research notes
```

The core library should own domain concepts and scheduling calculations, but not platform APIs.

Core responsibilities:

- treatment plans
- medication definitions
- dose schedules
- dose events
- next-dose calculation
- taken-count calculation
- reminder request generation

Platform responsibilities:

- SQLite or other local persistence implementation
- optional account connection and sync
- notification/alarm scheduling
- native UI
- platform permissions and lifecycle handling

## Current status

This repository is in the research and architecture phase. Implementation details may change as the core model is refined.

## Medical disclaimer

This project is a tracking tool only. It does not provide medical advice, dosing recommendations, diagnosis, or treatment guidance. Medication plans should be configured by the user based on their own clinician-approved or personally chosen regimen.
