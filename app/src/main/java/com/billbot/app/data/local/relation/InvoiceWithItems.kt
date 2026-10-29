package com.billbot.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.billbot.app.data.local.entity.InvoiceEntity
import com.billbot.app.data.local.entity.InvoiceItemEntity

data class InvoiceWithItems(
    @Embedded
    val invoice: InvoiceEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>
)
