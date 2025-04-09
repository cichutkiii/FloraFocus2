package pl.preclaw.florafocus.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import pl.preclaw.florafocus.data.model.PlantUpdateDetails
import pl.preclaw.florafocus.data.repository.UserPlant
import pl.preclaw.florafocus.data.repository.UserPlantWithLocations

class PlantViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as FloraFocusApplication).plantRepository

    // Pobieranie danych
    val allTemplates = repository.getAllPlantTemplates()
    val userPlants = repository.getUserPlants()

    // Operacje na danych
    fun addPlant(templateId: String, name: String, variety: String, quantity: Int, notes: String, plantingDate: String) {
        viewModelScope.launch {
            repository.addPlant(
                templateId = templateId,
                name = name,
                variety = variety,
                quantity = quantity,
                notes = notes,
                plantingDate = plantingDate
            )
        }
    }

    fun updatePlant(plantId: Int, variety: String, quantity: Int, notes: String, plantingDate: String) {
        viewModelScope.launch {
            val updates = PlantUpdateDetails(
                variety = variety,
                quantity = quantity,
                notes = notes,
                plantingDate = plantingDate
            )
            repository.updatePlant(plantId, updates)
        }
    }

    fun deletePlant(plantId: Int) {
        viewModelScope.launch {
            repository.deletePlant(plantId)
        }
    }

    // Metody dla widoku szczegółów
    fun getPlantDetails(plantId: Int): Flow<UserPlantWithLocations?> =
        repository.getPlantDetails(plantId)

    // Metody dla widoku lokalizacji
    fun getPlantsInLocation(locationId: String): Flow<List<UserPlant>> =
        repository.getPlantsInLocation(locationId)

    fun addPlantToLocation(templateId: String, name: String, locationId: String) {
        viewModelScope.launch {
            repository.addPlant(
                templateId = templateId,
                name = name,
                locationId = locationId
            )
        }
    }

    fun checkCompatibility(templateId: String, locationId: String): Flow<Pair<List<UserPlant>, List<UserPlant>>> = flow {
        val result = repository.checkCompatibility(templateId, locationId)
        emit(result)
    }
}