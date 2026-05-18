package app.transition.hrt.core

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Core treatment plan owned by the shared SDK. */
data class TreatmentPlan(
    val id: String,
    val startedAt: Instant,
    val timezone: String,
    val medications: List<Medication>,
)

data class Medication(
    val id: String,
    val treatmentId: String? = null,
    val category: MedicationCategory,
    val displayName: String,
    val productName: String? = null,
    val activeIngredients: List<ActiveIngredientDose>,
    val physicalDose: PhysicalDose? = null,
    val route: AdministrationRoute,
    val schedule: Schedule,
    val remindersEnabled: Boolean,
    val notes: String? = null,
)

enum class MedicationCategory {
    ESTROGEN,
    TESTOSTERONE_BLOCKER,
    PROGESTOGEN,
    OTHER,
}

data class ActiveIngredientDose(
    val ingredientId: String,
    val displayName: String,
    /** Decimal-safe value represented as text; callers decide precision rules. */
    val amount: String,
    val unit: String,
)

data class PhysicalDose(
    val form: DoseForm,
    val quantityDecimal: String,
    val quantityUnit: String,
    val fractionLabel: String? = null,
    val productUnitStrengthLabel: String? = null,
)

enum class DoseForm {
    TABLET,
    CAPSULE,
    PATCH,
    GEL,
    INJECTION,
    OTHER,
}

enum class AdministrationRoute {
    ORAL,
    SUBLINGUAL,
    TRANSDERMAL,
    INJECTION,
    OTHER,
}

sealed class Schedule {
    data class Interval(
        val anchorAt: Instant,
        val intervalMinutes: Long,
        val driftPolicy: DriftPolicy = DriftPolicy.FIXED_ANCHOR,
    ) : Schedule() {
        init {
            require(intervalMinutes > 0) { "intervalMinutes must be positive" }
        }
    }
}

enum class DriftPolicy {
    FIXED_ANCHOR,
}

data class DoseEvent(
    val id: String,
    val medicationId: String,
    val scheduledAt: Instant,
    val status: DoseStatus,
    val takenAt: Instant? = null,
    val note: String? = null,
)

enum class DoseStatus {
    PENDING,
    TAKEN,
    SKIPPED,
    MISSED,
}

data class ScheduledDose(
    val medicationId: String,
    val scheduledAt: Instant,
    val medication: Medication,
)

data class ReminderRequest(
    val id: String,
    val medicationId: String,
    val scheduledDoseAt: Instant,
    val fireAt: Instant,
    val title: String,
    val body: String,
)

data class TreatmentSummary(
    val treatmentPlan: TreatmentPlan?,
    val nextDose: ScheduledDose?,
    val takenCount: Int,
)

interface TreatmentRepository {
    fun currentTreatmentPlan(): TreatmentPlan?
    fun save(plan: TreatmentPlan)
}

interface DoseEventRepository {
    fun events(): List<DoseEvent>
    fun eventsForMedication(medicationId: String): List<DoseEvent> = events().filter { it.medicationId == medicationId }
    fun save(event: DoseEvent)
}

interface MedicationRepository
interface SettingsRepository

interface Clock {
    fun now(): Instant
}

class FixedClock(private val fixedNow: Instant) : Clock {
    override fun now(): Instant = fixedNow
}

object ScheduleCalculator {
    fun occurrences(schedule: Schedule, from: Instant, to: Instant): List<Instant> = when (schedule) {
        is Schedule.Interval -> intervalOccurrences(schedule, from, to)
    }

    fun nextOccurrence(schedule: Schedule, now: Instant): Instant = when (schedule) {
        is Schedule.Interval -> nextIntervalOccurrence(schedule, now)
    }

    private fun intervalOccurrences(schedule: Schedule.Interval, from: Instant, to: Instant): List<Instant> {
        if (to < from) return emptyList()
        val first = firstIntervalOccurrenceAtOrAfter(schedule, from)
        val occurrences = mutableListOf<Instant>()
        var cursor = first
        while (cursor <= to) {
            occurrences += cursor
            cursor = cursor.plus(schedule.intervalMinutes.minutes)
        }
        return occurrences
    }

    private fun nextIntervalOccurrence(schedule: Schedule.Interval, now: Instant): Instant {
        val firstAtOrAfter = firstIntervalOccurrenceAtOrAfter(schedule, now)
        return if (firstAtOrAfter <= now) firstAtOrAfter.plus(schedule.intervalMinutes.minutes) else firstAtOrAfter
    }

