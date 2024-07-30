package com.example.testrickmorty.data

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.let {
            Gson().fromJson(it, Array<String>::class.java).toList()
        } ?: emptyList()
    }

    @TypeConverter
    fun fromList(value: List<String>?): String {
        return Gson().toJson(value)
    }
}
