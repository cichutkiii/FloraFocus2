package pl.preclaw.florafocus.data.model

import com.google.firebase.database.PropertyName

data class Plant(
    val id: String? = null,
    @get:PropertyName("commonName") val commonName: String = "",
    @get:PropertyName("careSteps") val careSteps: List<CareStep> = emptyList(),
    val edible: Boolean = false,
    val growth: String = "",
    val waterRequirement: String = "",
    val lightRequirement: String = "",
    val usdaHardinessZone: String = "",
    val soilType: String = "",
    val family: String = "",
    val edibleParts: List<String> = emptyList(),
    val sowingDate: DateRange = DateRange(),
    val pests: List<String> = emptyList(),
    val diseases: List<String> = emptyList(),
    val companions: List<String> = emptyList(),
    val incompatibles: List<String> = emptyList(),
    val weatherDependencies: Map<String, TemperatureRange> = emptyMap(),
    val growthPhaseTriggers: Map<String, String> = emptyMap()
) {
    // Konstruktor bezargumentowy wymagany przez Firebase
    constructor() : this(null)

    fun toMap(): Map<String, Any?> = mapOf(
        "commonName" to commonName,
        "edible" to edible,
        "growth" to growth,
        "waterRequirement" to waterRequirement,
        "lightRequirement" to lightRequirement,
        "usdaHardinessZone" to usdaHardinessZone,
        "soilType" to soilType,
        "family" to family,
        "edibleParts" to edibleParts,
        "careSteps" to careSteps.map { it.toMap() },
        "sowingDate" to sowingDate.toMap(),
        "pests" to pests,
        "diseases" to diseases,
        "companions" to companions,
        "incompatibles" to incompatibles,
        "weatherDependencies" to weatherDependencies.mapValues { it.value.toMap() },
        "growthPhaseTriggers" to growthPhaseTriggers
    )
}