package com.billbot.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["invoiceId"]),
        Index(value = ["productId"])
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long = 0,
    val productId: Long? = null,
    val productCode: String = "",
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val totalAmount: Double
)
