package pl.preclaw.florafocus.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.preclaw.florafocus.ui.viewmodel.GardenViewModel

// W MainScreen.kt, w funkcji MainScreen, dodaj GardenViewModel:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onAddPlantClick: () -> Unit) {
    val tabs = listOf("Zadania", "Rośliny", "Miejsca")
    var selectedTab by remember { mutableIntStateOf(0) }
    val mainViewModel: MainViewModel = viewModel()
    val gardenViewModel: GardenViewModel = viewModel()  // Dodane

    val plants by mainViewModel.plants.collectAsState(initial = emptyList())
    val allPlants by mainViewModel.allPlants.collectAsState(initial = emptyList())

    var showPlantSelection by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FloraFocus") }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    val icon = when (index) {
                        0 -> Icons.Default.CheckCircle
                        1 -> Icons.Default.Person
                        else -> Icons.Default.Place
                    }

                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> TasksScreen(mainViewModel)
                1 -> PlantsScreen(
                    plants = plants,
                    onAddPlantClick = onAddPlantClick,
                    viewModel = mainViewModel
                )
                2 -> PlacesScreen(
                    gardenViewModel = gardenViewModel,  // Zaktualizowane
                    mainViewModel = mainViewModel       // Dodane
                )
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