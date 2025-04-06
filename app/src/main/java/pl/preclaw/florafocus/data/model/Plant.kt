package pl.preclaw.florafocus.data.model

import com.google.firebase.database.PropertyName

data class Plant(
    val id: String? = null,

    @get:PropertyName("commonName")
    @set:PropertyName("commonName")
    var commonName: String = "",

    @get:PropertyName("careSteps")
    @set:PropertyName("careSteps")
    var careSteps: List<CareStep> = emptyList(),

    @get:PropertyName("Edible")
    @set:PropertyName("Edible")
    var edible: Boolean = false,

    @get:PropertyName("Growth")
    @set:PropertyName("Growth")
    var growth: String = "",

    @get:PropertyName("Water requirement")
    @set:PropertyName("Water requirement")
    var waterRequirement: String = "",

    @get:PropertyName("Light requirement")
    @set:PropertyName("Light requirement")
    var lightRequirement: String = "",

    @get:PropertyName("USDA Hardiness zone")
    @set:PropertyName("USDA Hardiness zone")
    var usdaHardinessZone: String = "",

    @get:PropertyName("Soil type")
    @set:PropertyName("Soil type")
    var soilType: String = "",

    @get:PropertyName("Family")
    @set:PropertyName("Family")
    var family: String = "",

    @get:PropertyName("Edible parts")
    @set:PropertyName("Edible parts")
    var edibleParts: List<String> = emptyList(),

    var sowingDate: DateRange = DateRange(),
    var pests: List<String> = emptyList(),
    var diseases: List<String> = emptyList(),
    var companions: List<String> = emptyList(),
    var incompatibles: List<String> = emptyList(),
    var weatherDependencies: Map<String, TemperatureRange> = emptyMap(),
    var growthPhaseTriggers: Map<String, String> = emptyMap()
) {
    // Konstruktor bezargumentowy wymagany przez Firebase
    constructor() : this(null)

    fun toMap(): Map<String, Any?> = mapOf(
        "commonName" to commonName,
        "Edible" to edible,
        "Growth" to growth,
        "Water requirement" to waterRequirement,
        "Light requirement" to lightRequirement,
        "USDA Hardiness zone" to usdaHardinessZone,
        "Soil type" to soilType,
        "Family" to family,
        "Edible parts" to edibleParts,
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