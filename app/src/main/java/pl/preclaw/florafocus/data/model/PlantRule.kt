package pl.preclaw.florafocus.data.model


data class PlantRule(
    val careSteps: List<CareStep>,
    val species: String,
    val weatherDependencies: Map<String, ClosedRange<Double>>,
    val growthPhaseTriggers: Map<Int, String>
)

