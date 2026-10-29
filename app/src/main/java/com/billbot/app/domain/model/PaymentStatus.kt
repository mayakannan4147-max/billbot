package com.billbot.app.domain.model

enum class PaymentStatus(val displayName: String) {
    PAID("Paid"),
    PARTIAL("Partially Paid"),
    UNPAID("Unpaid")
}
