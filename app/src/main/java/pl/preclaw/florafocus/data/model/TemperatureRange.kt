package pl.preclaw.florafocus.data.model

data class TemperatureRange(
    val min: Double = 0.0,
    val max: Double = 0.0
) {
    // Konstruktor bezargumentowy wymagany przez Firebase
    constructor() : this(0.0, 0.0)

    fun toMap(): Map<String, Any?> = mapOf(
        "min" to min,
        "max" to max
    )
}