package pl.preclaw.florafocus.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [
    UserPlant::class,
    GardenSpaceEntity::class,
    GardenAreaEntity::class,
    PlantLocationEntity::class,
    PlantPlacementEntity::class
], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userPlantDao(): UserPlantDao
    abstract fun gardenSpaceDao(): GardenSpaceDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "flora-db"
                )
                    .addMigrations(MIGRATION_1_2) // Dodanie migracji
                    .build().also { instance = it }
            }
        }
    }
}