package pl.preclaw.florafocus.data.model

data class PlantUpdateDetails(
    val variety: String? = null,
    val quantity: Int? = null,
    val plantingDate: String? = null,
    val notes: String? = null,
    val waterRequirement: String? = null,
    val lightRequirement: String? = null,
    val soilType: String? = null,
    // Możliwość rozszerzenia o dodatkowe pola w przyszłości
    val customTasks: List<CareStep>? = null
)