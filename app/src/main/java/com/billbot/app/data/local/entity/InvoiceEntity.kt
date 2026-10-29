package com.billbot.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.model.PaymentStatus

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["createdAt"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String = "",
    val subtotal: Double,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double,
    val balanceAmount: Double,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
