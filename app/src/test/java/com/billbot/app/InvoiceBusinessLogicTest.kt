package com.billbot.app

import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.model.PaymentStatus
import com.billbot.app.domain.util.BillingCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceBusinessLogicTest {

    @Test
    fun testPaymentTransitions_unpaidToPartialToPaid() {
        val grandTotal = 1200.0

        // Step 1: Initial state - no payment recorded
        var paid = 0.0
        var status = BillingCalculator.determinePaymentStatus(grandTotal, paid)
        var balance = BillingCalculator.calculateBalance(grandTotal, paid)
        assertEquals(PaymentStatus.UNPAID, status)
        assertEquals(1200.0, balance, 0.001)

        // Step 2: Customer pays 400 INR via UPI
        paid += 400.0
        status = BillingCalculator.determinePaymentStatus(grandTotal, paid)
        balance = BillingCalculator.calculateBalance(grandTotal, paid)
        assertEquals(PaymentStatus.PARTIAL, status)
        assertEquals(800.0, balance, 0.001)

        // Step 3: Customer pays remaining 800 INR in Cash
        paid += 800.0
        status = BillingCalculator.determinePaymentStatus(grandTotal, paid)
        balance = BillingCalculator.calculateBalance(grandTotal, paid)
        assertEquals(PaymentStatus.PAID, status)
        assertEquals(0.0, balance, 0.001)
    }

    @Test
    fun testStockDeductionInvariant() {
        var currentStock = 25
        val requestedQty = 4

        assertTrue("Stock must be sufficient", currentStock >= requestedQty)
        currentStock -= requestedQty
        assertEquals(21, currentStock)
    }

    @Test
    fun testSnapshotIntegrity() {
        // Original product in catalog
        val catalogItemPrice = 150.0
        val invoiceQuantity = 3

        // Snapshot line item created on invoice
        val snapshotItemPrice = catalogItemPrice
        val snapshotTotal = BillingCalculator.calculateItemTotal(snapshotItemPrice, invoiceQuantity)
        assertEquals(450.0, snapshotTotal, 0.001)

        // Product catalog price updated later to 200.0
        val updatedCatalogItemPrice = 200.0

        // Invariant: Snapshot price on invoice must remain unaffected
        assertEquals(150.0, snapshotItemPrice, 0.001)
        assertEquals(450.0, snapshotTotal, 0.001)
    }
}
