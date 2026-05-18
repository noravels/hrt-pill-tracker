# Architecture implications from HRT domain research

This document converts research notes into technical constraints for the initial architecture.

## Core principles

1. Core is schedule/domain logic only.
2. Core receives repository interfaces; it does not own SQLite directly.
3. Notifications are platform-specific; core returns reminder requests.
4. Medication model separates product, active ingredients, physical dose, route, and schedule.
5. Taken counts and next dose are derived from dose events and schedules.
6. The first implementation can support the T-Fem-like two-medication use case without hardcoding the whole domain to only two medications.

## Proposed entities

```text
TreatmentPlan
  id
  startedAt
  timezone
  medications[]

Medication
  id
  treatmentId
  category
  displayName
  productName optional
  activeIngredients[]
  physicalDose optional
  route
  schedule
  remindersEnabled
  notes optional

ActiveIngredientDose
  ingredientId
  displayName
  amount
  unit

PhysicalDose
  form
  quantity
  quantityUnit
  fractionLabel optional
  productUnitStrengthLabel optional

Schedule
  type
  anchorAt
  intervalMinutes optional
  localTimes optional future
  weeklyDays optional future
  cycle optional future
  driftPolicy

DoseEvent
  id
  medicationId
  scheduledAt
  status
  takenAt optional
  note optional

ReminderRequest
  id
  medicationId
  scheduledDoseAt
  fireAt
  title
  body
```

## MVP-specific preset

The app can include a user-editable preset for the first use case:

```text
Preset: T-Fem-style interval tracker

Medication 1:
  category: testosterone_blocker
  displayName: Androcur
  active ingredient: cyproterone acetate 12.5 mg
  physical dose: 0.25 tablet, product unit strength 50 mg tablet
  route: oral
  schedule: every 48 hours from user-selected first dose time

Medication 2:
  category: estrogen
  displayName: Estradiol / Climen
  active ingredient: estradiol or user-defined estradiol label 2 mg
  physical dose: 1 tablet unless user edits
  route: sublingual
  schedule: every 12 hours from user-selected first dose time
```

The preset must be editable before saving.

## Open product decisions

Need decide later:

1. Late dose behavior:
   - fixed anchor: next dose remains on original schedule grid
   - shift from taken: next dose is interval after actual taken time

2. Missed threshold:
   - when does pending become missed?
   - global default vs per-medication grace period

3. Combination products:
   - If a product contains estrogen + anti-androgen, do we track it as one medication with two active ingredients, or split into two logical medication tracks?
   - Recommended: allow both; MVP can use one medication per schedule.

4. Inventory:
   - Should quarter-tablet consumption subtract 0.25 from stock?
   - Not MVP, but physical dose model enables this later.

5. Dose units:
   - Need decimal-safe representation. Avoid binary floating point for mg/tablet fractions.
   - Kotlin: store decimal as string or scaled integer.

## Test cases to write first

1. Given a medication anchored at 2026-01-01T09:00 every 12 hours, next dose at 2026-01-01T10:00 is 2026-01-01T21:00.
2. Given a blocker anchored every 48 hours, estrogen every 12 hours, next dose calculation returns the earlier due occurrence across both.
3. Given one taken event for estrogen, taken count for estrogen is 1 and total taken count is 1.
4. Given a quarter-tablet physical dose, display formatter returns “1/4 tablet (12.5 mg cyproterone acetate)” or equivalent.
5. Given fixed-anchor drift policy and a late taken dose, future occurrences remain anchored to the original grid.
6. Given reminder horizon of 7 days, core emits reminder requests only within that horizon.

## Recommended initial tech stack after research

- Core SDK: Kotlin Multiplatform.
- Time: kotlinx-datetime.
- Serialization: kotlinx-serialization-json.
- Async/service layer: kotlinx.coroutines.
- Tests: kotlin.test with fake in-memory repositories.
- Android wrapper: Kotlin + Jetpack Compose + platform repository implementation + Android notification scheduler.
- iOS wrapper: SwiftUI + KMP framework + platform repository implementation + UNUserNotificationCenter scheduler.
- Web: keep as bridge TODO for now.
