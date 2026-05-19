# Android Phase 1 Design Direction

This document locks the proposed Android Phase 1 design direction before native implementation begins.

Implementation is not started by this document. It is a design handoff for a future Jetpack Compose app.

## Selected direction

Use `sketches/android/004-phase1-selected-flow/` as the Phase 1 baseline.

It combines:

- the calm, spacious home screen from `001-calm-cards`
- the routine clarity of `003-routine-timeline`
- a minimal setup flow that keeps local-only usage first

## Product tone

The app should feel:

- calm
- trustworthy
- private
- low-pressure
- practical
- not clinical
- not childish

The palette should be based on the transgender pride flag colors: soft blue, soft pink, and white. The colors should be used with restraint so the app feels identity-aware, calm, and trustworthy rather than loud or novelty-themed.

The app should not feel like a generic habit game. It is a sensitive medication tracker, so the design should avoid streak pressure, guilt language, or gamified punishment.

## Phase 1 screens

### 1. Today / Home

Purpose:

- Show the next dose immediately.
- Make “mark taken” the obvious primary action.
- Show active routine cards.
- Show local reminder status.

Primary components:

- local-only chip
- next dose hero card
- mark dose taken button
- active routine medication cards
- reminder status card
- bottom navigation

Home should work for:

- estrogen-only routines
- estrogen + T-blocker routines
- multiple active medications later

### 2. Setup

Purpose:

- Let the user create an initial local treatment routine.
- Avoid requiring login.
- Make T-blocker optional.

Initial setup fields:

- medication name
- dose label
- route
- interval
- first dose / anchor time
- reminders on/off

The first MVP can start with estrogen setup and optional T-blocker add/edit. The domain model already supports more flexible medication lists.

### 3. Routine

Purpose:

- Show the schedule as a timeline.
- Show taken/upcoming items.
- Let users understand fixed-anchor behavior without technical wording.

Timeline should show:

- completed doses
- next dose
- later upcoming doses
- optional meds without implying they are mandatory

### 4. Settings

Purpose:

- Local-only mode status.
- Local notification status.
- Medical disclaimer.
- Future placeholders for Phase 2 and Phase 3.

Phase 2 placeholder:

- optional backup/sync with Google account

Phase 3 placeholder:

- donation/support
- advertising, if added later

## Navigation

Use simple bottom navigation:

- Today
- Routine
- History
- Settings

For Phase 1, History may be minimal or read-only. It can show taken dose events from local storage.

## Visual system

### Colors

- Palette basis: transgender pride flag colors — blue `#5BCEFA`, pink `#F5A9B8`, and white `#FFFFFF`
- Background: pale trans-blue/white wash in light mode; near-black blue-charcoal in dark mode
- Surface: white cards in light mode; elevated blue-charcoal cards in dark mode
- Accent: accessibility-adjusted trans blue for primary controls
- Secondary accent: trans pink for pills, notes, and warm highlights
- Success/reminder: teal-blue derived from the trans-blue family
- Warning/attention: muted pink rather than harsh red/orange
- Text: dark blue-charcoal in light mode; warm off-white in dark mode
- Dividers: pale blue-gray in light mode; low-contrast blue-gray in dark mode

### Shape

- Large rounded cards
- Rounded buttons
- Soft chip pills
- No sharp clinical table-first layout on the home screen

### Typography

- Android native Material typography is fine.
- Support light and dark mode from Phase 1; dark mode should be calm blue-charcoal, not pure black.
- Emphasize large next-dose time.
- Use concise labels and avoid medical advice wording.

## Interaction principles

- Marking a dose taken should provide immediate confirmation.
- The next dose should update after a dose is marked taken.
- Local-only status should be visible but not scary.
- Reminder permission should be requested at the right moment, not during the first screen if avoidable.
- Avoid guilt language for missed doses.

## Compose implementation mapping

Suggested Compose components:

- `TodayScreen`
- `NextDoseHeroCard`
- `MedicationRoutineCard`
- `ReminderStatusCard`
- `SetupMedicationScreen`
- `IntervalPicker`
- `RoutePicker`
- `RoutineTimelineScreen`
- `SettingsScreen`
- `LocalOnlyBanner`

Suggested state sources:

- Core SDK `TreatmentService.summary()`
- Core SDK `TreatmentService.nextDose()`
- Core SDK `TreatmentService.markTaken()`
- Core SDK `TreatmentService.buildReminders()`

Platform-specific implementation remains outside core:

- local DB adapter
- notification scheduler
- Android permission handling

## Mockup files

Open the selected design:

```bash
open sketches/android/004-phase1-selected-flow/index.html
```

Other variants remain available for comparison:

```bash
open sketches/android/001-calm-cards/index.html
open sketches/android/002-compact-schedule/index.html
open sketches/android/003-routine-timeline/index.html
```
