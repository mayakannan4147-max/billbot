package com.billbot.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billbot.app.data.local.entity.ProductEntity
import com.billbot.app.data.local.relation.InvoiceWithItems
import com.billbot.app.data.repository.InvoiceRepository
import com.billbot.app.data.repository.ProductRepository
import com.billbot.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class DashboardUiState(
    val todaySales: Double = 0.0,
    val totalInvoices: Int = 0,
    val pendingPayments: Double = 0.0,
    val recentInvoices: List<InvoiceWithItems> = emptyList(),
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val currencySymbol: String = "₹",
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        invoiceRepository.allInvoices,
        productRepository.allProducts,
        settingsRepository.settingsFlow
    ) { invoices, products, settings ->
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todaySales = invoices
            .filter { it.invoice.createdAt >= startOfToday }
            .sumOf { it.invoice.paidAmount }

        val pendingPayments = invoices
            .sumOf { it.invoice.balanceAmount }

        val recent = invoices.take(5)

        val lowStock = products.filter { it.stockQuantity in 0..5 }

        DashboardUiState(
            todaySales = todaySales,
            totalInvoices = invoices.size,
            pendingPayments = pendingPayments,
            recentInvoices = recent,
            lowStockProducts = lowStock,
            currencySymbol = settings?.currencySymbol ?: "₹",
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    class Factory(
        private val invoiceRepository: InvoiceRepository,
        private val productRepository: ProductRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(invoiceRepository, productRepository, settingsRepository) as T
        }
    }
}
