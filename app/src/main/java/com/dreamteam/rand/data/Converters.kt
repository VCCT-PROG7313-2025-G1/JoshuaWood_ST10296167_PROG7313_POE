package com.dreamteam.rand.data

import androidx.room.TypeConverter
import com.dreamteam.rand.data.entity.TransactionType
import java.util.Date

// converts between database types and kotlin types
class Converters {
    // convert timestamp to date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // convert date to timestamp
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // convert transaction type to string
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    // convert string to transaction type
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
} 