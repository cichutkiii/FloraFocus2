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


class FirebasePlantRepository {
    private val database = FirebaseDatabase.getInstance()
    private val plantsRef = database.getReference("plants")

    fun getAllPlants(): Flow<List<Plant>> = flow {
        try {
            println("Rozpoczęto pobieranie roślin z Firebase")
            val snapshot = plantsRef.get().await()
            println("Pobrano dane z Firebase: ${snapshot.childrenCount} elementów")

            val plants = snapshot.children.mapNotNull { plantSnapshot ->
                val plantId = plantSnapshot.key

                try {
                    // Pobierz wartości jako mapę
                    val plant = plantSnapshot.getValue(Plant::class.java)

                    // Jeśli udało się pobrać obiekt Plant, zwróć go z ustawionym ID
                    if (plant != null) {
                        println("Poprawnie pobrano roślinę: $plantId - ${plant.commonName}")
                        plant.copy(id = plantId)
                    } else {
                        // Alternatywne podejście - ręczne mapowanie
                        println("Próba ręcznego mapowania dla $plantId")
                        val plantValues = plantSnapshot.getValue<Map<String, Any>>()

                        if (plantValues != null) {
                            // Pobierz kroki pielęgnacyjne
                            val careStepsData = plantValues["careSteps"] as? List<Map<String, Any>> ?: emptyList()
                            val careSteps = careStepsData.map { stepMap ->
                                val task = stepMap["task"] as? String ?: ""
                                val dateRangeMap = stepMap["dateRange"] as? Map<String, String>
                                val dateRange = if (dateRangeMap != null) {
                                    DateRange(
                                        start = dateRangeMap["start"] ?: "",
                                        end = dateRangeMap["end"] ?: ""
                                    )
                                } else {
                                    DateRange()
                                }
                                CareStep(task = task, dateRange = dateRange)
                            }

                            // Utworzenie obiektu Plant z odczytanych danych
                            Plant(
                                id = plantId,
                                commonName = (plantValues["commonName"] as? String)
                                    ?: plantId ?: "", // Użyj ID jako nazwy, jeśli brak commonName
                                careSteps = careSteps,
                                edible = (plantValues["Edible"] as? Boolean) ?: false,
                                growth = (plantValues["Growth"] as? String) ?: "",
                                waterRequirement = (plantValues["Water requirement"] as? String) ?: "",
                                lightRequirement = (plantValues["Light requirement"] as? String) ?: "",
                                usdaHardinessZone = (plantValues["USDA Hardiness zone"] as? String) ?: "",
                                soilType = (plantValues["Soil type"] as? String) ?: "",
                                family = (plantValues["Family"] as? String) ?: ""
                            )
                        } else {
                            println("Nie udało się pobrać danych dla $plantId")
                            null
                        }
                    }
                } catch (e: Exception) {
                    println("Błąd przy przetwarzaniu rośliny $plantId: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }

            println("Pobrano ${plants.size} roślin z Firebase")
            emit(plants)
        } catch (e: Exception) {
            println("Globalny błąd przy pobieraniu roślin: ${e.message}")
            e.printStackTrace()
            emit(emptyList<Plant>())
        }
    }
}