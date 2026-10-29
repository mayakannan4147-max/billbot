package com.billbot.app

import com.billbot.app.domain.util.InvoiceNumberGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InvoiceNumberGeneratorTest {

    @Test
    fun testGenerateInvoiceNumber_defaultPrefix() {
        val invoiceNumber = InvoiceNumberGenerator.generateInvoiceNumber(
            prefix = "INV-",
            sequence = 1001
        )
        assertEquals("INV-1001", invoiceNumber)
    }

    @Test
    fun testGenerateInvoiceNumber_padding() {
        val invoiceNumber = InvoiceNumberGenerator.generateInvoiceNumber(
            prefix = "BILL-",
            sequence = 42,
            minDigits = 5
        )
        assertEquals("BILL-00042", invoiceNumber)
    }

    @Test
    fun testExtractSequence() {
        val seq1 = InvoiceNumberGenerator.extractSequence("INV-1005", "INV-")
        assertEquals(1005, seq1)

        val seq2 = InvoiceNumberGenerator.extractSequence("BILL-0020", "BILL-")
        assertEquals(20, seq2)

        val invalid = InvoiceNumberGenerator.extractSequence("XYZ-100", "INV-")
        assertNull(invalid)
    }
}
