package pl.preclaw.florafocus.data.repository

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.preclaw.florafocus.data.model.CareStep

@Entity(tableName = "userplant")
data class UserPlant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val careSteps: List<CareStep>,
    val locationId: String = "" // Domyślna pusta wartość dla kompatybilności z istniejącymi danymi
)