package pl.preclaw.florafocus.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPlantDao {
    @Query("SELECT * FROM userplant")
    fun getAll(): Flow<List<UserPlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: UserPlant)

    // Nowa metoda - sprawdza czy roślina o takim plantId i locationId już istnieje
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

    @Delete
    suspend fun delete(plant: UserPlant)

    @Query("DELETE FROM userplant WHERE id = :plantId")
    suspend fun deleteById(plantId: Int)
}