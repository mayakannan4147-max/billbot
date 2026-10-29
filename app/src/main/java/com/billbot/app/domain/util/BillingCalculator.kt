package com.billbot.app.domain.util

import com.billbot.app.data.local.entity.InvoiceItemEntity
import com.billbot.app.domain.model.PaymentStatus
import java.math.BigDecimal
import java.math.RoundingMode

object BillingCalculator {

    /**
     * Rounds a double value to 2 decimal places using HALF_UP rounding.
     */
    fun roundToTwoDecimals(value: Double): Double {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    /**
     * Calculates the total amount for a single line item (unitPrice * quantity).
     * Prevents negative prices and negative quantities.
     */
    fun calculateItemTotal(unitPrice: Double, quantity: Int): Double {
        val safePrice = maxOf(0.0, unitPrice)
        val safeQty = maxOf(0, quantity)
        val total = BigDecimal.valueOf(safePrice).multiply(BigDecimal.valueOf(safeQty.toLong()))
        return total.setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Calculates the subtotal of an invoice from its line items.
     */
    fun calculateSubtotal(items: List<InvoiceItemEntity>): Double {
        var subtotal = BigDecimal.ZERO
        for (item in items) {
            val itemTotal = BigDecimal.valueOf(calculateItemTotal(item.unitPrice, item.quantity))
            subtotal = subtotal.add(itemTotal)
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Calculates the discount amount given the subtotal and discount percentage (0 - 100%).
     */
    fun calculateDiscountAmount(subtotal: Double, discountPercent: Double): Double {
        val safeSubtotal = maxOf(0.0, subtotal)
        val safePercent = discountPercent.coerceIn(0.0, 100.0)
        val discount = BigDecimal.valueOf(safeSubtotal)
            .multiply(BigDecimal.valueOf(safePercent))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        return discount.toDouble()
    }

    /**
     * Calculates GST / applicable tax on the taxable amount (subtotal - discount).
     */
    fun calculateTaxAmount(taxableAmount: Double, taxRatePercent: Double): Double {
        val safeTaxable = maxOf(0.0, taxableAmount)
        val safeRate = maxOf(0.0, taxRatePercent)
        val tax = BigDecimal.valueOf(safeTaxable)
            .multiply(BigDecimal.valueOf(safeRate))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        return tax.toDouble()
    }

    /**
     * Calculates the final payable grand total:
     * Grand Total = (Subtotal - Discount) + Tax
     */
    fun calculateGrandTotal(subtotal: Double, discountAmount: Double, taxAmount: Double): Double {
        val safeSubtotal = BigDecimal.valueOf(maxOf(0.0, subtotal))
        val safeDiscount = BigDecimal.valueOf(maxOf(0.0, discountAmount))
        val safeTax = BigDecimal.valueOf(maxOf(0.0, taxAmount))

        val taxable = safeSubtotal.subtract(safeDiscount).max(BigDecimal.ZERO)
        val grandTotal = taxable.add(safeTax)
        return grandTotal.setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Determines payment settlement status based on grandTotal and paidAmount.
     */
    fun determinePaymentStatus(grandTotal: Double, paidAmount: Double): PaymentStatus {
        val safeTotal = maxOf(0.0, grandTotal)
        val safePaid = maxOf(0.0, paidAmount)

        return when {
            safePaid <= 0.0 -> PaymentStatus.UNPAID
            safePaid >= safeTotal -> PaymentStatus.PAID
            else -> PaymentStatus.PARTIAL
        }
    }

    /**
     * Calculates the remaining unpaid balance.
     */
    fun calculateBalance(grandTotal: Double, paidAmount: Double): Double {
        val safeTotal = BigDecimal.valueOf(maxOf(0.0, grandTotal))
        val safePaid = BigDecimal.valueOf(maxOf(0.0, paidAmount))
        val balance = safeTotal.subtract(safePaid).max(BigDecimal.ZERO)
        return balance.setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}
