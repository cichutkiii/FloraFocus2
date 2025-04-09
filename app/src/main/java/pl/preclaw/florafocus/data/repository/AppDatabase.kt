package pl.preclaw.florafocus.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserPlant::class,
        PlantLocationCrossRef::class,
        GardenSpaceEntity::class,
        GardenAreaEntity::class,
        PlantLocationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userPlantDao(): UserPlantDao
    abstract fun plantLocationDao(): PlantLocationDao
    abstract fun gardenSpaceDao(): GardenSpaceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flora_focus_db" // Nowa nazwa bazy danych, aby uniknąć konfliktów
                ).build().also { instance = it }
            }
        }
    }
}