package pl.preclaw.florafocus.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.model.PlantUpdateDetails

/**
 * Repozytorium do zarządzania roślinami, łączące dane z Firebase i lokalnej bazy Room.
 */
class PlantRepository(
    private val database: AppDatabase,
    private val firebaseRepo: FirebasePlantRepository
) {
    // Pobieranie wszystkich dostępnych roślin z Firebase
    fun getAllAvailablePlants(): Flow<List<Plant>> {
        return firebaseRepo.getAllPlants()
    }

    // Pobieranie roślin użytkownika
    fun getUserPlants(): Flow<List<UserPlant>> {
        return database.userPlantDao().getAll()
    }

    // Dodawanie rośliny
    suspend fun addUserPlant(
        plant: Plant,
        locationId: String? = null,
        variety: String = "",
        quantity: Int = 1,
        notes: String = "",
        plantingDate: String = ""
    ): Long {
        // Konwersja z Plant (Firebase) do UserPlant (Room)
        val userPlant = UserPlant(
            plantId = plant.id ?: "",
            name = plant.commonName.ifEmpty { plant.id ?: "Nieznana roślina" },
            locationId = locationId ?: "",
            // Wszystkie pozostałe istotne informacje z rośliny
            edible = plant.edible,
            growth = plant.growth,
            waterRequirement = plant.waterRequirement,
            lightRequirement = plant.lightRequirement,
            usdaHardinessZone = plant.usdaHardinessZone,
            soilType = plant.soilType,
            family = plant.family,
            careSteps = plant.careSteps,
            edibleParts = plant.edibleParts,
            sowingDate = plant.sowingDate,
            pests = plant.pests,
            diseases = plant.diseases,
            companions = plant.companions,
            incompatibles = plant.incompatibles,
            weatherDependencies = plant.weatherDependencies,
            growthPhaseTriggers = plant.growthPhaseTriggers,
            // Dane wprowadzone przez użytkownika
            variety = variety,
            quantity = quantity,
            plantingDate = plantingDate,
            notes = notes
        )

        // Zapisanie w Room i pobranie ID
        val id = database.userPlantDao().insert(userPlant)

        // Jeśli podano lokalizację, dodaj również powiązanie
        if (locationId != null) {
            val placement = PlantPlacementEntity(
                plantId = plant.id ?: "",
                locationId = locationId,
                variety = variety,
                quantity = quantity,
                plantingDate = plantingDate,
                notes = notes
            )
            database.gardenSpaceDao().insertPlantPlacement(placement)
        }

        return id
    }

    // Usuwanie rośliny
    suspend fun removeUserPlant(userPlantId: Int) {
        val plant = database.userPlantDao().getById(userPlantId)

        // Jeśli roślina ma lokalizację, usuń powiązanie
        if (plant?.locationId?.isNotEmpty() == true) {
            // Znajdź i usuń powiązanie
            val placements = database.gardenSpaceDao()
                .getPlacementsForLocation(plant.locationId).first()

            val placementToDelete = placements.find {
                it.plantId == plant.plantId || it.plantId == plant.name
            }

            placementToDelete?.let {
                database.gardenSpaceDao().deletePlantPlacement(it)
            }
        }

        // Usuń roślinę użytkownika
        database.userPlantDao().deleteById(userPlantId)
    }

    // NOWA METODA: Aktualizacja rośliny
    /**
     * Aktualizuje informacje o roślinie użytkownika.
     * Jeśli roślina jest powiązana z lokalizacją, aktualizuje również to powiązanie.
     */
    suspend fun updateUserPlant(userPlantId: Int, updates: PlantUpdateDetails) {
        // Pobierz aktualną roślinę
        val currentPlant = database.userPlantDao().getById(userPlantId)

        if (currentPlant != null) {
            // Stwórz zaktualizowaną wersję, zachowując wartości, które nie są zmieniane
            val updatedPlant = currentPlant.copy(
                variety = updates.variety ?: currentPlant.variety,
                quantity = updates.quantity ?: currentPlant.quantity,
                plantingDate = updates.plantingDate ?: currentPlant.plantingDate,
                notes = updates.notes ?: currentPlant.notes,
                waterRequirement = updates.waterRequirement ?: currentPlant.waterRequirement,
                lightRequirement = updates.lightRequirement ?: currentPlant.lightRequirement,
                soilType = updates.soilType ?: currentPlant.soilType,
                customTasks = updates.customTasks ?: currentPlant.customTasks
            )

            // Zapisz zaktualizowaną roślinę w bazie danych
            database.userPlantDao().update(updatedPlant)

            // Jeśli roślina jest powiązana z lokalizacją, zaktualizuj również powiązanie
            if (currentPlant.locationId.isNotEmpty()) {
                val placements = database.gardenSpaceDao()
                    .getPlacementsForLocation(currentPlant.locationId).first()

                val placement = placements.find {
                    it.plantId == currentPlant.plantId || it.plantId == currentPlant.name
                }

                placement?.let {
                    // Zamiast używać metody update, usuń stare powiązanie i dodaj nowe
                    database.gardenSpaceDao().deletePlantPlacement(it)

                    val updatedPlacement = PlantPlacementEntity(
                        id = it.id, // Zachowaj to samo ID
                        plantId = it.plantId,
                        locationId = it.locationId,
                        variety = updates.variety ?: it.variety,
                        quantity = updates.quantity ?: it.quantity,
                        plantingDate = updates.plantingDate ?: it.plantingDate,
                        notes = updates.notes ?: it.notes
                    )

                    database.gardenSpaceDao().insertPlantPlacement(updatedPlacement)
                }
            }
        }
    }
}