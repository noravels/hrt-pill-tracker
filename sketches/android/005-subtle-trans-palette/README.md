# Variant: Subtle Trans Palette Revision

## Design stance

This preserves the first trans-flag-inspired revision before making the main Phase 1 direction more visibly trans-themed.

## Key choices

- Layout: mobile Android frame with separate Home, Setup, Routine, and Settings screens.
- Visual tone: trans-flag-inspired palette (soft blue, pink, white) with calm medical-tracker restraint; identity-aware without looking loud, clinical, or childish.
- Primary action: the home hero makes “Mark dose taken” the clearest action.
- Privacy: local-only mode is visible but not alarmist.
- Optionality: T-blocker is represented as optional, so estrogen-only routines do not look like an error.
- Future phases: settings includes Phase 2 backup/sync and Phase 3 support/revenue placeholders without implementing them.
- Interaction: side panel switches screens; “Mark dose taken” shows a toast and updates button state; setup save returns to home; Light/Dark toggle previews both themes.

## Trade-offs

- Strong at: Phase 1 clarity, low-pressure sensitive health UX, local-only confidence, easy Compose translation.
- Weak at: less dense than a power-user schedule table; advanced history analytics are not designed yet.

## Best for

- First Android MVP where the user opens the app mainly to check the next dose and mark it taken.
- Users who may have estrogen-only or estrogen + blocker routines.
- A native Compose implementation that should start simple and expand screen-by-screen.
