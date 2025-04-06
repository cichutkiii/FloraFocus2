package pl.preclaw.florafocus.data.repository

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.DateRange

@Entity(tableName = "userplant")
data class UserPlant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: String = "", // ID rośliny w Firebase (np. "ziemniak")
    val name: String, // Nazwa wyświetlana (może być commonName lub id)
    val careSteps: List<CareStep>,
    val locationId: String = "", // ID lokalizacji

    // Dodatkowe pola z Plant
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
    val weatherDependencies: Map<String, pl.preclaw.florafocus.data.model.TemperatureRange> = emptyMap(),
    val growthPhaseTriggers: Map<String, String> = emptyMap(),

    // Nowe pola do przechowywania szczegółów konkretnego nasadzenia
    val plantingDate: String = "", // Data posadzenia w formacie "dd-MM-yyyy"
    val variety: String = "", // Odmiana rośliny
    val quantity: Int = 1, // Ilość roślin
    val notes: String = "", // Dodatkowe notatki
    val customTasks: List<CareStep> = emptyList() // Niestandardowe zadania pielęgnacyjne
)