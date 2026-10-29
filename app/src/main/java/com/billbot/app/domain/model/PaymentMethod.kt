package com.billbot.app.domain.model

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card"),
    NONE("Unspecified")
}
