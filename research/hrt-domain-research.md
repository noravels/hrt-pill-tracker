# HRT pill tracker domain research

Last updated: 2026-05-19 01:08 +03

Important: this document is product/domain research for a medication tracking app. It is not medical advice, dose advice, or a treatment recommendation. The app must never recommend starting, stopping, increasing, or decreasing hormones. It should track a plan that the user explicitly enters or imports from a clinician/user-defined preset.

## User-provided starting use case

User pattern to support first:

- Start date/time: user enters `startedAt`.
- Testosterone blocker:
  - Product example: Androcur.
  - Active ingredient: cyproterone acetate.
  - Sold as 50 mg tablet in at least some markets.
  - User dose example: 12.5 mg = quarter of a 50 mg tablet.
  - Interval example: every 48 hours.
- Estrogen:
  - Product example: Climen / estradiol product.
  - Active ingredient tracking should be generic estradiol/estrogen, not hardcoded to a brand.
  - User dose example: 2 mg.
  - Route example: sublingual.
  - Interval example: every 12 hours.

The tracker must handle estrogen and T-blocker schedules independently because their intervals can differ.

## High-level findings

1. Do not model this as only “one pill twice a day”. Transfeminine HRT can include multiple medication categories, multiple products per category, variable dose fractions, and multiple routes.
2. The core domain should be medication-agnostic enough to support pills, patches, gel, injections, implants, and possibly non-hormone medications later.
3. The schedule engine should operate on dose events and intervals, not on fixed morning/evening slots only.
4. Alarm/notification scheduling should be platform-specific. Core should produce reminder requests; Android/iOS/Web schedule them.
5. Storage should be an interface provided to core. Core should not know SQLite directly.
6. Doses should separate:
   - active ingredient amount, e.g. 12.5 mg cyproterone acetate
   - physical form amount, e.g. 0.25 tablet of a 50 mg tablet
   - route, e.g. oral/sublingual/transdermal/injection
7. Brands and products should be optional metadata. The schedule should not depend on a brand name.

## Medication/category model implications

Minimum categories for MVP:

- Estrogen
- Testosterone blocker / anti-androgen

Do not hardcode only these categories in the storage shape. Use an enum for known categories plus a custom category escape hatch.

Recommended shape:

```text
MedicationCategory
  - estrogen
  - testosterone_blocker
  - progestogen          future
  - gnrh_analog         future
  - other
```

A `Medication` should represent a thing the user takes/tracks. It should not assume there is only one drug per category.

Examples:

```text
Medication A:
  category: testosterone_blocker
  productName: Androcur
  activeIngredients:
    - cyproterone_acetate 12.5 mg
  physicalDose:
    form: tablet
    unitCount: 0.25
    unitStrengthLabel: 50 mg tablet
  route: oral
  interval: 48 hours

Medication B:
  category: estrogen
  productName: Climen or custom label
  activeIngredients:
    - estradiol or estradiol_valerate 2 mg
  physicalDose:
    form: tablet
    unitCount: 1
  route: sublingual
  interval: 12 hours
```

## Dose amount vs physical amount

This is important because “12.5 mg Androcur” and “quarter pill” are both useful, but they mean different things.

Recommended model:

```text
DoseAmount:
  activeIngredientId
  amountDecimal
  unit: mg | mcg | g | mL | IU | custom

PhysicalDose:
  form: tablet | patch | gel | injection | implant | spray | custom
  quantityDecimal
  quantityUnit: tablet | patch | pump | vial | mL | custom
  fractionLabel optional: quarter | half | etc.
  productUnitStrengthLabel optional: 50 mg tablet
```

Why:

- Quarter/half tablet is a physical instruction.
- mg is an active dose amount.
- Patches may be “mcg/day” rather than “mg pill”.
- Gel may be pumps/grams plus estradiol content.
- Injections may be mg and mL, with vial concentration.

## Route model

Routes should be explicit because reminders and notes differ by route, and users may want route-specific history.

