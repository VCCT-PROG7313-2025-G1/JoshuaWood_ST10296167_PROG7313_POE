package com.dreamteam.rand.data

import androidx.room.TypeConverter
import com.dreamteam.rand.data.entity.GoalStatus
import com.dreamteam.rand.data.entity.TransactionType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun fromGoalStatus(value: GoalStatus): String {
        return value.name
    }

    @TypeConverter
    fun toGoalStatus(value: String): GoalStatus {
        return GoalStatus.valueOf(value)
    }
} 