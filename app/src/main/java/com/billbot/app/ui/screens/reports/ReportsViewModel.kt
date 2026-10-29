package com.billbot.app.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billbot.app.data.repository.InvoiceRepository
import com.billbot.app.data.repository.SettingsRepository
import com.billbot.app.domain.model.BestSellingProduct
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.model.PaymentMethodBreakdown
import com.billbot.app.domain.model.ReportSummary
import com.billbot.app.domain.model.TimeRangeFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class ReportsUiState(
    val selectedFilter: TimeRangeFilter = TimeRangeFilter.THIS_MONTH,
    val summary: ReportSummary = ReportSummary(),
    val topProducts: List<BestSellingProduct> = emptyList(),
    val paymentBreakdowns: List<PaymentMethodBreakdown> = emptyList(),
    val currencySymbol: String = "₹",
    val isLoading: Boolean = false
)

class ReportsViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TimeRangeFilter.THIS_MONTH)

    val uiState: StateFlow<ReportsUiState> = combine(
        invoiceRepository.allInvoices,
        _selectedFilter,
        settingsRepository.settingsFlow
    ) { invoicesWithItems, filter, settings ->
        val (startTime, endTime) = calculateTimeRange(filter)

        val filteredInvoices = invoicesWithItems.filter {
            it.invoice.createdAt in startTime..endTime
        }

        val totalRev = filteredInvoices.sumOf { it.invoice.grandTotal }
        val totalPaid = filteredInvoices.sumOf { it.invoice.paidAmount }
        val totalPending = filteredInvoices.sumOf { it.invoice.balanceAmount }
        val count = filteredInvoices.size
        val avg = if (count > 0) totalRev / count else 0.0

        val summary = ReportSummary(
            totalRevenue = totalRev,
            totalInvoices = count,
            totalPaid = totalPaid,
            totalPending = totalPending,
            averageInvoiceValue = avg
        )

        // Calculate Best Selling Products
        val allItemsInFilteredInvoices = filteredInvoices.flatMap { it.items }
        val productGrouping = allItemsInFilteredInvoices.groupBy { it.productName }
        val topProducts = productGrouping.map { (name, items) ->
            val code = items.firstOrNull()?.productCode ?: ""
            val units = items.sumOf { it.quantity }
            val rev = items.sumOf { it.totalAmount }
            BestSellingProduct(
                productName = name,
                productCode = code,
                unitsSold = units,
                totalRevenue = rev
            )
        }.sortedByDescending { it.unitsSold }.take(5)

        // Calculate Payment Methods Breakdown
        val methodGroups = filteredInvoices.groupBy { it.invoice.paymentMethod }
        val paymentBreakdowns = listOf(PaymentMethod.CASH, PaymentMethod.UPI, PaymentMethod.CARD).map { method ->
            val matching = methodGroups[method] ?: emptyList()
            PaymentMethodBreakdown(
                paymentMethod = method,
                totalAmount = matching.sumOf { it.invoice.paidAmount },
                count = matching.size
            )
        }

        ReportsUiState(
            selectedFilter = filter,
            summary = summary,
            topProducts = topProducts,
            paymentBreakdowns = paymentBreakdowns,
            currencySymbol = settings?.currencySymbol ?: "₹",
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )

    fun setTimeFilter(filter: TimeRangeFilter) {
        _selectedFilter.value = filter
    }

    private fun calculateTimeRange(filter: TimeRangeFilter): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        return when (filter) {
            TimeRangeFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to now
            }
            TimeRangeFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to now
            }
            TimeRangeFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to now
            }
            TimeRangeFilter.ALL_TIME -> {
                0L to now
            }
        }
    }

    class Factory(
        private val invoiceRepository: InvoiceRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(invoiceRepository, settingsRepository) as T
        }
    }
}
