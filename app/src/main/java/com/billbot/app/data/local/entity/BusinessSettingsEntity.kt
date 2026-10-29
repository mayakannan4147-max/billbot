package com.billbot.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_settings")
data class BusinessSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "BillBot Retail Mart",
    val address: String = "123 Commercial Street, MG Road, Bengaluru, Karnataka - 560001",
    val phone: String = "+91 98765 43210",
    val email: String = "contact@billbotmart.in",
    val gstin: String = "29AAAAA0000A1Z5",
    val invoicePrefix: String = "INV-",
    val nextInvoiceSequence: Int = 1001,
    val defaultTaxRate: Double = 18.0,
    val isTaxEnabled: Boolean = true,
    val currencySymbol: String = "₹",
    val termsAndConditions: String = "Goods once sold will not be taken back. Subject to local jurisdiction. Thank you for your business!"
)
