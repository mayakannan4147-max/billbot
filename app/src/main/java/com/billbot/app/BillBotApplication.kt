package com.billbot.app

import android.app.Application
import com.billbot.app.data.local.BillBotDatabase
import com.billbot.app.data.repository.CustomerRepository
import com.billbot.app.data.repository.InvoiceRepository
import com.billbot.app.data.repository.ProductRepository
import com.billbot.app.data.repository.SettingsRepository
import com.billbot.app.pdf.PdfInvoiceGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BillBotApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { BillBotDatabase.getDatabase(this, applicationScope) }

    val productRepository by lazy { ProductRepository(database.productDao()) }
    val customerRepository by lazy { CustomerRepository(database.customerDao(), database.invoiceDao()) }
    val invoiceRepository by lazy { InvoiceRepository(database) }
    val settingsRepository by lazy { SettingsRepository(database.businessSettingsDao()) }
    val pdfGenerator by lazy { PdfInvoiceGenerator(this) }
}