    private fun firstIntervalOccurrenceAtOrAfter(schedule: Schedule.Interval, instant: Instant): Instant {
        if (instant <= schedule.anchorAt) return schedule.anchorAt
        var cursor = schedule.anchorAt
        while (cursor < instant) {
            cursor = cursor.plus(schedule.intervalMinutes.minutes)
        }
        return cursor
    }
}

class TreatmentService(
    private val treatmentRepository: TreatmentRepository,
    private val doseEventRepository: DoseEventRepository,
    private val clock: Clock,
) {
    fun summary(now: Instant = clock.now()): TreatmentSummary = TreatmentSummary(
        treatmentPlan = treatmentRepository.currentTreatmentPlan(),
        nextDose = nextDose(now),
        takenCount = takenCount(),
    )

    fun nextDose(now: Instant = clock.now()): ScheduledDose? {
        val plan = treatmentRepository.currentTreatmentPlan() ?: return null
        val completed = doseEventRepository.events()
            .filter { it.status == DoseStatus.TAKEN || it.status == DoseStatus.SKIPPED || it.status == DoseStatus.MISSED }
            .map { it.medicationId to it.scheduledAt }
            .toSet()

        return plan.medications
            .asSequence()
            .map { medication -> firstOpenDoseAfter(medication, now, completed) }
            .filterNotNull()
            .minByOrNull { it.scheduledAt }
    }

    fun markTaken(medicationId: String, scheduledAt: Instant, note: String? = null): DoseEvent {
        val event = DoseEvent(
            id = "taken-$medicationId-$scheduledAt",
            medicationId = medicationId,
            scheduledAt = scheduledAt,
            status = DoseStatus.TAKEN,
            takenAt = clock.now(),
            note = note,
        )
        doseEventRepository.save(event)
        return event
    }

    fun takenCount(medicationId: String? = null): Int = doseEventRepository.events()
        .count { event -> event.status == DoseStatus.TAKEN && (medicationId == null || event.medicationId == medicationId) }

    fun buildReminders(now: Instant = clock.now(), horizonEnd: Instant): List<ReminderRequest> {
        val plan = treatmentRepository.currentTreatmentPlan() ?: return emptyList()
        if (horizonEnd <= now) return emptyList()
        val completed = doseEventRepository.events()
            .filter { it.status == DoseStatus.TAKEN || it.status == DoseStatus.SKIPPED || it.status == DoseStatus.MISSED }
            .map { it.medicationId to it.scheduledAt }
            .toSet()

        return plan.medications
            .filter { it.remindersEnabled }
            .flatMap { medication ->
                ScheduleCalculator.occurrences(medication.schedule, now, horizonEnd)
                    .filter { scheduledAt -> scheduledAt > now }
                    .filter { scheduledAt -> (medication.id to scheduledAt) !in completed }
                    .map { scheduledAt ->
                        ReminderRequest(
                            id = "reminder-${medication.id}-$scheduledAt",
                            medicationId = medication.id,
                            scheduledDoseAt = scheduledAt,
                            fireAt = scheduledAt,
                            title = "Time for ${medication.displayName}",
                            body = medication.formatDoseDisplay(),
                        )
                    }
            }
            .sortedBy { it.fireAt }
    }

    private fun firstOpenDoseAfter(
        medication: Medication,
        now: Instant,
        completed: Set<Pair<String, Instant>>,
    ): ScheduledDose {
        var scheduledAt = ScheduleCalculator.nextOccurrence(medication.schedule, now)
        while ((medication.id to scheduledAt) in completed) {
            val schedule = medication.schedule
            scheduledAt = when (schedule) {
                is Schedule.Interval -> scheduledAt.plus(schedule.intervalMinutes.minutes)
            }
        }
        return ScheduledDose(medication.id, scheduledAt, medication)
    }
}

fun Medication.formatDoseDisplay(): String {
    val doseText = physicalDose?.let { dose ->
        buildString {
            if (!dose.fractionLabel.isNullOrBlank()) {
                append(dose.fractionLabel)
            } else {
                append(dose.quantityDecimal)
            }
            append(' ')
            append(dose.quantityUnit)
            if (!dose.productUnitStrengthLabel.isNullOrBlank()) {
                append(" (")
                append(dose.productUnitStrengthLabel)
                append(')')
            }
        }
    } ?: displayName

    val ingredientText = activeIngredients.joinToString(", ") { ingredient ->
        "${ingredient.displayName} ${ingredient.amount} ${ingredient.unit}"
    }

    return if (ingredientText.isBlank()) doseText else "$doseText - $ingredientText"
}
