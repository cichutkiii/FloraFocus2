package pl.preclaw.florafocus

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import pl.preclaw.florafocus.ui.screens.MainScreen
import pl.preclaw.florafocus.ui.screens.PlantSelectionScreen
import pl.preclaw.florafocus.ui.theme.FloraFocusTheme
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FloraFocusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val showPlantSelection = remember { mutableStateOf(false) }

                    if (showPlantSelection.value) {
                        PlantSelectionScreen(
                            availablePlants = viewModel.allPlants.value,
                            onPlantSelected = {
                                viewModel.addUserPlant(it)
                                showPlantSelection.value = false
                            },
                            onDismiss = { showPlantSelection.value = false }
                        )
                    } else {
                        MainScreen(
                            onAddPlantClick = { showPlantSelection.value = true }
                        )
                    }
                }
            }
        }
    }
}