package app.transition.hrt.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class TreatmentServiceTest {
    private val base = Instant.parse("2026-01-01T08:00:00Z")

    @Test
    fun twelveHourEstrogenNextDose() {
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60)))

        val next = service.nextDose(Instant.parse("2026-01-01T09:00:00Z"))

        assertNotNull(next)
        assertEquals("estrogen", next.medicationId)
        assertEquals(Instant.parse("2026-01-01T20:00:00Z"), next.scheduledAt)
    }

    @Test
    fun eightHourEstrogenNextDose() {
        val service = serviceWith(planWith(estrogen(intervalMinutes = 8 * 60)))

        val next = service.nextDose(Instant.parse("2026-01-01T09:00:00Z"))

        assertNotNull(next)
        assertEquals(Instant.parse("2026-01-01T16:00:00Z"), next.scheduledAt)
    }

    @Test
    fun sixHourEstrogenNextDose() {
        val service = serviceWith(planWith(estrogen(intervalMinutes = 6 * 60)))

        val next = service.nextDose(Instant.parse("2026-01-01T09:00:00Z"))

        assertNotNull(next)
        assertEquals(Instant.parse("2026-01-01T14:00:00Z"), next.scheduledAt)
    }

    @Test
    fun blocker48HourAndEstrogen12HourReturnsEarlierAcrossBoth() {
        val service = serviceWith(planWith(
            estrogen(intervalMinutes = 12 * 60),
            blocker(anchorAt = Instant.parse("2026-01-01T07:00:00Z"), intervalMinutes = 48 * 60),
        ))

        val next = service.nextDose(Instant.parse("2026-01-01T09:00:00Z"))

        assertNotNull(next)
        assertEquals("estrogen", next.medicationId)
        assertEquals(Instant.parse("2026-01-01T20:00:00Z"), next.scheduledAt)
    }

    @Test
    fun estrogenOnlyPlanWorksForNextDoseTakenCountAndReminders() {
        val eventRepo = InMemoryDoseEventRepository()
        eventRepo.save(DoseEvent(
            id = "taken-1",
            medicationId = "estrogen",
            scheduledAt = base,
            status = DoseStatus.TAKEN,
            takenAt = Instant.parse("2026-01-01T08:03:00Z"),
        ))
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60)), eventRepo)

        assertEquals(1, service.takenCount())
        assertEquals(1, service.takenCount("estrogen"))
        assertEquals(Instant.parse("2026-01-01T20:00:00Z"), service.nextDose(Instant.parse("2026-01-01T09:00:00Z"))?.scheduledAt)
        val reminders = service.buildReminders(
            now = Instant.parse("2026-01-01T09:00:00Z"),
            horizonEnd = Instant.parse("2026-01-02T09:00:00Z"),
        )
        assertEquals(listOf(
            Instant.parse("2026-01-01T20:00:00Z"),
            Instant.parse("2026-01-02T08:00:00Z"),
        ), reminders.map { it.fireAt })
    }

    @Test
    fun quarterTabletDisplayFormattingIncludesFractionAmountAndIngredient() {
        val medication = estrogen(
            physicalDose = PhysicalDose(
                form = DoseForm.TABLET,
                quantityDecimal = "0.25",
                quantityUnit = "tablet",
                fractionLabel = "1/4",
                productUnitStrengthLabel = "2 mg tablet",
            ),
            ingredientAmount = "0.5",
        )

        val display = medication.formatDoseDisplay()

        assertTrue(display.contains("1/4"), display)
        assertTrue(display.contains("0.5 mg"), display)
        assertTrue(display.contains("Estradiol"), display)
    }

    @Test
    fun fixedAnchorLateTakenDoseDoesNotShiftFutureOccurrence() {
        val eventRepo = InMemoryDoseEventRepository()
        eventRepo.save(DoseEvent(
            id = "late-dose",
            medicationId = "estrogen",
            scheduledAt = base,
            status = DoseStatus.TAKEN,
            takenAt = Instant.parse("2026-01-01T11:30:00Z"),
        ))
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60)), eventRepo)

        val next = service.nextDose(Instant.parse("2026-01-01T12:00:00Z"))

        assertNotNull(next)
        assertEquals(Instant.parse("2026-01-01T20:00:00Z"), next.scheduledAt)
    }

    @Test
    fun reminderHorizonOnlyEmitsRemindersInsideHorizon() {
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60)))

        val reminders = service.buildReminders(
            now = Instant.parse("2026-01-01T09:00:00Z"),
            horizonEnd = Instant.parse("2026-01-01T21:00:00Z"),
        )

        assertEquals(listOf(Instant.parse("2026-01-01T20:00:00Z")), reminders.map { it.fireAt })
    }

    @Test
    fun markTakenPersistsATakenDoseEventViaFakeRepo() {
        val eventRepo = InMemoryDoseEventRepository()
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60)), eventRepo, FixedClock(Instant.parse("2026-01-01T20:05:00Z")))

        val event = service.markTaken(medicationId = "estrogen", scheduledAt = Instant.parse("2026-01-01T20:00:00Z"), note = "after dinner")

        val persisted = eventRepo.events().single()
        assertEquals(event, persisted)
        assertEquals(DoseStatus.TAKEN, persisted.status)
        assertEquals(Instant.parse("2026-01-01T20:05:00Z"), persisted.takenAt)
        assertEquals("after dinner", persisted.note)
    }

    private fun serviceWith(
        plan: TreatmentPlan,
        eventRepository: InMemoryDoseEventRepository = InMemoryDoseEventRepository(),
        clock: Clock = FixedClock(base),
    ) = TreatmentService(InMemoryTreatmentRepository(plan), eventRepository, clock)

    private fun planWith(vararg medications: Medication) = TreatmentPlan(
        id = "plan-1",
        startedAt = base,
        timezone = "UTC",
        medications = medications.toList(),
    )

    private fun estrogen(
        intervalMinutes: Long = 12 * 60,
        anchorAt: Instant = base,
        physicalDose: PhysicalDose? = null,
        ingredientAmount: String = "2",
    ) = Medication(
        id = "estrogen",
        category = MedicationCategory.ESTROGEN,
        displayName = "Estradiol",
        productName = "Estradiol tablets",
        activeIngredients = listOf(ActiveIngredientDose("estradiol", "Estradiol", ingredientAmount, "mg")),
        physicalDose = physicalDose,
        route = AdministrationRoute.ORAL,
        schedule = Schedule.Interval(anchorAt = anchorAt, intervalMinutes = intervalMinutes),
        remindersEnabled = true,
    )

    private fun blocker(
        anchorAt: Instant,
        intervalMinutes: Long,
    ) = Medication(
        id = "blocker",
        category = MedicationCategory.TESTOSTERONE_BLOCKER,
        displayName = "Cyproterone",
        activeIngredients = listOf(ActiveIngredientDose("cyproterone", "Cyproterone acetate", "12.5", "mg")),
        route = AdministrationRoute.ORAL,
        schedule = Schedule.Interval(anchorAt = anchorAt, intervalMinutes = intervalMinutes),
        remindersEnabled = true,
    )
}
