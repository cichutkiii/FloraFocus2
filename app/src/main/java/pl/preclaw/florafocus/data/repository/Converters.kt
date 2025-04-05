package pl.preclaw.florafocus.data.repository

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.DateRange
import pl.preclaw.florafocus.data.model.LightCondition
import pl.preclaw.florafocus.data.model.LocationType
import pl.preclaw.florafocus.data.model.SoilType

class Converters {
    private val gson = Gson()

    // Konwertery dla CareStep
    @TypeConverter
    fun fromCareStepList(value: List<CareStep>): String {
        val type = object : TypeToken<List<CareStep>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCareStepList(value: String): List<CareStep> {
        val type = object : TypeToken<List<CareStep>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
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

    // Konwertery dla LightCondition
    @TypeConverter
    fun fromLightCondition(value: LightCondition?): String? {
        return value?.name
    }

    @TypeConverter
    fun toLightCondition(value: String?): LightCondition? {
        return if (value == null) null else try {
            LightCondition.valueOf(value)
        } catch (e: Exception) {
            null
        }
    }

    // Konwertery dla SoilType
    @TypeConverter
    fun fromSoilType(value: SoilType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toSoilType(value: String?): SoilType? {
        return if (value == null) null else try {
            SoilType.valueOf(value)
        } catch (e: Exception) {
            null
        }
    }

    // Konwertery dla List<String>
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // Konwertery dla DateRange
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

    // Konwertery dla Boolean
    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value != 0
    }
}