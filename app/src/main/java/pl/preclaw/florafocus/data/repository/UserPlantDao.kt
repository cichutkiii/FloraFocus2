package pl.preclaw.florafocus.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.preclaw.florafocus.data.repository.UserPlant

@Dao
interface UserPlantDao {
    @Query("SELECT * FROM userplant")
    fun getAll(): Flow<List<UserPlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: UserPlant)

    @Delete
    suspend fun delete(plant: UserPlant)

    @Query("DELETE FROM userplant WHERE id = :plantId")
    suspend fun deleteById(plantId: Int)}