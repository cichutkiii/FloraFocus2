package pl.preclaw.florafocus.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.preclaw.florafocus.data.repository.GardenAreaEntity
import pl.preclaw.florafocus.data.repository.PlantLocationEntity
import pl.preclaw.florafocus.ui.navigation.*
import pl.preclaw.florafocus.ui.viewmodel.GardenViewModel
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import pl.preclaw.florafocus.ui.navigation.NavigationLevel
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onAddPlantClick: () -> Unit) {
    val tabs = listOf("Zadania", "Rośliny", "Miejsca")
    var selectedTab by remember { mutableIntStateOf(0) }
    val mainViewModel: MainViewModel = viewModel()
    val gardenViewModel: GardenViewModel = viewModel()

    // Stan nawigacji
    val navState by mainViewModel.navigationState.collectAsState()

    // Stan widoku
    val plants by mainViewModel.plants.collectAsState(initial = emptyList())
    val allPlants by mainViewModel.allPlants.collectAsState(initial = emptyList())
    var showPlantSelection by remember { mutableStateOf(false) }

    // Stan dla zarządzania szczegółami rośliny
    var selectedPlantId by remember { mutableStateOf<Int?>(null) }

    // Stan dla śledzenia hierarchii nawigacji w PlacesScreen
    var currentLevel by remember { mutableStateOf(NavigationLevel.SPACES) }
    var selectedSpaceIndex by remember { mutableStateOf(0) }
    var currentArea by remember { mutableStateOf<GardenAreaEntity?>(null) }
    var currentLocation by remember { mutableStateOf<PlantLocationEntity?>(null) }

    // Pobieranie wszystkich przestrzeni dla obsługi przycisku wstecz
    val spacesWithAreas by gardenViewModel.allSpaces.collectAsState(initial = emptyList())

    // Obsługa przycisku wstecz
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    BackHandler {
        when {
            selectedPlantId != null -> {
                // Jeśli jesteśmy w szczegółach rośliny
                selectedPlantId = null
                mainViewModel.resetNavigation()
            }
            currentLevel == NavigationLevel.LOCATION_DETAILS -> {
                // Jeśli jesteśmy w szczegółach lokalizacji
                currentLocation = null
                currentLevel = NavigationLevel.LOCATIONS
                // Aktualizuj pasek nawigacji
                currentArea?.let { area ->
                    mainViewModel.updateNavigation(
                        NavigationStateFactory.locationsState(
                            title = area.name,
                            onBackPress = {
                                currentArea = null
                                currentLevel = NavigationLevel.AREAS
                            }
                        )
                    )
                }
            }
            currentLevel == NavigationLevel.LOCATIONS -> {
                // Jeśli jesteśmy na liście lokalizacji
                currentArea = null
                currentLevel = NavigationLevel.AREAS
                // Aktualizuj pasek nawigacji
                spacesWithAreas.getOrNull(selectedSpaceIndex)?.let { space ->
                    mainViewModel.updateNavigation(
                        NavigationStateFactory.areasState(
                            title = space.space.name,
                            onBackPress = {
                                currentLevel = NavigationLevel.SPACES
                            }
                        )
                    )
                }
            }
            currentLevel == NavigationLevel.AREAS -> {
                // Jeśli jesteśmy na liście obszarów
                currentLevel = NavigationLevel.SPACES
                mainViewModel.resetNavigation()
            }
            else -> {
                // Jeżeli jesteśmy na najwyższym poziomie, pozwól systemowi obsłużyć przycisk wstecz
                // To zamknie aplikację, jeśli nie ma więcej aktywności na stosie wstecz
                return@BackHandler
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(navState.title) },
                navigationIcon = {
                    if (navState.showBackButton) {
                        IconButton(onClick = navState.onBackPress) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Wróć"
                            )
                        }
                    }
                },
                actions = {
                    navState.actions.forEach { action ->
                        IconButton(onClick = action.onClick) {
                            Icon(action.icon, action.contentDescription)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (selectedPlantId == null && currentLevel == NavigationLevel.SPACES) {
                // Pokazuj menu tylko gdy nie jesteśmy w widoku szczegółów
                NavigationBar {
                    tabs.forEachIndexed { index, title ->
                        val icon = when (index) {
                            0 -> Icons.Default.CheckCircle
                            1 -> Icons.Default.Person
                            else -> Icons.Default.Place
                        }

                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                // Resetuj nawigację dla przełączenia zakładek
                                mainViewModel.resetNavigation()
                                // Resetuj stany nawigacji miejsc
                                currentLevel = NavigationLevel.SPACES
                                currentArea = null
                                currentLocation = null
                            },
                            icon = { Icon(icon, contentDescription = title) },
                            label = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                selectedPlantId != null -> {
                    // Widok szczegółów rośliny
                    PlantDetailsScreen(
                        userPlantId = selectedPlantId!!,
                        viewModel = mainViewModel,
                        onNavigateBack = {
                            selectedPlantId = null
                            mainViewModel.resetNavigation()
                        }
                    )
                }
                else -> {
                    // Główne ekrany aplikacji
                    when (selectedTab) {
                        0 -> TasksScreen(mainViewModel)
                        1 -> PlantsScreen(
                            plants = plants,
                            gardenViewModel = gardenViewModel,
                            viewModel = mainViewModel,
                            onPlantClick = { plant ->
                                selectedPlantId = plant.id
                                mainViewModel.setPlantDetailsNavigation(
                                    plantName = plant.name,
                                    onBackPress = {
                                        selectedPlantId = null
                                        mainViewModel.resetNavigation()
                                    },
                                    onEditClick = {
                                        // Dialog edycji jest pokazywany w PlantDetailsScreen
                                    }
                                )
                            }
                        )
                        2 -> PlacesScreen(
                            gardenViewModel = gardenViewModel,
                            mainViewModel = mainViewModel
                        )
                    }
                }
            }
        }
    }

    if (showPlantSelection) {
        PlantSelectionScreen(
            availablePlants = allPlants,
            onPlantSelected = {
                mainViewModel.addUserPlant(it)
                showPlantSelection = false
            },
            onDismiss = { showPlantSelection = false }
        )
    }
}