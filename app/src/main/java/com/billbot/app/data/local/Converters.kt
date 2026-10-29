package com.billbot.app.data.local

import androidx.room.TypeConverter
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.model.PaymentStatus

class Converters {
    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus?): String {
        return value?.name ?: PaymentStatus.UNPAID.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String?): PaymentStatus {
        return try {
            if (value != null) PaymentStatus.valueOf(value) else PaymentStatus.UNPAID
        } catch (e: IllegalArgumentException) {
            PaymentStatus.UNPAID
        }
    }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod?): String {
        return value?.name ?: PaymentMethod.NONE.name
    }

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod {
        return try {
            if (value != null) PaymentMethod.valueOf(value) else PaymentMethod.NONE
        } catch (e: IllegalArgumentException) {
            PaymentMethod.NONE
        }
    }
}
