package com.billbot.app.ui.screens.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billbot.app.data.local.entity.CustomerEntity
import com.billbot.app.data.local.relation.InvoiceWithItems
import com.billbot.app.data.repository.CustomerRepository
import com.billbot.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomerListUiState(
    val customers: List<CustomerEntity> = emptyList(),
    val searchQuery: String = "",
    val currencySymbol: String = "₹",
    val isLoading: Boolean = false
)

class CustomerListViewModel(
    private val customerRepository: CustomerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<CustomerListUiState> = combine(
        _searchQuery.flatMapLatest { query -> customerRepository.searchCustomers(query) },
        _searchQuery,
        settingsRepository.settingsFlow
    ) { customers, query, settings ->
        CustomerListUiState(
            customers = customers,
            searchQuery = query,
            currencySymbol = settings?.currencySymbol ?: "₹",
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomerListUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addCustomer(name: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            val customer = CustomerEntity(
                name = name,
                phone = phone,
                email = email,
                address = address
            )
            customerRepository.insertCustomer(customer)
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customer)
        }
    }

    fun getCustomerPurchaseHistory(customerId: Long): Flow<List<InvoiceWithItems>> {
        return customerRepository.getCustomerPurchaseHistory(customerId)
    }

    class Factory(
        private val customerRepository: CustomerRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CustomerListViewModel(customerRepository, settingsRepository) as T
        }
    }
}
