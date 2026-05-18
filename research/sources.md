# Research sources

Last updated: 2026-05-19 01:08 +03

This file records sources checked for product/domain modeling. It is not medical advice.

## 1. Rainbow Health Ontario - Feminizing Hormone Therapy

URL: https://www.rainbowhealthontario.ca/TransHealthGuide/gp-femht.html

Relevant points extracted:

- Feminizing hormone therapy commonly involves estrogen and anti-androgens.
- Estrogen forms/routes include oral estradiol, transdermal patches, transdermal gel, and injectable estradiol valerate.
- Anti-androgen options discussed include spironolactone and cyproterone.
- The page lists cyproterone oral dosing examples including “12.5 mg (1/4 50 mg tab) q2d - daily” as a starting-dose style entry in its dose table.
- The page lists estradiol oral examples such as 1-2 mg daily as starting dose and divided daily dosing in usual/maximum dose examples.
- The page explicitly mentions sublingual/injectable/transdermal route considerations in risk mitigation context.

Product implications:

- The app should support at least estrogen + anti-androgen categories.
- The app must support fractional tablets such as 1/4 of a 50 mg tablet.
- The app must support route metadata: oral, sublingual, transdermal patch, gel, injectable.
- The app must support different intervals per medication.

## 2. UK emc - Androcur 50 mg tablets SmPC

URL: https://www.medicines.org.uk/emc/product/15996/smpc

Relevant points extracted:

- Product: Androcur 50 mg tablets.
- Active ingredient: cyproterone acetate.
- Form: tablet.
- Method of administration: oral administration, tablets taken with liquid after meals.

Product implications:

- Brand/product name and active ingredient should be separate fields.
- Physical product strength should be representable as “50 mg tablet”.
- User dose should be representable as both active amount, e.g. 12.5 mg cyproterone acetate, and physical amount, e.g. 0.25 tablet.

## 3. PMC article - How low can you go? Titrating the lowest effective dose of cyproterone acetate

URL: https://pmc.ncbi.nlm.nih.gov/articles/PMC12573561/

Relevant points extracted:

- The article describes feminizing GAHT as commonly consisting of feminizing hormones such as estradiol and an anti-androgen to block testosterone release/effect.
- Cyproterone acetate and spironolactone are described as commonly prescribed anti-androgens in the discussed Australian context.
- The study includes low/alternate dosing concepts, including 12.5 mg cyproterone twice weekly in its protocol endpoint discussion.
- Baseline participant data included oral estradiol valerate and topical estradiol.

Product implications:

- Alternate-day/twice-weekly style T-blocker schedules should not be impossible in the model.
- The core should not assume daily-only dosing.
- The model should support active ingredient, route, and interval independently.

## 4. RxReasoner - Climen tablet overview

URL: https://www.rxreasoner.com/monographs/climen

Relevant points extracted:

- Product: Climen coated tablet.
- Active ingredients listed: cyproterone and estradiol.
- Product is described as a combination of estrogen and progestin/anti-androgen ingredient.
- Pack size listed as 21 tablets in the overview.

Product implications:

- A single product may contain multiple active ingredients.
- The model should allow a medication/product dose to list multiple active ingredients.
- Brand “Climen” should not be treated as synonymous with only one generic ingredient.

## 5. Prospecte.ro - CLIMEN 2 mg / 1 mg leaflet metadata

URL: https://prospecte.ro/en/drugs/climen-2mg-1mg-sugar-coated-tablets-bayer-w64043001

Relevant points extracted:

- Product: CLIMEN 2 mg / 1 mg sugar-coated tablets.
- Substance listed as estradiol + cyproterone.
- Concentration metadata: 2 mg / 1 mg.
- Quantity metadata: 21.

Product implications:

- Combination tablets need multiple active ingredients and per-ingredient strengths.
- The schedule should track the user’s intended dose, not assume every product maps to exactly one category.
- For MVP, we can still let the user label the estrogen medication as “Climen 2 mg sublingual”, but the data model should not prevent combination products.

## Notes on source reliability and use

- Medical guideline/source content is used only to identify real-world schedule/product shapes the app may need to represent.
- The app should avoid dose recommendations and should display medical disclaimer language.
- User-entered data should be treated as the source of truth for their schedule.
- Presets, if added, should be editable and labeled as templates, not recommendations.
