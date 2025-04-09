package pl.preclaw.florafocus.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.DateRange
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.model.TemperatureRange

class FirebasePlantRepository {
    private val database = FirebaseDatabase.getInstance()
    private val plantsRef = database.getReference("plants")

    /**
     * Pobiera wszystkie rośliny z Firebase
     */
    fun getAllPlants(): Flow<List<Plant>> = flow {
        try {
            val snapshot = plantsRef.get().await()
            val plants = snapshot.children.mapNotNull { plantSnapshot ->
                try {
                    val plantId = plantSnapshot.key
                    val plant = plantSnapshot.getValue(Plant::class.java)

                    if (plant != null) {
                        plant.copy(id = plantId)
                    } else {
                        // Alternatywna metoda mapowania, jeśli automatyczna nie działa
                        val plantMap = plantSnapshot.getValue<Map<String, Any>>()
                        if (plantMap != null) {
                            // Manualne mapowanie
                            // ...
                        } else null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            emit(plants)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Pobiera określoną roślinę z Firebase po ID
     */
    fun getPlantById(id: String): Flow<Plant?> = flow {
        try {
            val snapshot = plantsRef.child(id).get().await()
            val plant = snapshot.getValue(Plant::class.java)?.copy(id = id)
            emit(plant)
        } catch (e: Exception) {
            emit(null)
        }
    }
}

// Rozszerzenie do konwersji ze starego UserPlant do nowego UserPlant
fun pl.preclaw.florafocus.data.repository.UserPlant.toNewModel(): UserPlant {
    return UserPlant(
        id = this.id,
        templateId = this.plantId,
        name = this.name,
        variety = this.variety,
        notes = this.notes,
        quantity = this.quantity,
        plantingDate = this.plantingDate,
        edible = this.edible,
        growth = this.growth,
        waterRequirement = this.waterRequirement,
        lightRequirement = this.lightRequirement,
        soilType = this.soilType,
        family = this.family
    )
}

// Rozszerzenie do konwersji ze starego PlantPlacementEntity do nowego PlantLocationCrossRef
fun pl.preclaw.florafocus.data.repository.PlantPlacementEntity.toNewCrossRef(plantId: Int): PlantLocationCrossRef {
    return PlantLocationCrossRef(
        plantId = plantId,
        locationId = this.locationId,
        addedDate = this.plantingDate,
        notes = this.notes
    )
}