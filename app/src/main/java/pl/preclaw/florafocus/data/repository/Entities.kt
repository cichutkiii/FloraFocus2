package pl.preclaw.florafocus.data.repository


import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.preclaw.florafocus.data.model.LocationType
import java.util.UUID

@Entity(tableName = "garden_space")
data class GardenSpaceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = ""
)

@Entity(tableName = "garden_area",
    foreignKeys = [
        ForeignKey(
            entity = GardenSpaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GardenAreaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val parentId: String  // ID głównej przestrzeni
)

@Entity(tableName = "plant_location",
    foreignKeys = [
        ForeignKey(
            entity = GardenAreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlantLocationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val parentId: String,  // ID podprzestrzeni
    val type: LocationType = LocationType.BED,
    val lightConditions: String = "",
    val soilType: String = "",
    val notes: String = ""
)

@Entity(tableName = "plant_placement",
    foreignKeys = [
        ForeignKey(
            entity = PlantLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlantPlacementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val plantId: String,   // ID rośliny
    val locationId: String, // ID elementu przestrzeni
    val plantingDate: String = "",
    val quantity: Int = 1,
    val notes: String = "",
    val variety: String = ""
)

// Klasy do relacji między encjami (dla zapytań z @Transaction i @Relation)
data class AreaWithLocations(
    @Embedded val area: GardenAreaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val locations: List<PlantLocationEntity>
)

data class SpaceWithAreas(
    @Embedded val space: GardenSpaceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId",
        entity = GardenAreaEntity::class
    )
    val areas: List<AreaWithLocations>
)

data class LocationWithPlants(
    @Embedded val location: PlantLocationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "locationId"
    )
    val plantPlacements: List<PlantPlacementEntity>
)

// DAO dla operacji na bazie danych
