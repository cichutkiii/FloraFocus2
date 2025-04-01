package pl.preclaw.florafocus.data.repository

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.preclaw.florafocus.data.model.CareStep

@Entity(tableName = "userplant")
data class UserPlant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // To powinno być pole na nazwę
    val careSteps: List<CareStep>
)