package com.billbot.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billbot.app.domain.util.CurrencyFormatter
import com.billbot.app.ui.components.EmptyState
import com.billbot.app.ui.components.SectionHeader
import com.billbot.app.ui.components.StatCard
import com.billbot.app.ui.components.StatusBadge
import com.billbot.app.ui.theme.Amber100
import com.billbot.app.ui.theme.Amber600
import com.billbot.app.ui.theme.Blue100
import com.billbot.app.ui.theme.Blue600
import com.billbot.app.ui.theme.Emerald100
import com.billbot.app.ui.theme.Emerald600
import com.billbot.app.ui.theme.Rose100
import com.billbot.app.ui.theme.Rose600
import com.billbot.app.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onCreateBillClick: () -> Unit,
    onInvoiceClick: (Long) -> Unit,
    onViewAllInvoicesClick: () -> Unit,
    onManageInventoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val todayDateFormatted = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header greeting
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = "Welcome to BillBot",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = todayDateFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Metrics summary cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Today's Sales",
                        value = CurrencyFormatter.format(uiState.todaySales, uiState.currencySymbol),
                        icon = Icons.Default.CurrencyRupee,
                        containerColor = Emerald100,
                        contentColor = Emerald600,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Total Invoices",
                        value = uiState.totalInvoices.toString(),
                        icon = Icons.Default.Receipt,
                        containerColor = Blue100,
                        contentColor = Blue600,
                        modifier = Modifier.weight(1f)
                    )
                }

                StatCard(
                    title = "Pending Payments",
                    value = CurrencyFormatter.format(uiState.pendingPayments, uiState.currencySymbol),
                    subtitle = if (uiState.pendingPayments > 0) "Outstanding balance to collect" else "All bills fully settled",
                    icon = Icons.Default.PendingActions,
                    containerColor = if (uiState.pendingPayments > 0) Rose100 else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (uiState.pendingPayments > 0) Rose600 else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 3. Prominent "Create New Bill" Hero Action Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCreateBillClick() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Blue600, Color(0xFF1E40AF), Slate900)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quick Billing",
                                style = MaterialTheme.typography.labelLarge,
                                color = Blue100
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Create New Invoice",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Generate professional bill with GST & print PDF",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Bill",
                                tint = Blue600,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Low stock inventory warning if any
        if (uiState.lowStockProducts.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Amber100),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onManageInventoryClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Amber600,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low Stock Alert (${uiState.lowStockProducts.size} items)",
                                fontWeight = FontWeight.Bold,
                                color = Amber600,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${uiState.lowStockProducts.first().name} and others need restocking",
                                color = Slate900,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View",
                            tint = Amber600
                        )
                    }
                }
            }
        }

        // 5. Recent Invoices Section Header
        item {
            SectionHeader(
                title = "Recent Invoices",
                actionLabel = if (uiState.recentInvoices.isNotEmpty()) "View All" else null,
                onActionClick = onViewAllInvoicesClick
            )
        }

        // 6. Recent Invoices List or Empty State
        if (uiState.recentInvoices.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.PointOfSale,
                    title = "No Invoices Yet",
                    description = "Start your day by creating your first bill. Tap the button below.",
                    actionButtonText = "+ Create First Bill",
                    onActionClick = onCreateBillClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(uiState.recentInvoices, key = { it.invoice.id }) { item ->
                InvoiceRowCard(
                    invoiceWithItems = item,
                    currencySymbol = uiState.currencySymbol,
                    onClick = { onInvoiceClick(item.invoice.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InvoiceRowCard(
    invoiceWithItems: com.billbot.app.data.local.relation.InvoiceWithItems,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val invoice = invoiceWithItems.invoice
    val formattedDate = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(invoice.createdAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = invoice.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusBadge(status = invoice.paymentStatus)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = invoice.customerName.ifBlank { "Walk-in Customer" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "$formattedDate • ${invoiceWithItems.items.size} items • ${invoice.paymentMethod.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(invoice.grandTotal, currencySymbol),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                if (invoice.balanceAmount > 0.0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Due: ${CurrencyFormatter.format(invoice.balanceAmount, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
