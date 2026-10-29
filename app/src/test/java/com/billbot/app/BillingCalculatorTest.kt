package com.billbot.app

import com.billbot.app.data.local.entity.InvoiceItemEntity
import com.billbot.app.domain.model.PaymentStatus
import com.billbot.app.domain.util.BillingCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class BillingCalculatorTest {

    @Test
    fun testCalculateItemTotal_normal() {
        val total = BillingCalculator.calculateItemTotal(150.0, 3)
        assertEquals(450.0, total, 0.001)
    }

    @Test
    fun testCalculateItemTotal_fractionalPrice() {
        val total = BillingCalculator.calculateItemTotal(19.99, 3)
        // 19.99 * 3 = 59.97
        assertEquals(59.97, total, 0.001)
    }

    @Test
    fun testCalculateItemTotal_negativePriceAndQuantitySanitized() {
        val totalNegativePrice = BillingCalculator.calculateItemTotal(-50.0, 2)
        assertEquals(0.0, totalNegativePrice, 0.001)

        val totalNegativeQty = BillingCalculator.calculateItemTotal(50.0, -2)
        assertEquals(0.0, totalNegativeQty, 0.001)
    }

    @Test
    fun testCalculateSubtotal_multipleItems() {
        val items = listOf(
            InvoiceItemEntity(invoiceId = 1, productName = "Rice", unitPrice = 100.0, quantity = 2, totalAmount = 200.0),
            InvoiceItemEntity(invoiceId = 1, productName = "Oil", unitPrice = 150.50, quantity = 1, totalAmount = 150.50),
            InvoiceItemEntity(invoiceId = 1, productName = "Sugar", unitPrice = 45.0, quantity = 4, totalAmount = 180.0)
        )
        // 200 + 150.50 + 180 = 530.50
        val subtotal = BillingCalculator.calculateSubtotal(items)
        assertEquals(530.50, subtotal, 0.001)
    }

    @Test
    fun testCalculateDiscountAmount() {
        val discount10Percent = BillingCalculator.calculateDiscountAmount(1000.0, 10.0)
        assertEquals(100.0, discount10Percent, 0.001)

        val discountZeroPercent = BillingCalculator.calculateDiscountAmount(1000.0, 0.0)
        assertEquals(0.0, discountZeroPercent, 0.001)

        val discountOddPercent = BillingCalculator.calculateDiscountAmount(333.33, 5.0)
        // 333.33 * 0.05 = 16.6665 -> 16.67
        assertEquals(16.67, discountOddPercent, 0.001)
    }

    @Test
    fun testCalculateTaxAmount_gst() {
        // Taxable: 1000, GST: 18% -> 180.0
        val tax = BillingCalculator.calculateTaxAmount(1000.0, 18.0)
        assertEquals(180.0, tax, 0.001)

        // Taxable: 500, GST: 5% -> 25.0
        val tax5Percent = BillingCalculator.calculateTaxAmount(500.0, 5.0)
        assertEquals(25.0, tax5Percent, 0.001)
    }

    @Test
    fun testCalculateGrandTotal() {
        // Subtotal: 1000, Discount: 100, Taxable: 900, Tax (18% of 900): 162 -> Grand Total: 1062
        val grandTotal = BillingCalculator.calculateGrandTotal(
            subtotal = 1000.0,
            discountAmount = 100.0,
            taxAmount = 162.0
        )
        assertEquals(1062.0, grandTotal, 0.001)
    }

    @Test
    fun testDeterminePaymentStatus() {
        val grandTotal = 500.0

        // Zero payment -> UNPAID
        assertEquals(PaymentStatus.UNPAID, BillingCalculator.determinePaymentStatus(grandTotal, 0.0))

        // Negative payment -> UNPAID
        assertEquals(PaymentStatus.UNPAID, BillingCalculator.determinePaymentStatus(grandTotal, -50.0))

        // Partial payment -> PARTIAL
        assertEquals(PaymentStatus.PARTIAL, BillingCalculator.determinePaymentStatus(grandTotal, 200.0))

        // Full payment -> PAID
        assertEquals(PaymentStatus.PAID, BillingCalculator.determinePaymentStatus(grandTotal, 500.0))

        // Overpayment -> PAID
        assertEquals(PaymentStatus.PAID, BillingCalculator.determinePaymentStatus(grandTotal, 550.0))
    }

    @Test
    fun testCalculateBalance() {
        val grandTotal = 1500.0

        assertEquals(1500.0, BillingCalculator.calculateBalance(grandTotal, 0.0), 0.001)
        assertEquals(500.0, BillingCalculator.calculateBalance(grandTotal, 1000.0), 0.001)
        assertEquals(0.0, BillingCalculator.calculateBalance(grandTotal, 1500.0), 0.001)
        assertEquals(0.0, BillingCalculator.calculateBalance(grandTotal, 1600.0), 0.001)
    }
}
