package pl.preclaw.florafocus.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.preclaw.florafocus.data.model.Plant

@Dao
interface UserPlantDao {
    // Podstawowe operacje CRUD
    @Query("SELECT * FROM user_plants")
    fun getAllPlants(): Flow<List<UserPlant>>

    @Query("SELECT * FROM user_plants WHERE id = :id")
    suspend fun getPlantById(id: Int): UserPlant?

    @Query("SELECT * FROM user_plants WHERE id = :id")
    fun getPlantByIdFlow(id: Int): Flow<UserPlant?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: UserPlant): Long

    @Update
    suspend fun updatePlant(plant: UserPlant)

    @Delete
    suspend fun deletePlant(plant: UserPlant)

    @Query("DELETE FROM user_plants WHERE id = :id")
    suspend fun deletePlantById(id: Int)

    // Złożone zapytania z relacjami
    @Transaction
    @Query("SELECT * FROM user_plants")
    fun getPlantsWithLocations(): Flow<List<UserPlantWithLocations>>

    @Transaction
    @Query("SELECT * FROM user_plants WHERE id = :id")
    fun getPlantWithLocations(id: Int): Flow<UserPlantWithLocations?>

    @Transaction
    @Query("""
    SELECT up.* FROM user_plants up
    INNER JOIN plant_location_crossref plc ON up.id = plc.plantId
    WHERE plc.locationId = :locationId
""")
    fun getPlantsInLocation(locationId: String): Flow<List<UserPlant>>

    @Transaction
    @Query("""
        SELECT up.* FROM user_plants up
        INNER JOIN plant_location_crossref plc ON up.id = plc.plantId
        WHERE plc.locationId = :locationId
    """)
    fun getPlantDetailsInLocation(locationId: String): Flow<List<UserPlantWithLocations>>
}

@Dao
interface PlantLocationDao {
    // Zarządzanie powiązaniami roślina-lokalizacja
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: PlantLocationCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: PlantLocationCrossRef)

    @Query("DELETE FROM plant_location_crossref WHERE plantId = :plantId AND locationId = :locationId")
    suspend fun deleteCrossRefByIds(plantId: Int, locationId: String)

    @Query("""
        SELECT * FROM plant_location_crossref
        WHERE plantId = :plantId
    """)
    fun getLocationsForPlant(plantId: Int): Flow<List<PlantLocationCrossRef>>

    @Query("""
        SELECT * FROM plant_location_crossref
        WHERE locationId = :locationId
    """)
    fun getPlantsForLocation(locationId: String): Flow<List<PlantLocationCrossRef>>

    // Szczegółowe zapytania
    @Transaction
    @Query("""
        SELECT * FROM plant_location_crossref
        WHERE plantId = :plantId AND locationId = :locationId
    """)
    fun getPlantLocationDetails(plantId: Int, locationId: String): Flow<PlantLocationDetails?>
}

@Dao
interface GardenSpaceDao {
    @Transaction
    @Query("SELECT * FROM garden_space")
    fun getAllSpacesWithAreas(): Flow<List<SpaceWithAreas>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpace(space: GardenSpaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArea(area: GardenAreaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: PlantLocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantPlacement(placement: PlantPlacementEntity)

    @Delete
    suspend fun deleteSpace(space: GardenSpaceEntity)

    @Delete
    suspend fun deleteArea(area: GardenAreaEntity)

    @Delete
    suspend fun deleteLocation(location: PlantLocationEntity)

    @Delete
    suspend fun deletePlantPlacement(placement: PlantPlacementEntity)

    @Query("SELECT * FROM garden_area WHERE parentId = :spaceId")
    fun getAreasForSpace(spaceId: String): Flow<List<GardenAreaEntity>>

    @Query("SELECT * FROM plant_location WHERE parentId = :areaId")
    fun getLocationsForArea(areaId: String): Flow<List<PlantLocationEntity>>

    @Query("SELECT * FROM plant_placement WHERE locationId = :locationId")
    fun getPlacementsForLocation(locationId: String): Flow<List<PlantPlacementEntity>>
}