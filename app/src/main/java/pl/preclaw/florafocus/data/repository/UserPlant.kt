package pl.preclaw.florafocus.data.repository

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.DateRange
import pl.preclaw.florafocus.data.model.TemperatureRange

@Entity(tableName = "user_plants")
data class UserPlant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val templateId: String,            // ID szablonu z Firebase
    val name: String,                  // Nazwa wyświetlana
    val variety: String = "",          // Odmiana
    val notes: String = "",            // Notatki
    val quantity: Int = 1,             // Ilość
    val plantingDate: String = "",     // Data posadzenia

    // Skopiowane dane z szablonu do szybkiego dostępu
    val edible: Boolean = false,
    val growth: String = "",
    val waterRequirement: String = "",
    val lightRequirement: String = "",
    val soilType: String = "",
    val family: String = "",

    // Ta relacja jest reprezentowana przez PlantLocationCrossRef
    // Lista lokalizacji NIE jest przechowywana bezpośrednio w encji
)

data class PlantTemplate(
    val id: String,
    val commonName: String,
    val careSteps: List<CareStep>,
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
)
data class UserPlantWithLocations(
    @Embedded val plant: UserPlant,
    @Relation(
        entity = PlantLocationEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlantLocationCrossRef::class,
            parentColumn = "plantId",
            entityColumn = "locationId"
        )
    )
    val locations: List<PlantLocationEntity>
)

// Lokalizacja wraz z roślinami w niej
data class LocationWithPlants(
    @Embedded val location: PlantLocationEntity,
    @Relation(
        entity = UserPlant::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlantLocationCrossRef::class,
            parentColumn = "locationId",
            entityColumn = "plantId"
        )
    )
    val plants: List<UserPlant>
)

// Szczegóły powiązania między rośliną a lokalizacją
data class PlantLocationDetails(
    @Embedded val crossRef: PlantLocationCrossRef,
    @Relation(
        parentColumn = "plantId",
        entityColumn = "id"
    )
    val plant: UserPlant,
    @Relation(
        parentColumn = "locationId",
        entityColumn = "id"
    )
    val location: PlantLocationEntity
)

// Kompletne informacje o roślinie (dane podstawowe + szablon + lokalizacje)
data class CompletePlantInfo(
    @Embedded val userPlant: UserPlant,
    // Dane templateId mogą być używane do pobrania dodatkowych informacji z Firebase
    @Relation(
        entity = PlantLocationEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlantLocationCrossRef::class,
            parentColumn = "plantId",
            entityColumn = "locationId"
        )
    )
    val locations: List<PlantLocationWithDetails>
)

data class PlantLocationWithDetails(
    @Embedded val location: PlantLocationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "locationId"
    )
    val crossRef: PlantLocationCrossRef
)