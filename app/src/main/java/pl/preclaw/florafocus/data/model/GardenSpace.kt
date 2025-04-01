package pl.preclaw.florafocus.data.model

import java.util.UUID

// Główna przestrzeń (np. "Ogródek", "Balkon", "Mieszkanie")
data class GardenSpace(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val areas: MutableList<GardenArea> = mutableListOf()
)

// Podprzestrzeń (np. "Old Garden", "Prosecco", "New Garden")
data class GardenArea(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val parentId: String,  // ID głównej przestrzeni
    val locations: MutableList<PlantLocation> = mutableListOf()
)

// Element przestrzeni (np. "Grządka 1", "Pod cześnią")
data class PlantLocation(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val parentId: String,  // ID podprzestrzeni
    val type: LocationType = LocationType.BED,
    val lightConditions: String = "",
    val soilType: String = "",
    val notes: String = "",
    val plants: MutableList<PlantPlacement> = mutableListOf()
)

// Typ elementu przestrzeni
enum class LocationType {
    BED,          // Grządka
    RAISED_BED,   // Podwyższona grządka
    POT,          // Donica
    TREE_SPOT,    // Miejsce na drzewo
    GENERAL_AREA, // Ogólna przestrzeń
    OTHER         // Inne
}

// Roślina umieszczona w konkretnym miejscu
data class PlantPlacement(
    val id: String = UUID.randomUUID().toString(),
    val plantId: String,   // ID rośliny z modelu Plant
    val locationId: String, // ID elementu przestrzeni
    val plantingDate: String = "",
    val quantity: Int = 1,
    val notes: String = "",
    val variety: String = "" // Odmiana/gatunek, np. dla rozróżnienia tej samej rośliny w różnych grządkach
)