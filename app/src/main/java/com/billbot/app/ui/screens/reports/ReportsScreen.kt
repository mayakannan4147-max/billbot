package com.billbot.app.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.model.TimeRangeFilter
import com.billbot.app.domain.util.CurrencyFormatter
import com.billbot.app.ui.components.EmptyState
import com.billbot.app.ui.components.SectionHeader
import com.billbot.app.ui.components.StatCard
import com.billbot.app.ui.theme.Amber100
import com.billbot.app.ui.theme.Amber600
import com.billbot.app.ui.theme.Blue100
import com.billbot.app.ui.theme.Blue600
import com.billbot.app.ui.theme.Emerald100
import com.billbot.app.ui.theme.Emerald600
import com.billbot.app.ui.theme.Purple100
import com.billbot.app.ui.theme.Purple600
import com.billbot.app.ui.theme.Rose100
import com.billbot.app.ui.theme.Rose600

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currency = uiState.currencySymbol
    val summary = uiState.summary

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = "Sales & Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time performance reports from saved invoices",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Time Period Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRangeFilter.values().forEach { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.setTimeFilter(filter) },
                        label = { Text(filter.displayName) }
                    )
                }
            }
        }

        // Revenue Breakdown Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Revenue (${uiState.selectedFilter.displayName})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Blue600,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = CurrencyFormatter.format(summary.totalRevenue, currency),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Blue600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${summary.totalInvoices} Invoices • Avg. Ticket: ${CurrencyFormatter.format(summary.averageInvoiceValue, currency)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Blue600.copy(alpha = 0.85f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Collected (Paid)",
                        value = CurrencyFormatter.format(summary.totalPaid, currency),
                        icon = Icons.Default.CheckCircle,
                        containerColor = Emerald100,
                        contentColor = Emerald600,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Pending (Due)",
                        value = CurrencyFormatter.format(summary.totalPending, currency),
                        icon = Icons.Default.PendingActions,
                        containerColor = if (summary.totalPending > 0) Rose100 else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (summary.totalPending > 0) Rose600 else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Payment Methods Breakdown
        item {
            SectionHeader(title = "Payment Methods Breakdown")
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.paymentBreakdowns.forEach { breakdown ->
                        val (icon, color) = when (breakdown.paymentMethod) {
                            PaymentMethod.CASH -> Icons.Default.Payments to Emerald600
                            PaymentMethod.UPI -> Icons.Default.QrCode to Purple600
                            PaymentMethod.CARD -> Icons.Default.CreditCard to Blue600
                            PaymentMethod.NONE -> Icons.Default.Receipt to MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(breakdown.paymentMethod.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("${breakdown.count} invoices", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Text(
                                text = CurrencyFormatter.format(breakdown.totalAmount, currency),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        // Best-Selling Products Section
        item {
            SectionHeader(title = "Best-Selling Products")
        }

        if (uiState.topProducts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No product sales recorded in this time period.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            itemsIndexed(uiState.topProducts, key = { _, item -> item.productName }) { index, product ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank Badge
                            Surface(
                                shape = CircleShape,
                                color = if (index == 0) Amber100 else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (index == 0) Amber600 else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = product.productName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${product.unitsSold} units sold",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = CurrencyFormatter.format(product.totalRevenue, currency),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
