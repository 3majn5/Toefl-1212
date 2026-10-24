package com.example.data.db

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        for (item in value) {
            array.put(item)
        }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        for (item in value) {
            array.put(item)
        }
        return array.toString()
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<Int>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            list.add(array.getInt(i))
        }
        return list
    }
}
