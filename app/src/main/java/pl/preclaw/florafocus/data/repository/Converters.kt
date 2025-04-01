package pl.preclaw.florafocus.data.repository

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.LightCondition
import pl.preclaw.florafocus.data.model.LocationType
import pl.preclaw.florafocus.data.model.SoilType

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

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}