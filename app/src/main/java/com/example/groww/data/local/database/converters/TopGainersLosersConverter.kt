package com.example.groww.data.local.database.converters

import androidx.room.TypeConverter
import com.example.groww.data.model.network.StockInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TopGainersLosersConverter {
    @TypeConverter
    fun fromStockInfoList(list: List<StockInfo>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toStockInfoList(json: String): List<StockInfo> {
        val type = object : TypeToken<List<StockInfo>>() {}.type
        return Gson().fromJson(json, type)
    }
}