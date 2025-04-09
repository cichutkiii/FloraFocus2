package pl.preclaw.florafocus.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.model.PlantUpdateDetails
import java.time.format.DateTimeFormatter

/**
 * Repozytorium do zarządzania roślinami, łączące dane z Firebase i lokalnej bazy Room.
 */
class PlantRepository(
    private val database: AppDatabase,
    private val firebaseRepo: FirebasePlantRepository
) {
    // SEKCJA 1: OPERACJE NA SZABLONACH ROŚLIN (Z FIREBASE)

    /**
     * Pobiera wszystkie dostępne szablony roślin z Firebase
     */
    fun getAllPlantTemplates(): Flow<List<PlantTemplate>> =
        firebaseRepo.getAllPlants().map { plants ->
            plants.map { plant ->
                PlantTemplate(
                    id = plant.id ?: "",
                    commonName = plant.commonName,
                    careSteps = plant.careSteps,
                    edible = plant.edible,
                    growth = plant.growth,
                    waterRequirement = plant.waterRequirement,
                    lightRequirement = plant.lightRequirement,
                    usdaHardinessZone = plant.usdaHardinessZone,
                    soilType = plant.soilType,
                    family = plant.family,
                    edibleParts = plant.edibleParts,
                    sowingDate = plant.sowingDate,
                    pests = plant.pests,
                    diseases = plant.diseases,
                    companions = plant.companions,
                    incompatibles = plant.incompatibles,
                    weatherDependencies = plant.weatherDependencies,
                    growthPhaseTriggers = plant.growthPhaseTriggers
                )
            }
        }

    /**
     * Pobiera konkretny szablon rośliny po ID
     */
    fun getPlantTemplateById(id: String): Flow<PlantTemplate?> =
        getAllPlantTemplates().map { templates ->
            templates.find { it.id == id }
        }

    // SEKCJA 2: OPERACJE NA ROŚLINACH UŻYTKOWNIKA

    /**
     * Pobiera wszystkie rośliny użytkownika
     */
    fun getUserPlants(): Flow<List<UserPlant>> =
        database.userPlantDao().getAllPlants()

    /**
     * Pobiera szczegóły rośliny po ID
     */
    fun getPlantDetails(plantId: Int): Flow<UserPlantWithLocations?> =
        database.userPlantDao().getPlantWithLocations(plantId)

    /**
     * Pobiera rośliny znajdujące się w określonej lokalizacji
     */
    fun getPlantsInLocation(locationId: String): Flow<List<UserPlant>> =
        database.userPlantDao().getPlantsInLocation(locationId)

    // SEKCJA 3: OPERACJE CRUD NA ROŚLINACH UŻYTKOWNIKA

    /**
     * Dodaje nową roślinę do kolekcji użytkownika na podstawie szablonu
     * Opcjonalnie umieszcza ją od razu w określonej lokalizacji
     */
    suspend fun addPlant(
        templateId: String,
        name: String = "",
        variety: String = "",
        quantity: Int = 1,
        notes: String = "",
        plantingDate: String = "",
        locationId: String? = null
    ): Int = database.runInTransaction {
        // 1. Pobierz szablon z Firebase dla tych danych
        val template = getAllPlantTemplates().first().find { it.id == templateId }

        // 2. Utwórz obiekt UserPlant
        val userPlant = UserPlant(
            templateId = templateId,
            name = name.ifEmpty { template?.commonName ?: templateId },
            variety = variety,
            notes = notes,
            quantity = quantity,
            plantingDate = plantingDate,
            // Skopiuj standardowe dane z szablonu
            edible = template?.edible ?: false,
            growth = template?.growth ?: "",
            waterRequirement = template?.waterRequirement ?: "",
            lightRequirement = template?.lightRequirement ?: "",
            soilType = template?.soilType ?: "",
            family = template?.family ?: ""
        )

        // 3. Zapisz roślinę w bazie danych
        val userPlantId = database.userPlantDao().insertPlant(userPlant).toInt()

        // 4. Jeśli podano lokalizację, dodaj powiązanie
        if (locationId != null && locationId.isNotEmpty()) {
            val crossRef = PlantLocationCrossRef(
                plantId = userPlantId,
                locationId = locationId,
                addedDate = getCurrentDate()
            )
            database.plantLocationDao().insertCrossRef(crossRef)
        }

        userPlantId // Zwróć ID nowo dodanej rośliny
    }

    /**
     * Aktualizuje istniejącą roślinę użytkownika
     */
    suspend fun updatePlant(plantId: Int, updates: PlantUpdateDetails) {
        database.runInTransaction {
            // 1. Pobierz aktualną roślinę
            val currentPlant = database.userPlantDao().getPlantById(plantId)

            // 2. Jeśli roślina istnieje, zaktualizuj ją
            if (currentPlant != null) {
                val updatedPlant = currentPlant.copy(
                    variety = updates.variety ?: currentPlant.variety,
                    quantity = updates.quantity ?: currentPlant.quantity,
                    plantingDate = updates.plantingDate ?: currentPlant.plantingDate,
                    notes = updates.notes ?: currentPlant.notes,
                    waterRequirement = updates.waterRequirement ?: currentPlant.waterRequirement,
                    lightRequirement = updates.lightRequirement ?: currentPlant.lightRequirement,
                    soilType = updates.soilType ?: currentPlant.soilType
                )

                // 3. Zapisz zaktualizowaną roślinę
                database.userPlantDao().updatePlant(updatedPlant)
            }
        }
    }

    /**
     * Usuwa roślinę użytkownika i wszystkie jej powiązania
     */
    suspend fun deletePlant(plantId: Int) {
        database.userPlantDao().deletePlantById(plantId)
        // Powiązania PlantLocationCrossRef zostaną usunięte kaskadowo
    }

    // SEKCJA 4: ZARZĄDZANIE POWIĄZANIAMI ROŚLINA-LOKALIZACJA

    /**
     * Dodaje istniejącą roślinę do określonej lokalizacji
     */
    suspend fun addPlantToLocation(plantId: Int, locationId: String, notes: String = "") {
        val crossRef = PlantLocationCrossRef(
            plantId = plantId,
            locationId = locationId,
            addedDate = getCurrentDate(),
            notes = notes
        )
        database.plantLocationDao().insertCrossRef(crossRef)
    }

    /**
     * Usuwa roślinę z określonej lokalizacji
     */
    suspend fun removePlantFromLocation(plantId: Int, locationId: String) {
        database.plantLocationDao().deleteCrossRefByIds(plantId, locationId)
    }

    /**
     * Sprawdza kompatybilność rośliny z roślinami w lokalizacji
     */
    suspend fun checkCompatibility(
        plantTemplateId: String,
        locationId: String
    ): Pair<List<UserPlant>, List<UserPlant>> {
        val plantsInLocation = getPlantsInLocation(locationId).first()
        val template = getPlantTemplateById(plantTemplateId).first() ?: return Pair(emptyList(), emptyList())

        val compatiblePlants = mutableListOf<UserPlant>()
        val incompatiblePlants = mutableListOf<UserPlant>()

        // Pobierz wszystkie szablony, aby sprawdzić kompatybilność
        val allTemplates = getAllPlantTemplates().first()

        for (plant in plantsInLocation) {
            val plantTemplate = allTemplates.find { it.id == plant.templateId }

            // Sprawdź czy rośliny są kompatybilne
            val isCompanion = template.companions.any { companion ->
                plantTemplate?.commonName?.contains(companion, ignoreCase = true) == true ||
                        plantTemplate?.id?.contains(companion, ignoreCase = true) == true
            }

            val isIncompatible = template.incompatibles.any { incompatible ->
                plantTemplate?.commonName?.contains(incompatible, ignoreCase = true) == true ||
                        plantTemplate?.id?.contains(incompatible, ignoreCase = true) == true
            }

            // Sklasyfikuj roślinę
            if (isCompanion) {
                compatiblePlants.add(plant)
            }

            if (isIncompatible) {
                incompatiblePlants.add(plant)
            }
        }

        return Pair(compatiblePlants, incompatiblePlants)
    }

    // SEKCJA 5: FUNKCJE POMOCNICZE

    private fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return LocalDate.now().format(formatter)
    }
}    // SEKCJA 5: FUNKCJE POMOCNICZE

    private fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return LocalDate.now().format(formatter)
    }
}