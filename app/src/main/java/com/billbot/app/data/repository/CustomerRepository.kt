package com.billbot.app.data.repository

import com.billbot.app.data.local.dao.CustomerDao
import com.billbot.app.data.local.dao.InvoiceDao
import com.billbot.app.data.local.entity.CustomerEntity
import com.billbot.app.data.local.relation.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

class CustomerRepository(
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao
) {
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomersFlow()

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> {
        return if (query.isBlank()) {
            customerDao.getAllCustomersFlow()
        } else {
            customerDao.searchCustomersFlow(query.trim())
        }
    }

    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)

    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    fun getCustomerPurchaseHistory(customerId: Long): Flow<List<InvoiceWithItems>> {
        return invoiceDao.getInvoicesForCustomerFlow(customerId)
    }

    val customerCount: Flow<Int> = customerDao.getCustomerCountFlow()
}
