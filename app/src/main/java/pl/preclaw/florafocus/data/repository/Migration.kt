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