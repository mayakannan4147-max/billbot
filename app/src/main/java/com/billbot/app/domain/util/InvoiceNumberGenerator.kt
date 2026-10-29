package com.billbot.app.domain.util

object InvoiceNumberGenerator {

    /**
     * Generates a formatted invoice number from a prefix and sequence number.
     * Example: prefix="INV-", sequence=1001, minDigits=4 -> "INV-1001"
     * Example: prefix="BILL-", sequence=5, minDigits=4 -> "BILL-0005"
     */
    fun generateInvoiceNumber(
        prefix: String = "INV-",
        sequence: Int,
        minDigits: Int = 4
    ): String {
        val cleanPrefix = prefix.trim()
        val safeSequence = maxOf(1, sequence)
        val formattedNumber = String.format("%0${minDigits}d", safeSequence)
        return "$cleanPrefix$formattedNumber"
    }

    /**
     * Extracts the sequence integer from an invoice number if it matches the prefix.
     */
    fun extractSequence(invoiceNumber: String, prefix: String = "INV-"): Int? {
        val cleanPrefix = prefix.trim()
        if (invoiceNumber.startsWith(cleanPrefix)) {
            val suffix = invoiceNumber.substring(cleanPrefix.length)
            return suffix.toIntOrNull()
        }
        return null
    }
}
