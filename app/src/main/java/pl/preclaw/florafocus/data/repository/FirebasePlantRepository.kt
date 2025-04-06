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

    fun getAllPlants(): Flow<List<Plant>> = flow {
        try {
            println("Rozpoczęto pobieranie roślin z Firebase")
            val snapshot = plantsRef.get().await()
            println("Pobrano dane z Firebase: ${snapshot.childrenCount} elementów")

            val plants = snapshot.children.mapNotNull { plantSnapshot ->
                val plantId = plantSnapshot.key

                try {
                    // Najpierw próbujemy automatycznego mapowania
                    val plant = plantSnapshot.getValue(Plant::class.java)

                    // Jeśli udało się pobrać obiekt Plant, zwróć go z ustawionym ID
                    if (plant != null) {
                        println("Poprawnie pobrano roślinę automatycznie: $plantId - ${plant.commonName} - jadalna: ${plant.edible}")
                        plant.copy(id = plantId)
                    } else {
                        // Alternatywne podejście - ręczne mapowanie
                        println("Próba ręcznego mapowania dla $plantId")
                        val plantValues = plantSnapshot.getValue<Map<String, Any>>()

                        if (plantValues != null) {
                            println("Pobrane wartości dla $plantId: ${plantValues.keys.joinToString()}")

                            // Debug edible field
                            val edibleValue = plantValues["Edible"]
                            println("Edible wartość oryginalna: $edibleValue (type: ${edibleValue?.javaClass?.name ?: "null"})")

                            // Konwersja wartości Edible - obsługujemy bool, int, string
                            val isEdible = when (edibleValue) {
                                is Boolean -> edibleValue
                                is Int -> edibleValue == 1
                                is Long -> edibleValue == 1L
                                is String -> edibleValue.equals("true", ignoreCase = true)
                                else -> false
                            }
                            println("Edible po konwersji: $isEdible")

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

                            // Pobierz części jadalne
                            val edibleParts = (plantValues["Edible parts"] as? List<String>) ?: emptyList()

                            // Debug water/light requirement
                            println("Water requirement: ${plantValues["Water requirement"]}")
                            println("Light requirement: ${plantValues["Light requirement"]}")

                            // Pobierz powiązane listy
                            val pests = (plantValues["pests"] as? List<String>) ?: emptyList()
                            val diseases = (plantValues["diseases"] as? List<String>) ?: emptyList()
                            val companions = (plantValues["companions"] as? List<String>) ?: emptyList()
                            val incompatibles = (plantValues["incompatibles"] as? List<String>) ?: emptyList()

                            // Pobierz datę siewu
                            val sowingDateMap = plantValues["sowingDate"] as? Map<String, String>
                            val sowingDate = if (sowingDateMap != null) {
                                DateRange(
                                    start = sowingDateMap["start"] ?: "",
                                    end = sowingDateMap["end"] ?: ""
                                )
                            } else {
                                DateRange()
                            }

                            // Pobierz zależności pogodowe
                            val weatherDepsMap = plantValues["weatherDependencies"] as? Map<String, Map<String, Double>> ?: emptyMap()
                            val weatherDependencies = weatherDepsMap.mapValues { (_, value) ->
                                TemperatureRange(
                                    min = value["min"] ?: 0.0,
                                    max = value["max"] ?: 0.0
                                )
                            }

                            // Pobierz triggery faz wzrostu
                            val growthTriggersMap = plantValues["growthPhaseTriggers"] as? Map<String, String> ?: emptyMap()

                            // Utworzenie obiektu Plant z odczytanych danych
                            val createdPlant = Plant(
                                id = plantId,
                                commonName = (plantValues["commonName"] as? String)
                                    ?: plantId ?: "", // Użyj ID jako nazwy, jeśli brak commonName
                                careSteps = careSteps,
                                edible = isEdible,
                                growth = (plantValues["Growth"] as? String) ?: "",
                                waterRequirement = (plantValues["Water requirement"] as? String) ?: "",
                                lightRequirement = (plantValues["Light requirement"] as? String) ?: "",
                                usdaHardinessZone = (plantValues["USDA Hardiness zone"] as? String) ?: "",
                                soilType = (plantValues["Soil type"] as? String) ?: "",
                                family = (plantValues["Family"] as? String) ?: "",
                                edibleParts = edibleParts,
                                sowingDate = sowingDate,
                                pests = pests,
                                diseases = diseases,
                                companions = companions,
                                incompatibles = incompatibles,
                                weatherDependencies = weatherDependencies,
                                growthPhaseTriggers = growthTriggersMap
                            )

                            println("Ręcznie zbudowana roślina $plantId - ${createdPlant.commonName} - jadalna: ${createdPlant.edible}")
                            println("Water: ${createdPlant.waterRequirement}, Light: ${createdPlant.lightRequirement}")

                            createdPlant
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