package app.transition.hrt.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

    @Test
    fun farPastMinuteAnchorCalculatesNextOccurrenceOnGridAtOrAfterNow() {
        val schedule = Schedule.Interval(
            anchorAt = Instant.parse("1970-01-01T00:00:00Z"),
            intervalMinutes = 1,
        )

        val next = ScheduleCalculator.nextOccurrence(schedule, Instant.parse("2026-01-01T08:00:00Z"))

        assertEquals(Instant.parse("2026-01-01T08:00:00Z"), next)
    }

    @Test
    fun nextDoseIncludesDoseDueExactlyNow() {
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60)))

        val next = service.nextDose(base)

        assertNotNull(next)
        assertEquals(base, next.scheduledAt)
    }

    @Test
    fun buildRemindersIncludesDoseDueExactlyNowWithinHorizon() {
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60)))

        val reminders = service.buildReminders(
            now = base,
            horizonEnd = Instant.parse("2026-01-01T21:00:00Z"),
        )

        assertEquals(listOf(base, Instant.parse("2026-01-01T20:00:00Z")), reminders.map { it.fireAt })
    }

    @Test
    fun buildRemindersSuppressesMedicationWithRemindersDisabled() {
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60, remindersEnabled = false)))

        val reminders = service.buildReminders(
            now = base,
            horizonEnd = Instant.parse("2026-01-01T21:00:00Z"),
        )

        assertEquals(emptyList(), reminders)
    }

    @Test
    fun domainModelsRejectRepresentativeInvalidValues() {
        assertFailsWith<IllegalArgumentException> {
            TreatmentPlan(id = " ", startedAt = base, timezone = "UTC", medications = emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            TreatmentPlan(id = "plan", startedAt = base, timezone = " ", medications = emptyList())
        }
        assertFailsWith<IllegalArgumentException> { estrogen(id = "") }
        assertFailsWith<IllegalArgumentException> { estrogen(displayName = "") }
        assertFailsWith<IllegalArgumentException> { estrogen(activeIngredients = emptyList()) }
        assertFailsWith<IllegalArgumentException> {
            ActiveIngredientDose("estradiol", "Estradiol", " ", "mg")
        }
        assertFailsWith<IllegalArgumentException> {
            ActiveIngredientDose("estradiol", " ", "2", "mg")
        }
        assertFailsWith<IllegalArgumentException> {
            PhysicalDose(DoseForm.TABLET, quantityDecimal = " ", quantityUnit = "tablet")
        }
        assertFailsWith<IllegalArgumentException> {
            PhysicalDose(DoseForm.TABLET, quantityDecimal = "1", quantityUnit = " ")
        }
        assertFailsWith<IllegalArgumentException> {
            DoseEvent("event", "estrogen", base, DoseStatus.TAKEN, takenAt = null)
        }
        assertFailsWith<IllegalArgumentException> {
            DoseEvent(" ", "estrogen", base, DoseStatus.PENDING)
        }
        assertFailsWith<IllegalArgumentException> {
            DoseEvent("event", " ", base, DoseStatus.PENDING)
        }
    }

    @Test
    fun markTakenRequiresCurrentPlan() {
        val service = TreatmentService(InMemoryTreatmentRepository(null), InMemoryDoseEventRepository(), FixedClock(base))

        assertFailsWith<IllegalStateException> {
            service.markTaken(medicationId = "estrogen", scheduledAt = base)
        }
    }

    @Test
    fun markTakenRequiresMedicationInCurrentPlan() {
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60)))

        assertFailsWith<IllegalArgumentException> {
            service.markTaken(medicationId = "unknown", scheduledAt = base)
        }
    }

    @Test
    fun markTakenRequiresScheduledAtOnMedicationScheduleGrid() {
        val service = serviceWith(planWith(estrogen(intervalMinutes = 12 * 60)))

        assertFailsWith<IllegalArgumentException> {
            service.markTaken(medicationId = "estrogen", scheduledAt = Instant.parse("2026-01-01T09:00:00Z"))
        }
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
        id: String = "estrogen",
        displayName: String = "Estradiol",
        activeIngredients: List<ActiveIngredientDose> = listOf(ActiveIngredientDose("estradiol", "Estradiol", ingredientAmount, "mg")),
        remindersEnabled: Boolean = true,
    ) = Medication(
        id = id,
        category = MedicationCategory.ESTROGEN,
        displayName = displayName,
        productName = "Estradiol tablets",
        activeIngredients = activeIngredients,
        physicalDose = physicalDose,
        route = AdministrationRoute.ORAL,
        schedule = Schedule.Interval(anchorAt = anchorAt, intervalMinutes = intervalMinutes),
        remindersEnabled = remindersEnabled,
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
