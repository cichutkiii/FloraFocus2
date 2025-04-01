package pl.preclaw.florafocus.data.repository

import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.DateRange
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.model.PlantRule


val seedplants = listOf<Plant>()

//val apple = PlantRule(
//    species = "Malus domestica",
//    careSteps = listOf(
//        CareStep("Przycinanie", DateRange(15, 2, 31, 3)),
//        CareStep("Nawożenie", DateRange(1, 4, 30, 4)),
//        CareStep("Oprysk przeciw parchowi", DateRange(15, 4, 15, 5))
//    ),
//    weatherDependencies = mapOf(
//        "pruning" to 5.0..15.0,
//        "fertilizing" to 10.0..20.0
//    ),
//    growthPhaseTriggers = mapOf(
//        60 to "Kwitnienie",
//        120 to "Zawiązywanie owoców",
//        180 to "Dojrzewanie owoców"
//    )
//)
//
//val plum = PlantRule(
//    species = "Prunus domestica",
//    careSteps = listOf(
//        CareStep("Przycinanie", DateRange(1, 3, 15, 4)),
//        CareStep("Nawożenie", DateRange(15, 4, 15, 5)),
//        CareStep("Oprysk przeciw owocówce śliwkóweczce", DateRange(1, 6, 30, 6))
//    ),
//    weatherDependencies = mapOf(
//        "pruning" to 8.0..18.0,
//        "fertilizing" to 12.0..22.0
//    ),
//    growthPhaseTriggers = mapOf(
//        50 to "Kwitnienie",
//        100 to "Zawiązywanie owoców",
//        160 to "Dojrzewanie owoców"
//    )
//)
//
//val tomato = PlantRule(
//    species = "Solanum lycopersicum",
//    careSteps = listOf(
//        CareStep("Sadzenie", DateRange(15, 5, 31, 5)),
//        CareStep("Podwiązywanie", DateRange(1, 6, 30, 6)),
//        CareStep("Usuwanie bocznych pędów", DateRange(1, 6, 31, 8))
//    ),
//    weatherDependencies = mapOf(
//        "planting" to 15.0..25.0,
//        "watering" to 20.0..30.0
//    ),
//    growthPhaseTriggers = mapOf(
//        30 to "Kwitnienie",
//        60 to "Zawiązywanie owoców",
//        90 to "Dojrzewanie owoców"
//    )
//)
//
//val carrot = PlantRule(
//    species = "Daucus carota",
//    careSteps = listOf(
//        CareStep("Siew", DateRange(1, 4, 30, 4)),
//        CareStep("Przerzedzanie", DateRange(15, 5, 31, 5)),
//        CareStep("Nawadnianie", DateRange(1, 6, 31, 8))
//    ),
//    weatherDependencies = mapOf(
//        "sowing" to 8.0..18.0,
//        "watering" to 15.0..25.0
//    ),
//    growthPhaseTriggers = mapOf(
//        45 to "Rozwój liści",
//        90 to "Rozwój korzenia",
//        120 to "Dojrzewanie"
//    )
//)
//
//val rose = PlantRule(
//    species = "Rosa",
//    careSteps = listOf(
//        CareStep("Przycinanie", DateRange(15, 3, 15, 4)),
//        CareStep("Nawożenie", DateRange(1, 5, 31, 5)),
//        CareStep("Oprysk przeciw mszycam", DateRange(1, 6, 30, 6))
//    ),
//    weatherDependencies = mapOf(
//        "pruning" to 5.0..15.0,
//        "fertilizing" to 10.0..20.0
//    ),
//    growthPhaseTriggers = mapOf(
//        30 to "Pąkowanie",
//        60 to "Kwitnienie",
//        90 to "Przekwitanie"
//    )
//)
//
//val tulip = PlantRule(
//    species = "Tulipa",
//    careSteps = listOf(
//        CareStep("Sadzenie cebulek", DateRange(15, 9, 31, 10)),
//        CareStep("Nawożenie", DateRange(1, 3, 31, 3)),
//        CareStep("Wykopywanie cebulek", DateRange(1, 6, 30, 6))
//    ),
//    weatherDependencies = mapOf(
//        "planting" to 8.0..18.0,
//        "digging" to 15.0..25.0
//    ),
//    growthPhaseTriggers = mapOf(
//        30 to "Wzrost liści",
//        60 to "Kwitnienie",
//        90 to "Dojrzewanie cebulek"
//    )
//)
