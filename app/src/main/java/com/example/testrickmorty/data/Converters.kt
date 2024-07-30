package com.example.testrickmorty.data

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.let {
            val list = Gson().fromJson(it, Array<String>::class.java).toList()
            Log.d("Converters", "Deserialized List: $list")
            list
        } ?: emptyList()
    }

    @TypeConverter
    fun fromList(value: List<String>?): String {
        val json = Gson().toJson(value)
        Log.d("Converters", "Serialized JSON: $json")
        return json
    }
}
