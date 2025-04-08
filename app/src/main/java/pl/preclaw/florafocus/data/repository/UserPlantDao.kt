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
    // Istniejące metody...
    @Query("SELECT * FROM userplant")
    fun getAll(): Flow<List<UserPlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: UserPlant): Long

    @Delete
    suspend fun delete(plant: UserPlant)

    @Query("DELETE FROM userplant WHERE id = :plantId")
    suspend fun deleteById(plantId: Int)

    // Nowe metody do edycji

    // Pobieranie rośliny po ID
    @Query("SELECT * FROM userplant WHERE id = :id")
    suspend fun getById(id: Int): UserPlant?

    // Aktualizacja rośliny
    @Update
    suspend fun update(plant: UserPlant)

    // Opcjonalnie, jeśli potrzebujemy pobrać rośliny w konkretnej lokalizacji
    @Query("SELECT * FROM userplant WHERE locationId = :locationId")
    fun getPlantsInLocation(locationId: String): Flow<List<UserPlant>>
    @Query("SELECT * FROM userplant WHERE plantId = :plantId AND locationId = :locationId LIMIT 1")
    suspend fun findByPlantIdAndLocation(plantId: String, locationId: String): UserPlant?

    // Nowa metoda - bezpieczne dodawanie rośliny do lokalizacji
    @Transaction
    suspend fun addPlantToLocation(plant: UserPlant) {
        // Sprawdź, czy roślina o takim ID i lokalizacji już istnieje
        val existingPlant = findByPlantIdAndLocation(plant.plantId, plant.locationId)

        if (existingPlant == null) {
            // Jeśli nie istnieje, dodaj ją
            insert(plant)
        } else {
            // Jeśli istnieje, zaktualizuj jej dane
            val updatedPlant = plant.copy(id = existingPlant.id)
            insert(updatedPlant) // REPLACE strategia zaktualizuje istniejący rekord
        }
    }
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