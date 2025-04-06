package pl.preclaw.florafocus.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.DateRange
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.repository.AppDatabase
import pl.preclaw.florafocus.data.repository.FirebasePlantRepository
import pl.preclaw.florafocus.data.repository.UserPlant
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter

import pl.preclaw.florafocus.ui.navigation.NavigationState
import pl.preclaw.florafocus.ui.navigation.NavigationStateFactory

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).userPlantDao()
    private val firebaseRepo = FirebasePlantRepository()

    // Lista roślin użytkownika
    val userPlants = dao.getAll()

    // Lista wszystkich roślin z Firebase (do wyboru)
    private val _allPlants = MutableStateFlow<List<Plant>>(emptyList())
    val allPlants: StateFlow<List<Plant>> = _allPlants.asStateFlow()

    // Lista nadchodzących zadań
    private val _upcomingTasks = MutableStateFlow<List<Pair<Plant, CareStep>>>(emptyList())
    val upcomingTasks: StateFlow<List<Pair<Plant, CareStep>>> = _upcomingTasks.asStateFlow()

    // Dla zachowania kompatybilności z istniejącym kodem
    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants

    init {
        loadAllPlantsFromFirebase()
        observeUserPlants()
        viewModelScope.launch {
            // Odczekaj chwilę aż dane się załadują
            kotlinx.coroutines.delay(2000)
            debugPlants()
        }
    }
    private val _navigationState = MutableStateFlow(NavigationStateFactory.homeState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    /**
     * Aktualizuje stan nawigacji
     */
    fun updateNavigation(state: NavigationState) {
        _navigationState.value = state
    }

    /**
     * Przywraca domyślny stan nawigacji (ekran główny)
     */
    fun resetNavigation() {
        _navigationState.value = NavigationStateFactory.homeState()
    }

    /**
     * Ustawia stan nawigacji dla widoku szczegółów rośliny
     */
    fun setPlantDetailsNavigation(plantName: String, onBackPress: () -> Unit, onEditClick: () -> Unit) {
        _navigationState.value = NavigationStateFactory.detailsState(
            title = plantName,
            onBackPress = onBackPress,
            onEditClick = onEditClick
        )
    }
    // Obserwuj zmiany w liście roślin użytkownika i aktualizuj zadania
    private fun observeUserPlants() {
        viewModelScope.launch {
            userPlants.collect { userPlantsList ->
                updateUpcomingTasks(userPlantsList)
            }
        }
    }

    // Aktualizuj listę nadchodzących zadań
    private fun updateUpcomingTasks(userPlantsList: List<UserPlant>) {
        val tasks = mutableListOf<Pair<Plant, CareStep>>()
        val currentDate = LocalDate.now()

        userPlantsList.forEach { userPlant ->
            val plant = Plant(
                commonName = userPlant.name,
                careSteps = userPlant.careSteps
            )

            userPlant.careSteps.forEach { careStep ->
                if (isTaskUpcoming(careStep.dateRange, currentDate)) {
                    tasks.add(Pair(plant, careStep))
                }
            }
        }

        _upcomingTasks.value = tasks
    }

    // W klasie MainViewModel.kt, zaktualizuj metodę addUserPlant
    fun addUserPlant(
        plant: Plant,
        variety: String = "",
        quantity: Int = 1,
        notes: String = "",
        plantingDate: String = ""
    ) {
        viewModelScope.launch {
            // Określ nazwę dla rośliny - albo commonName, albo id (które jest nazwą w Firebase)
            val name = when {
                plant.commonName.isNotEmpty() -> plant.commonName
                plant.id != null -> plant.id
                else -> "Nieznana roślina"
            }

            println("Dodawanie rośliny do kolekcji użytkownika: $name")
            println("Szczegóły: odmiana=$variety, ilość=$quantity, data=$plantingDate, notatki=$notes")

            val userPlant = UserPlant(
                plantId = plant.id ?: "",  // Zapisz id jako plantId
                name = name,
                careSteps = plant.careSteps,
                // Nowe pola do przechowywania szczegółów nasadzenia
                variety = variety,
                quantity = quantity,
                notes = notes,
                plantingDate = plantingDate,
                // Skopiuj pozostałe dane z obiektu Plant
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
            dao.insert(userPlant)
        }
    }

    // Analogicznie zaktualizuj metodę addUserPlantToLocation
    fun addUserPlantToLocation(
        plant: Plant,
        locationId: String,
        variety: String = "",
        quantity: Int = 1,
        notes: String = "",
        plantingDate: String = ""
    ) {
        viewModelScope.launch {
            // Określ nazwę dla rośliny - albo commonName, albo id (które jest nazwą w Firebase)
            val name = when {
                plant.commonName.isNotEmpty() -> plant.commonName
                plant.id != null -> plant.id
                else -> "Nieznana roślina"
            }

            println("Dodawanie rośliny do lokalizacji: nazwa=$name, lokalizacja=$locationId, id=${plant.id}")
            println("Szczegóły: odmiana=$variety, ilość=$quantity, data=$plantingDate, notatki=$notes")

            val userPlant = UserPlant(
                plantId = plant.id ?: "",  // Zapisz id jako plantId
                name = name,
                careSteps = plant.careSteps,
                locationId = locationId,
                // Nowe pola do przechowywania szczegółów nasadzenia
                variety = variety,
                quantity = quantity,
                notes = notes,
                plantingDate = plantingDate,
                // Skopiuj pozostałe dane z obiektu Plant
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

            // Użyj nowej metody addPlantToLocation zamiast insert
            dao.addPlantToLocation(userPlant)
        }
    }

    // Usuń roślinę z listy użytkownika
    fun removeUserPlant(plant: UserPlant) {
        viewModelScope.launch {
            println("Usuwanie rośliny: ${plant.name}")
            dao.delete(plant)
        }
    }

    // Pobierz wszystkie rośliny z Firebase
    private fun loadAllPlantsFromFirebase() {
        viewModelScope.launch {
            firebaseRepo.getAllPlants().collect { plants ->
                // Logowanie każdej rośliny do celów debugowania
                println("Pobrano ${plants.size} roślin z Firebase")
                plants.forEach { plant ->
                    println("Roślina: id=${plant.id}, nazwa=${plant.commonName}, kroków=${plant.careSteps.size}")

                    // Jeśli commonName jest puste, ale id nie jest, ustawmy id jako commonName
                    if (plant.commonName.isEmpty() && plant.id != null) {
                        println("  -> Ustawiam id jako commonName dla ${plant.id}")
                    }
                }

                // Aktualizuj listę roślin
                _allPlants.value = plants
                _plants.value = plants // Dla kompatybilności
            }
        }
    }

    private fun isTaskUpcoming(dateRange: DateRange, currentDate: LocalDate): Boolean {
        if (dateRange.start.isEmpty() || dateRange.end.isEmpty()) {
            return false
        }

        try {
            // Parsuj daty jako MonthDay
            val formatter = DateTimeFormatter.ofPattern("dd-MM")

            // Uzyskaj MonthDay z ciągów znaków w formacie "dd-MM"
            val startMonthDay = MonthDay.parse(dateRange.start, formatter)
            val endMonthDay = MonthDay.parse(dateRange.end, formatter)

            // Uzyskaj MonthDay z aktualnej daty
            val currentMonthDay = MonthDay.from(currentDate)

            // Sprawdź czy okres przekracza granicę roku (np. od listopada do lutego)
            if (startMonthDay.isAfter(endMonthDay)) {
                // Data jest w okresie jeśli: jest przed końcem LUB po początku
                return currentMonthDay.isBefore(endMonthDay) ||
                        currentMonthDay.isAfter(startMonthDay) ||
                        currentMonthDay.equals(endMonthDay)
            } else {
                // Standardowy okres w obrębie jednego roku
                return (currentMonthDay.isAfter(startMonthDay) &&
                        currentMonthDay.isBefore(endMonthDay)) ||
                        currentMonthDay.equals(startMonthDay) ||
                        currentMonthDay.equals(endMonthDay)
            }
        } catch (e: Exception) {
            println("Błąd parsowania daty: ${e.message}, start=${dateRange.start}, end=${dateRange.end}")
            return false
        }
    }

    // Pobierz roślinę na podstawie jej ID
    fun getPlantById(plantId: String): Plant? {
        return _allPlants.value.find { it.id == plantId }
    }

    fun debugPlants() {
        viewModelScope.launch {
            // Debug istniejących roślin użytkownika
            userPlants.collect { plants ->
                println("DEBUG - Rośliny użytkownika (${plants.size}):")
                plants.forEach { plant ->
                    println("Roślina: ${plant.name}")
                    println("  Jadalna: ${plant.edible}")
                    println("  Water Req: ${plant.waterRequirement}")
                    println("  Light Req: ${plant.lightRequirement}")
                    println("  Soil Type: ${plant.soilType}")
                }
            }

            // Debug roślin z Firebase
            _allPlants.value.forEach { plant ->
                println("DEBUG Firebase - Roślina: ${plant.commonName} (${plant.id})")
                println("  Jadalna: ${plant.edible}")
                println("  Water Req: ${plant.waterRequirement}")
                println("  Light Req: ${plant.lightRequirement}")
                println("  Soil Type: ${plant.soilType}")
            }
        }
    }

    fun updateUserPlant(
        plant: UserPlant,
        variety: String,
        quantity: Int,
        notes: String,
        plantingDate: String,
        waterRequirement: String = plant.waterRequirement,
        lightRequirement: String = plant.lightRequirement,
        soilType: String = plant.soilType
    ) {
        viewModelScope.launch {
            // Utwórz zaktualizowaną kopię obiektu
            val updatedPlant = plant.copy(
                variety = variety,
                quantity = quantity,
                notes = notes,
                plantingDate = plantingDate,
                waterRequirement = waterRequirement,
                lightRequirement = lightRequirement,
                soilType = soilType
            )

            // Zapisz zaktualizowaną roślinę
            dao.insert(updatedPlant) // Room z onConflict = REPLACE zaktualizuje istniejący rekord
            println("Zaktualizowano roślinę: ${plant.name}, ID: ${plant.id}")
        }
    }
}