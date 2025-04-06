package pl.preclaw.florafocus

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.preclaw.florafocus.data.repository.AppDatabase
import pl.preclaw.florafocus.data.repository.FirebasePlantRepository
import pl.preclaw.florafocus.data.repository.*
import pl.preclaw.florafocus.data.repository.UserPlant

class MyApplication : Application() {
    private lateinit var database: AppDatabase
    private lateinit var firebaseRepo: FirebasePlantRepository

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "flora-db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3,MIGRATION_3_4, MIGRATION_4_5,MIGRATION_5_6)
        // Dodanie migracji

            .build()

        firebaseRepo = FirebasePlantRepository()

        if (isMigrationNeeded(this)) {
            migrateDataFromFirebase()
        }
    }

    private fun migrateDataFromFirebase() {
        val dao = database.userPlantDao()

        CoroutineScope(Dispatchers.IO).launch {
            firebaseRepo.getAllPlants().collect { firebasePlants ->
                firebasePlants.forEach { plant ->
                    val userPlant = UserPlant(
                        name = plant.commonName,
                        careSteps = plant.careSteps
                    )
                    dao.insert(userPlant)
                }
            }
            markMigrationComplete(this@MyApplication)
        }
    }

    private fun isMigrationNeeded(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return !prefs.getBoolean("migration_done", false)
    }

    private fun markMigrationComplete(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("migration_done", true).apply()
    }
}