package app.transition.hrt.core

class InMemoryTreatmentRepository(initialPlan: TreatmentPlan? = null) : TreatmentRepository {
    private var plan: TreatmentPlan? = initialPlan

    override fun currentTreatmentPlan(): TreatmentPlan? = plan

    override fun save(plan: TreatmentPlan) {
        this.plan = plan
    }
}

class InMemoryDoseEventRepository(initialEvents: List<DoseEvent> = emptyList()) : DoseEventRepository {
    private val storedEvents = initialEvents.toMutableList()

    override fun events(): List<DoseEvent> = storedEvents.toList()

    override fun save(event: DoseEvent) {
        storedEvents.removeAll { it.id == event.id }
        storedEvents += event
    }
}
