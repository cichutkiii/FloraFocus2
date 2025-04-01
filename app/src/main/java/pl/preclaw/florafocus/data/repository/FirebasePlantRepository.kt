package pl.preclaw.florafocus.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import pl.preclaw.florafocus.data.model.Plant

class FirebasePlantRepository {
    private val database = FirebaseDatabase.getInstance()
    private val plantsRef = database.getReference("plants")

    fun getAllPlants(): Flow<List<Plant>> = flow {
        val snapshot = plantsRef.get().await()
        val plants = snapshot.children.mapNotNull {
            val plant = it.getValue<Plant>()
            // Przypisz klucz węzła do pola id
            plant?.copy(id = it.key)
        }
        emit(plants)
    }
}