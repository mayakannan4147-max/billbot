package com.billbot.app.domain.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val numberFormat = (NumberFormat.getNumberInstance(Locale("en", "IN")) as DecimalFormat).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    /**
     * Formats an amount with the provided currency symbol (defaults to ₹).
     * Example: 12500.50 -> "₹12,500.50"
     */
    fun format(amount: Double, symbol: String = "₹"): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        return "$symbol${numberFormat.format(safeAmount)}"
    }

    /**
     * Formats an amount without the symbol.
     */
    fun formatWithoutSymbol(amount: Double): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        return numberFormat.format(safeAmount)
    }
}
