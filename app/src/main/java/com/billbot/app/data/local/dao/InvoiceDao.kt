package com.billbot.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.billbot.app.data.local.entity.InvoiceEntity
import com.billbot.app.data.local.entity.InvoiceItemEntity
import com.billbot.app.data.local.relation.InvoiceWithItems
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoicesWithItemsFlow(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentInvoicesWithItemsFlow(limit: Int): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    @Transaction
    @Query("SELECT * FROM invoices WHERE invoiceNumber = :number LIMIT 1")
    suspend fun getInvoiceWithItemsByNumber(number: String): InvoiceWithItems?

    @Transaction
    @Query("SELECT * FROM invoices WHERE invoiceNumber LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchInvoicesFlow(query: String): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getInvoicesForCustomerFlow(customerId: Long): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getInvoicesInDateRangeFlow(startTime: Long, endTime: Long): Flow<List<InvoiceWithItems>>

    @Query("UPDATE invoices SET paidAmount = :paidAmount, balanceAmount = :balanceAmount, paymentStatus = :status, paymentMethod = :method WHERE id = :invoiceId")
    suspend fun updateInvoicePayment(
        invoiceId: Long,
        paidAmount: Double,
        balanceAmount: Double,
        status: PaymentStatus,
        method: PaymentMethod
    )

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("SELECT COUNT(*) FROM invoices")
    fun getTotalInvoiceCountFlow(): Flow<Int>

    @Query("SELECT * FROM invoice_items")
    fun getAllInvoiceItemsFlow(): Flow<List<InvoiceItemEntity>>
}
