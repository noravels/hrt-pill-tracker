# Android Design Mockups

Three disposable HTML mockups explore Android phase 1 directions for the HRT pill tracker. They are intentionally self-contained sketches, not app implementation code.

## Variants

| Variant | Direction | Strengths | Tradeoffs |
| --- | --- | --- | --- |
| [001 Calm Cards](001-calm-cards/) | Spacious home screen centered on the next dose | Most reassuring; clearest primary action; strong local-only messaging | Less dense; schedule details require more vertical space |
| [002 Compact Schedule](002-compact-schedule/) | Schedule-first utility UI with tabs and list rows | Fast scanning; most Material-like; efficient use of space | Feels more operational and less emotionally warm |
| [003 Routine Timeline](003-routine-timeline/) | Chronological routine view with expandable reminder sheet | Best sense of daily progress; optional meds are naturally represented | More custom; timeline may be heavier to implement cleanly |
| [004 Phase 1 Selected Flow](004-phase1-selected-flow/) | Consolidated proposed Phase 1 design with Home, Setup, Routine, Settings and visible trans-flag theme | Best implementation handoff; combines calm cards with guided setup, timeline, and explicit brand palette | Less exploratory; assumes the 001 direction as baseline |
| [005 Subtle Trans Palette](005-subtle-trans-palette/) | Preserved previous revision with softer trans-flag-inspired colors | Useful rollback/reference if the stronger theme feels too bold | Less visibly “main theme is trans” than requested |
| [006 Trans Flag Native](006-trans-flag-native/) | Stronger Claude Design pass where the trans flag becomes the app chrome, dose surface, navigation language, and visual structure | Clear trans identity; useful reference | Still leans on gradients/top visual treatment |
| [007 Trans Structural Blocks](007-trans-structural-blocks/) | Non-gradient block system: side rails, solid section blocks, selected states, and vertical dose rail | Best response to “not just gradient on top”; most native-token friendly | Bolder, chunkier graphic style needs refinement before Compose |

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

Use 007 Trans Structural Blocks as the current Android implementation handoff. It responds to the “not just gradient on top” feedback: the trans flag colors become side rails, solid section blocks, selected states, and a vertical dose-card rail rather than a decorative top gradient.

004 Phase 1 Selected Flow remains a previous selected-flow reference, and 005 Subtle Trans Palette is the saved softer revision if a less flag-forward direction is needed later.

001 Calm Cards remains the baseline structural language. Borrow selectively from 002 Compact Schedule for the schedule list and reminder tab if users ask for denser daily planning. Borrow from 003 Routine Timeline if the app later needs a more habit/routine-oriented daily progress view.
