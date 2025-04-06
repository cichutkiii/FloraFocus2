package pl.preclaw.florafocus.data.repository

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Tworzenie nowych tabel
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `garden_space` (
                `id` TEXT NOT NULL, 
                `name` TEXT NOT NULL, 
                `description` TEXT NOT NULL DEFAULT '', 
                PRIMARY KEY(`id`)
            )
            """
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `garden_area` (
                `id` TEXT NOT NULL, 
                `name` TEXT NOT NULL, 
                `description` TEXT NOT NULL DEFAULT '', 
                `parentId` TEXT NOT NULL, 
                PRIMARY KEY(`id`), 
                FOREIGN KEY(`parentId`) REFERENCES `garden_space`(`id`) ON DELETE CASCADE
            )
            """
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `plant_location` (
                `id` TEXT NOT NULL, 
                `name` TEXT NOT NULL, 
                `description` TEXT NOT NULL DEFAULT '', 
                `parentId` TEXT NOT NULL, 
                `type` TEXT NOT NULL, 
                `lightConditions` TEXT NOT NULL DEFAULT '', 
                `soilType` TEXT NOT NULL DEFAULT '', 
                `notes` TEXT NOT NULL DEFAULT '', 
                PRIMARY KEY(`id`), 
                FOREIGN KEY(`parentId`) REFERENCES `garden_area`(`id`) ON DELETE CASCADE
            )
            """
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `plant_placement` (
                `id` TEXT NOT NULL, 
                `plantId` TEXT NOT NULL, 
                `locationId` TEXT NOT NULL, 
                `plantingDate` TEXT NOT NULL DEFAULT '', 
                `quantity` INTEGER NOT NULL DEFAULT 1, 
                `notes` TEXT NOT NULL DEFAULT '', 
                `variety` TEXT NOT NULL DEFAULT '', 
                PRIMARY KEY(`id`), 
                FOREIGN KEY(`locationId`) REFERENCES `plant_location`(`id`) ON DELETE CASCADE
            )
            """
        )
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Dodaj instrukcje migracji, jeśli są potrzebne
        // Na przykład:
        database.execSQL("ALTER TABLE userplant ADD COLUMN locationId TEXT NOT NULL DEFAULT ''")
    }
}
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Migracja z wersji 3 do 4
        // Najpierw tworzymy tymczasową tabelę z nową strukturą
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS userplant_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                plantId TEXT NOT NULL DEFAULT '',
                name TEXT NOT NULL,
                careSteps TEXT NOT NULL,
                locationId TEXT NOT NULL DEFAULT '',
                edible INTEGER NOT NULL DEFAULT 0,
                growth TEXT NOT NULL DEFAULT '',
                waterRequirement TEXT NOT NULL DEFAULT '',
                lightRequirement TEXT NOT NULL DEFAULT '',
                usdaHardinessZone TEXT NOT NULL DEFAULT '',
                soilType TEXT NOT NULL DEFAULT '',
                family TEXT NOT NULL DEFAULT '',
                edibleParts TEXT NOT NULL DEFAULT '[]',
                sowingDate TEXT NOT NULL DEFAULT '{"start":"","end":""}',
                pests TEXT NOT NULL DEFAULT '[]',
                diseases TEXT NOT NULL DEFAULT '[]',
                companions TEXT NOT NULL DEFAULT '[]',
                incompatibles TEXT NOT NULL DEFAULT '[]'
            )
            """
        )

        // Kopiujemy dane ze starej tabeli do nowej
        database.execSQL(
            """
            INSERT INTO userplant_new (id, name, careSteps, locationId)
            SELECT id, name, careSteps, locationId FROM userplant
            """
        )

        // Usuwamy starą tabelę
        database.execSQL("DROP TABLE userplant")

        // Zmieniamy nazwę nowej tabeli na oryginalną
        database.execSQL("ALTER TABLE userplant_new RENAME TO userplant")
    }
}
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Migracja z wersji 4 do 5
        // Najpierw tworzymy tymczasową tabelę z nową strukturą
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS userplant_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                plantId TEXT NOT NULL DEFAULT '',
                name TEXT NOT NULL,
                careSteps TEXT NOT NULL,
                locationId TEXT NOT NULL DEFAULT '',
                edible INTEGER NOT NULL DEFAULT 0,
                growth TEXT NOT NULL DEFAULT '',
                waterRequirement TEXT NOT NULL DEFAULT '',
                lightRequirement TEXT NOT NULL DEFAULT '',
                usdaHardinessZone TEXT NOT NULL DEFAULT '',
                soilType TEXT NOT NULL DEFAULT '',
                family TEXT NOT NULL DEFAULT '',
                edibleParts TEXT NOT NULL DEFAULT '[]',
                sowingDate TEXT NOT NULL DEFAULT '{"start":"","end":""}',
                pests TEXT NOT NULL DEFAULT '[]',
                diseases TEXT NOT NULL DEFAULT '[]',
                companions TEXT NOT NULL DEFAULT '[]',
                incompatibles TEXT NOT NULL DEFAULT '[]',
                plantingDate TEXT NOT NULL DEFAULT '',
                variety TEXT NOT NULL DEFAULT '',
                quantity INTEGER NOT NULL DEFAULT 1,
                notes TEXT NOT NULL DEFAULT '',
                customTasks TEXT NOT NULL DEFAULT '[]'
            )
            """
        )

        // Kopiujemy dane ze starej tabeli do nowej
        database.execSQL(
            """
            INSERT INTO userplant_new (
                id, plantId, name, careSteps, locationId, 
                edible, growth, waterRequirement, lightRequirement, usdaHardinessZone, 
                soilType, family, edibleParts, sowingDate, pests, 
                diseases, companions, incompatibles
            )
            SELECT 
                id, plantId, name, careSteps, locationId, 
                edible, growth, waterRequirement, lightRequirement, usdaHardinessZone, 
                soilType, family, edibleParts, sowingDate, pests, 
                diseases, companions, incompatibles 
            FROM userplant
            """
        )

        // Usuwamy starą tabelę
        database.execSQL("DROP TABLE userplant")

        // Zmieniamy nazwę nowej tabeli na oryginalną
        database.execSQL("ALTER TABLE userplant_new RENAME TO userplant")
    }
}
// W pliku Migration.kt dodaj nową migrację

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Tworzymy tymczasową tabelę z nową strukturą
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS userplant_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                plantId TEXT NOT NULL DEFAULT '',
                name TEXT NOT NULL,
                careSteps TEXT NOT NULL,
                locationId TEXT NOT NULL DEFAULT '',
                edible INTEGER NOT NULL DEFAULT 0,
                growth TEXT NOT NULL DEFAULT '',
                waterRequirement TEXT NOT NULL DEFAULT '',
                lightRequirement TEXT NOT NULL DEFAULT '',
                usdaHardinessZone TEXT NOT NULL DEFAULT '',
                soilType TEXT NOT NULL DEFAULT '',
                family TEXT NOT NULL DEFAULT '',
                edibleParts TEXT NOT NULL DEFAULT '[]',
                sowingDate TEXT NOT NULL DEFAULT '{"start":"","end":""}',
                pests TEXT NOT NULL DEFAULT '[]',
                diseases TEXT NOT NULL DEFAULT '[]',
                companions TEXT NOT NULL DEFAULT '[]',
                incompatibles TEXT NOT NULL DEFAULT '[]',
                weatherDependencies TEXT NOT NULL DEFAULT '{}',
                growthPhaseTriggers TEXT NOT NULL DEFAULT '{}',
                plantingDate TEXT NOT NULL DEFAULT '',
                variety TEXT NOT NULL DEFAULT '',
                quantity INTEGER NOT NULL DEFAULT 1,
                notes TEXT NOT NULL DEFAULT '',
                customTasks TEXT NOT NULL DEFAULT '[]'
            )
            """
        )

        // Kopiujemy dane ze starej tabeli do nowej
        database.execSQL(
            """
            INSERT INTO userplant_new (
                id, plantId, name, careSteps, locationId, 
                edible, growth, waterRequirement, lightRequirement, usdaHardinessZone, 
                soilType, family, edibleParts, sowingDate, pests, 
                diseases, companions, incompatibles, plantingDate, variety,
                quantity, notes, customTasks
            )
            SELECT 
                id, plantId, name, careSteps, locationId, 
                edible, growth, waterRequirement, lightRequirement, usdaHardinessZone, 
                soilType, family, edibleParts, sowingDate, pests, 
                diseases, companions, incompatibles, plantingDate, variety,
                quantity, notes, customTasks
            FROM userplant
            """
        )

        // Usuwamy starą tabelę
        database.execSQL("DROP TABLE userplant")

        // Zmieniamy nazwę nowej tabeli na oryginalną
        database.execSQL("ALTER TABLE userplant_new RENAME TO userplant")
    }
}