package pl.preclaw.florafocus.data.model


data class CareStep(
    val task: String = "",
    val dateRange: DateRange = DateRange()
) {
    // Konstruktor bezargumentowy wymagany przez Firebase
    constructor() : this("", DateRange())

    // Metoda konwertująca obiekt na mapę dla Firebase
    fun toMap(): Map<String, Any?> = mapOf(
        "task" to task,
        "dateRange" to dateRange.toMap()
    )
}