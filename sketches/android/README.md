# Android Design Mockups

Three disposable HTML mockups explore Android phase 1 directions for the HRT pill tracker. They are intentionally self-contained sketches, not app implementation code.

## Variants

| Variant | Direction | Strengths | Tradeoffs |
| --- | --- | --- | --- |
| [001 Calm Cards](001-calm-cards/) | Spacious home screen centered on the next dose | Most reassuring; clearest primary action; strong local-only messaging | Less dense; schedule details require more vertical space |
| [002 Compact Schedule](002-compact-schedule/) | Schedule-first utility UI with tabs and list rows | Fast scanning; most Material-like; efficient use of space | Feels more operational and less emotionally warm |
| [003 Routine Timeline](003-routine-timeline/) | Chronological routine view with expandable reminder sheet | Best sense of daily progress; optional meds are naturally represented | More custom; timeline may be heavier to implement cleanly |

## Shared requirements covered

- Android/mobile viewport with Material-inspired patterns.
- No login or cloud account requirement in the UI.
- Next dose is visible near the top.
- Primary action to mark a dose taken.
- Estrogen schedule is shown with generic Estradiol content.
- T-blocker schedule is shown as optional, with support for estrogen-only plans.
- Local reminder status is visible.
- Each mockup includes at least one inline JavaScript interaction.
- HTML/CSS/JS are self-contained with no build step.

## Recommendation

Use 001 Calm Cards as the baseline for phase 1. It best matches the medical/sensitive product tone: calm, trustworthy, low-pressure, and focused on the next safe action. It also communicates local-only privacy clearly without making the app feel like a settings screen.

Borrow selectively from 002 Compact Schedule for the schedule list and reminder tab if users ask for denser daily planning. Borrow from 003 Routine Timeline if the app later needs a more habit/routine-oriented daily progress view.
