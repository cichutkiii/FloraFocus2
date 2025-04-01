package pl.preclaw.florafocus.data.repository


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pl.preclaw.florafocus.data.model.CareStep

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
}