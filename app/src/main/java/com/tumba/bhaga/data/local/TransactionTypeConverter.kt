package com.tumba.bhaga.data.local

import androidx.room.TypeConverter
import com.tumba.bhaga.data.local.entity.TransactionType

class TransactionTypeConverter {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}