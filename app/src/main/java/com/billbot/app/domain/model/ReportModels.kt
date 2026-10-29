package com.billbot.app.domain.model

enum class TimeRangeFilter(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

data class ReportSummary(
    val totalRevenue: Double = 0.0,
    val totalInvoices: Int = 0,
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val averageInvoiceValue: Double = 0.0
)

data class BestSellingProduct(
    val productName: String,
    val productCode: String,
    val unitsSold: Int,
    val totalRevenue: Double
)

data class PaymentMethodBreakdown(
    val paymentMethod: PaymentMethod,
    val totalAmount: Double,
    val count: Int
)