Initial route enum:

```text
Route
  - oral
  - sublingual
  - buccal
  - transdermal_patch
  - transdermal_gel
  - injection_im
  - injection_subq
  - implant
  - other
```

Do not make route control scheduling rules at first. Keep route as metadata unless/until we add route-specific pharmacokinetic helpers.

## Schedule model

Support interval-based schedules first.

```text
Schedule
  type: interval
  anchorAt: Instant
  intervalMinutes: Long
  gracePeriodMinutes optional
  reminderOffsetMinutes optional
```

For MVP, `anchorAt` can be the first dose time for that medication. `startedAt` belongs to treatment; `firstDoseAt`/`anchorAt` belongs to each medication.

This distinction matters because estrogen and blocker may not start at the exact same time.

Future schedule types to leave room for:

```text
ScheduleType
  - interval: every N minutes/hours/days
  - daily_times: specific local times, e.g. 09:00 and 21:00
  - weekly_days: e.g. patch every Monday/Thursday
  - cycle: e.g. 21 days on, 7 days off
  - as_needed: tracked but no next-dose expectation
```

For the current use case:

```text
Androcur:
  schedule.type = interval
  interval = 48 hours

Estrogen:
  schedule.type = interval
  interval = 12 hours
```

## Dose event model

A schedule produces expected dose occurrences. A dose event records what happened.

```text
DoseEvent
  id
  medicationId
  scheduledAt
  status: pending | taken | skipped | missed
  takenAt optional
  createdAt
  updatedAt
  note optional
```

Do not only store “taken count”. Taken count should be derived from events, with optional cached summaries later.

Rules to clarify later:

- If user takes a dose late, does the next dose stay on the original grid or shift from actual `takenAt`?
- If user skips a dose, does the schedule continue unchanged?
- If user takes an extra dose, is it attached to nearest scheduled occurrence or recorded as unscheduled?

For MVP, use a simple rule:

- Default schedule is anchored to `anchorAt` and interval; it does not drift.
- A late dose marks the matching scheduled occurrence as taken, but future scheduled times remain on the grid.
- Allow this to become a setting later: `scheduleDriftPolicy = fixed_anchor | shift_from_taken`.

## Core functions implied by the use case

Core SDK should provide pure/testable calculations plus repository-backed services.

Pure schedule functions:

```text
calculateOccurrences(medication, from, to)
getNextDose(plan, events, now)
getDoseStatus(occurrence, matchingEvent, now)
calculateTakenCount(events, medicationId?)
buildReminderRequests(plan, events, now, horizon)
```

Repository-backed service functions:

```text
getTreatmentSummary()
createTreatment(plan)
upsertMedication(medication)
markDoseTaken(medicationId, scheduledAt, takenAt)
markDoseSkipped(medicationId, scheduledAt)
getNextDose()
getTakenCount(medicationId?)
getReminderRequests(horizon)
```

## Reminder/alarm model

Core should emit reminder requests only:

```text
ReminderRequest
  id
  medicationId
  scheduledDoseAt
  fireAt
  title
  body
  platformPayload optional key/value map
```

Platform wrappers consume these:

- Android: AlarmManager/WorkManager + NotificationManager.
- iOS: UNUserNotificationCenter.
- Web later: Service Worker + Notifications API, if supported.

The core should expose a horizon-based planner, e.g. “next 7 days”, because iOS/Android have practical notification limits and permission differences.

## Storage interface implication

Core should define repository interfaces:

```text
TreatmentRepository
MedicationRepository
DoseEventRepository
SettingsRepository
```

Concrete storage lives outside pure core:

- Android wrapper can implement repositories with Room or SQLite.
- iOS wrapper can implement repositories with SQLite.swift/GRDB or a KMP-compatible storage adapter.
- Web bridge can later implement repositories with IndexedDB or SQLite WASM.

Core tests should use in-memory fake repositories.

## Sources reviewed

See `research/sources.md` for source list and key extracted details.
