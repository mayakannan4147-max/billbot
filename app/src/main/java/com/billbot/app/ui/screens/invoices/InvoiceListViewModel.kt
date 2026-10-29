package com.billbot.app.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billbot.app.data.local.relation.InvoiceWithItems
import com.billbot.app.data.repository.InvoiceRepository
import com.billbot.app.data.repository.SettingsRepository
import com.billbot.app.domain.model.PaymentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class InvoiceListUiState(
    val invoices: List<InvoiceWithItems> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: PaymentStatus? = null,
    val currencySymbol: String = "₹",
    val isLoading: Boolean = false
)

class InvoiceListViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<PaymentStatus?>(null)

    val uiState: StateFlow<InvoiceListUiState> = combine(
        _searchQuery.flatMapLatest { query -> invoiceRepository.searchInvoices(query) },
        _searchQuery,
        _statusFilter,
        settingsRepository.settingsFlow
    ) { invoices, query, filter, settings ->
        val filtered = if (filter != null) {
            invoices.filter { it.invoice.paymentStatus == filter }
        } else {
            invoices
        }
        InvoiceListUiState(
            invoices = filtered,
            searchQuery = query,
            statusFilter = filter,
            currencySymbol = settings?.currencySymbol ?: "₹",
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InvoiceListUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: PaymentStatus?) {
        _statusFilter.value = filter
    }

    class Factory(
        private val invoiceRepository: InvoiceRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InvoiceListViewModel(invoiceRepository, settingsRepository) as T
        }
    }
}
