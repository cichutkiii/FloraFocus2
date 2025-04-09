package pl.preclaw.florafocus.data.repository

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.DateRange
import pl.preclaw.florafocus.data.model.LightCondition
import pl.preclaw.florafocus.data.model.LocationType
import pl.preclaw.florafocus.data.model.SoilType
import pl.preclaw.florafocus.data.model.TemperatureRange

class Converters {
    private val gson = Gson()

    // Konwertery dla CareStep
    // Konwerter dla List<CareStep>
    @TypeConverter
    fun fromCareStepList(value: List<CareStep>): String {
        return gson.toJson(value)
    }
    @TypeConverter
    fun toCareStepList(value: String): List<CareStep> {
        val type = object : TypeToken<List<CareStep>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // Konwerter dla List<String>
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // Konwerter dla DateRange
    @TypeConverter
    fun fromDateRange(value: DateRange): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDateRange(value: String): DateRange {
        return try {
            gson.fromJson(value, DateRange::class.java)
        } catch (e: Exception) {
            DateRange()
        }
    }

    // Konwerter dla Map<String, TemperatureRange>
    @TypeConverter
    fun fromTemperatureRangeMap(value: Map<String, TemperatureRange>): String {
        val type = object : TypeToken<Map<String, TemperatureRange>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toTemperatureRangeMap(value: String): Map<String, TemperatureRange> {
        val type = object : TypeToken<Map<String, TemperatureRange>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    // Konwerter dla Map<String, String>
    @TypeConverter
    fun fromStringStringMap(value: Map<String, String>): String {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toStringStringMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }



    // Konwertery dla LocationType
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








    // Konwertery dla Boolean
    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value != 0
    }

    // NOWE KONWERTERY
}