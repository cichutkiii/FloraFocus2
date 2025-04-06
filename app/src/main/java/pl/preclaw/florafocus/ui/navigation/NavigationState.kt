package pl.preclaw.florafocus.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Klasa reprezentująca stan nawigacji w aplikacji
 */
data class NavigationState(
    val title: String,
    val showBackButton: Boolean = false,
    val actions: List<ActionItem> = emptyList(),
    val onBackPress: () -> Unit = {}
)

/**
 * Element akcji dostępny w pasku górnym
 */
data class ActionItem(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

/**
 * Pomocnicze metody do tworzenia standardowych stanów nawigacji
 */
object NavigationStateFactory {

    fun homeState(): NavigationState {
        return NavigationState(
            title = "FloraFocus",
            showBackButton = false
        )
    }

    fun detailsState(
        title: String,
        onBackPress: () -> Unit,
        onEditClick: () -> Unit
    ): NavigationState {
        return NavigationState(
            title = title,
            showBackButton = true,
            actions = listOf(
                ActionItem(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edytuj",
                    onClick = onEditClick
                )
            ),
            onBackPress = onBackPress
        )
    }
    // Dodaj te metody do klasy NavigationStateFactory

    /**
     * Tworzy stan nawigacji dla ekranu przestrzeni
     */
    fun spacesState(title: String): NavigationState {
        return NavigationState(
            title = title,
            showBackButton = false
        )
    }

    /**
     * Tworzy stan nawigacji dla ekranu obszarów
     */
    fun areasState(
        title: String,
        onBackPress: () -> Unit
    ): NavigationState {
        return NavigationState(
            title = title,
            showBackButton = true,
            onBackPress = onBackPress
        )
    }

    /**
     * Tworzy stan nawigacji dla ekranu lokalizacji
     */
    fun locationsState(
        title: String,
        onBackPress: () -> Unit
    ): NavigationState {
        return NavigationState(
            title = title,
            showBackButton = true,
            onBackPress = onBackPress
        )
    }

    /**
     * Tworzy stan nawigacji dla ekranu szczegółów lokalizacji
     */
    fun locationDetailsState(
        title: String,
        onBackPress: () -> Unit
    ): NavigationState {
        return NavigationState(
            title = title,
            showBackButton = true,
            onBackPress = onBackPress
        )
    }
}
enum class NavigationLevel {
    SPACES,        // Lista przestrzeni (ogródki, balkony itp.)
    AREAS,         // Lista obszarów w wybranej przestrzeni
    LOCATIONS,     // Lista lokalizacji w wybranym obszarze
    LOCATION_DETAILS  // Szczegóły wybranej lokalizacji
}