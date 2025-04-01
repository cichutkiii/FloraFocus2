package pl.preclaw.florafocus.data.model



data class DateRange(
    val start: String = "",
    val end: String = ""
) {
    // Konstruktor bezargumentowy wymagany przez Firebase
    constructor() : this("", "")

    fun toMap(): Map<String, Any?> = mapOf(
        "start" to start,
        "end" to end
    )
}