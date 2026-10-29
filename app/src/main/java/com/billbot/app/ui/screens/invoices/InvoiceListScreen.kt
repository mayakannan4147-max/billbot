package com.billbot.app.ui.screens.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billbot.app.domain.model.PaymentStatus
import com.billbot.app.ui.components.BillBotSearchBar
import com.billbot.app.ui.components.EmptyState
import com.billbot.app.ui.screens.dashboard.InvoiceRowCard

@Composable
fun InvoiceListScreen(
    viewModel: InvoiceListViewModel,
    onInvoiceClick: (Long) -> Unit,
    onCreateBillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Invoices History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.invoices.size} invoices found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        BillBotSearchBar(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = "Search by invoice # or customer name..."
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips (All, Paid, Partial, Unpaid)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.statusFilter == null,
                onClick = { viewModel.setStatusFilter(null) },
                label = { Text("All") }
            )
            listOf(PaymentStatus.PAID, PaymentStatus.PARTIAL, PaymentStatus.UNPAID).forEach { status ->
                FilterChip(
                    selected = uiState.statusFilter == status,
                    onClick = { viewModel.setStatusFilter(status) },
                    label = { Text(status.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Invoices list
        if (uiState.invoices.isEmpty()) {
            EmptyState(
                icon = Icons.Default.ReceiptLong,
                title = "No Invoices Found",
                description = if (uiState.searchQuery.isNotBlank() || uiState.statusFilter != null) {
                    "No invoices match your current search/filter."
                } else {
                    "You haven't generated any invoices yet."
                },
                actionButtonText = "+ Create New Bill",
                onActionClick = onCreateBillClick,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.invoices, key = { it.invoice.id }) { item ->
                    InvoiceRowCard(
                        invoiceWithItems = item,
                        currencySymbol = uiState.currencySymbol,
                        onClick = { onInvoiceClick(item.invoice.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
