package pl.preclaw.florafocus.utils

import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.repository.UserPlant

/**
 * Klasa narzędziowa do sprawdzania kompatybilności roślin
 */
object PlantCompatibilityChecker {

    /**
     * Sprawdza kompatybilność pomiędzy rośliną a istniejącymi roślinami w danej lokalizacji
     * @param plant Roślina do sprawdzenia
     * @param existingPlants Lista istniejących roślin w lokalizacji
     * @return Pair zawierające listę przyjaznych i nieprzyjaznych roślin
     */
    fun checkCompatibility(
        plant: Plant,
        existingPlants: List<UserPlant>
    ): Pair<List<UserPlant>, List<UserPlant>> {
        val compatiblePlants = mutableListOf<UserPlant>()
        val incompatiblePlants = mutableListOf<UserPlant>()

        // ID rośliny (może być nazwa polska lub łacińska)
        val plantId = plant.id ?: ""
        val plantName = plant.commonName.lowercase()

        // Lista przyjaznych i nieprzyjaznych roślin dla sprawdzanej rośliny
        val companions = plant.companions.map { it.lowercase() }
        val incompatibles = plant.incompatibles.map { it.lowercase() }

        for (existingPlant in existingPlants) {
            val existingName = existingPlant.name.lowercase()
            val existingId = existingPlant.plantId.lowercase()

            // Sprawdź czy istniejąca roślina jest na liście przyjaznych
            val isCompanion = companions.any { companion ->
                existingName.contains(companion) || existingId.contains(companion)
            }

            // Sprawdź czy istniejąca roślina jest na liście nieprzyjaznych
            val isIncompatible = incompatibles.any { incompatible ->
                existingName.contains(incompatible) || existingId.contains(incompatible)
            }

            // Sprawdź również czy dodawana roślina jest na liście przyjaznych/nieprzyjaznych
            // istniejącej rośliny
            val existingCompanions = existingPlant.companions.map { it.lowercase() }
            val existingIncompatibles = existingPlant.incompatibles.map { it.lowercase() }

            val isInExistingCompanions = existingCompanions.any { companion ->
                plantName.contains(companion) || plantId.contains(companion)
            }

            val isInExistingIncompatibles = existingIncompatibles.any { incompatible ->
                plantName.contains(incompatible) || plantId.contains(incompatible)
            }

            // Jeśli jest na jednej z list przyjaznych, dodaj do przyjaznych
            if (isCompanion || isInExistingCompanions) {
                compatiblePlants.add(existingPlant)
            }

            // Jeśli jest na jednej z list nieprzyjaznych, dodaj do nieprzyjaznych
            if (isIncompatible || isInExistingIncompatibles) {
                incompatiblePlants.add(existingPlant)
            }
        }

        return Pair(compatiblePlants, incompatiblePlants)
    }
}