package com.billbot.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Billing : Screen("billing", "New Bill", Icons.Default.PointOfSale)
    data object Invoices : Screen("invoices", "Invoices", Icons.Default.ReceiptLong)
    data object Products : Screen("products", "Products", Icons.Default.Inventory2)
    data object Customers : Screen("customers", "Customers", Icons.Default.People)
    data object Reports : Screen("reports", "Reports", Icons.Default.Assessment)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    data object InvoiceDetail : Screen("invoice_detail/{invoiceId}", "Invoice Details") {
        fun createRoute(invoiceId: Long): String = "invoice_detail/$invoiceId"
    }

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Billing,
            Invoices,
            Products,
            Customers,
            Reports,
            Settings
        )
    }
}
