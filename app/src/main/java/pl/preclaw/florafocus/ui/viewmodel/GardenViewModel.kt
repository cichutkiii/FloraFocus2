package pl.preclaw.florafocus.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.preclaw.florafocus.data.repository.*

class GardenViewModel(application: Application) : AndroidViewModel(application) {
    private val gardenDao = AppDatabase.getInstance(application).gardenSpaceDao()

    // Pobieranie danych
    val allSpaces = gardenDao.getAllSpacesWithAreas()

    fun getAreasForSpace(spaceId: String): Flow<List<GardenAreaEntity>> {
        return gardenDao.getAreasForSpace(spaceId)
    }

    fun getLocationsForArea(areaId: String): Flow<List<PlantLocationEntity>> {
        return gardenDao.getLocationsForArea(areaId)
    }

    fun getPlacementsForLocation(locationId: String): Flow<List<PlantPlacementEntity>> {
        return gardenDao.getPlacementsForLocation(locationId)
    }

    // Dodawanie danych
    fun addSpace(space: GardenSpaceEntity) {
        viewModelScope.launch {
            gardenDao.insertSpace(space)
        }
    }

    fun addArea(area: GardenAreaEntity) {
        viewModelScope.launch {
            gardenDao.insertArea(area)
        }
    }

    fun addLocation(location: PlantLocationEntity) {
        viewModelScope.launch {
            gardenDao.insertLocation(location)
        }
    }

    fun addPlantPlacement(placement: PlantPlacementEntity) {
        viewModelScope.launch {
            gardenDao.insertPlantPlacement(placement)
        }
    }

    // Usuwanie danych
    fun deleteSpace(space: GardenSpaceEntity) {
        viewModelScope.launch {
            gardenDao.deleteSpace(space)
        }
    }

    fun deleteArea(area: GardenAreaEntity) {
        viewModelScope.launch {
            gardenDao.deleteArea(area)
        }
    }

    fun deleteLocation(location: PlantLocationEntity) {
        viewModelScope.launch {
            gardenDao.deleteLocation(location)
        }
    }

    fun deletePlantPlacement(placement: PlantPlacementEntity) {
        viewModelScope.launch {
            gardenDao.deletePlantPlacement(placement)
        }
    }
}