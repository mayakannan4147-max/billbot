package com.billbot.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["productCode"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productCode: String,
    val name: String,
    val sellingPrice: Double,
    val stockQuantity: Int,
    val unit: String = "pcs",
    val createdAt: Long = System.currentTimeMillis()
)
