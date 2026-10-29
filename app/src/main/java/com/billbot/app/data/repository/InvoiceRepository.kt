package com.billbot.app.data.repository

import androidx.room.withTransaction
import com.billbot.app.data.local.BillBotDatabase
import com.billbot.app.data.local.entity.InvoiceEntity
import com.billbot.app.data.local.entity.InvoiceItemEntity
import com.billbot.app.data.local.relation.InvoiceWithItems
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val database: BillBotDatabase
) {
    private val invoiceDao = database.invoiceDao()
    private val productDao = database.productDao()
    private val settingsDao = database.businessSettingsDao()

    val allInvoices: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoicesWithItemsFlow()

    fun getRecentInvoices(limit: Int = 10): Flow<List<InvoiceWithItems>> =
        invoiceDao.getRecentInvoicesWithItemsFlow(limit)

    fun searchInvoices(query: String): Flow<List<InvoiceWithItems>> {
        return if (query.isBlank()) {
            invoiceDao.getAllInvoicesWithItemsFlow()
        } else {
            invoiceDao.searchInvoicesFlow(query.trim())
        }
    }

    suspend fun getInvoiceById(id: Long): InvoiceWithItems? =
        invoiceDao.getInvoiceWithItemsById(id)

    suspend fun getInvoiceByNumber(number: String): InvoiceWithItems? =
        invoiceDao.getInvoiceWithItemsByNumber(number)

    fun getInvoicesInDateRange(startTime: Long, endTime: Long): Flow<List<InvoiceWithItems>> =
        invoiceDao.getInvoicesInDateRangeFlow(startTime, endTime)

    val totalInvoiceCount: Flow<Int> = invoiceDao.getTotalInvoiceCountFlow()

    val allInvoiceItems: Flow<List<InvoiceItemEntity>> = invoiceDao.getAllInvoiceItemsFlow()

    /**
     * Atomically saves the invoice, snapshot items, decrements stock, and increments invoice sequence.
     */
    suspend fun createInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long {
        return database.withTransaction {
            val invoiceId = invoiceDao.insertInvoice(invoice)

            val itemsWithInvoiceId = items.map { item ->
                item.copy(invoiceId = invoiceId)
            }
            invoiceDao.insertInvoiceItems(itemsWithInvoiceId)

            // Decrement stock for inventory products
            for (item in items) {
                if (item.productId != null && item.productId > 0) {
                    productDao.decrementStock(item.productId, item.quantity)
                }
            }

            // Increment sequence for subsequent invoices
            settingsDao.incrementInvoiceSequence()

            invoiceId
        }
    }

    /**
     * Records additional payment on an existing invoice, recalculating balance and status.
     */
    suspend fun recordPayment(
        invoiceId: Long,
        additionalAmount: Double,
        paymentMethod: PaymentMethod
    ) {
        database.withTransaction {
            val current = invoiceDao.getInvoiceWithItemsById(invoiceId)?.invoice ?: return@withTransaction
            val newPaidAmount = (current.paidAmount + additionalAmount).coerceAtMost(current.grandTotal)
            val newBalance = (current.grandTotal - newPaidAmount).coerceAtLeast(0.0)

            val newStatus = when {
                newBalance <= 0.0 -> PaymentStatus.PAID
                newPaidAmount > 0.0 -> PaymentStatus.PARTIAL
                else -> PaymentStatus.UNPAID
            }

            invoiceDao.updateInvoicePayment(
                invoiceId = invoiceId,
                paidAmount = newPaidAmount,
                balanceAmount = newBalance,
                status = newStatus,
                method = paymentMethod
            )
        }
    }

    suspend fun deleteInvoice(invoice: InvoiceEntity) {
        invoiceDao.deleteInvoice(invoice)
    }
}
