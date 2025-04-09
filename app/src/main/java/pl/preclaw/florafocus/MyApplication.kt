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
    val plantRepository: PlantRepository by lazy {
        PlantRepository(
            database = AppDatabase.getInstance(this),
            firebaseRepo = FirebasePlantRepository()
        )
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "flora-db"
        )
        // Dodanie migracji
            .fallbackToDestructiveMigration() // Dodaj tę linię

            .build()

        firebaseRepo = FirebasePlantRepository()

    }




}