package pl.preclaw.florafocus.data.repository


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.LocationType

class Converters {
    @TypeConverter
    fun fromCareStepList(value: List<CareStep>): String {
        val gson = Gson()
        val type = object : TypeToken<List<CareStep>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCareStepList(value: String): List<CareStep> {
        val gson = Gson()
        val type = object : TypeToken<List<CareStep>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
    @TypeConverter
    fun fromLocationType(value: LocationType): String {
        return value.name
    }

    @TypeConverter
    fun toLocationType(value: String): LocationType {
        return try {
            LocationType.valueOf(value)
        } catch (e: Exception) {
            LocationType.OTHER
        }
    }
}