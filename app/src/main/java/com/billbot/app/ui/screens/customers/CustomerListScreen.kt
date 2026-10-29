package com.billbot.app.ui.screens.customers

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billbot.app.data.local.entity.CustomerEntity
import com.billbot.app.data.local.relation.InvoiceWithItems
import com.billbot.app.domain.util.CurrencyFormatter
import com.billbot.app.ui.components.AddEditCustomerDialog
import com.billbot.app.ui.components.BillBotSearchBar
import com.billbot.app.ui.components.ConfirmDeleteDialog
import com.billbot.app.ui.components.EmptyState
import com.billbot.app.ui.components.StatusBadge
import com.billbot.app.ui.theme.Blue100
import com.billbot.app.ui.theme.Blue600
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerListScreen(
    viewModel: CustomerListViewModel,
    onInvoiceClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerForHistory by remember { mutableStateOf<CustomerEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        text = "Customer Directory",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.customers.size} registered customers",
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
                placeholder = "Search by customer name or phone..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Customers List
            if (uiState.customers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.People,
                    title = "No Customers Found",
                    description = if (uiState.searchQuery.isNotBlank()) "No customers match '${uiState.searchQuery}'" else "Add your frequent customers for quick invoicing and history tracking.",
                    actionButtonText = "+ Add New Customer",
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.customers, key = { it.id }) { customer ->
                        CustomerItemCard(
                            customer = customer,
                            onClick = { customerForHistory = customer },
                            onEdit = { customerToEdit = customer },
                            onDelete = { customerToDelete = customer }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddDialog) {
        AddEditCustomerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, email, address ->
                viewModel.addCustomer(name, phone, email, address)
                showAddDialog = false
            }
        )
    }

    // Edit Customer Dialog
    customerToEdit?.let { customer ->
        AddEditCustomerDialog(
            initialCustomer = customer,
            onDismiss = { customerToEdit = null },
            onSave = { name, phone, email, address ->
                viewModel.updateCustomer(
                    customer.copy(name = name, phone = phone, email = email, address = address)
                )
                customerToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    customerToDelete?.let { customer ->
        ConfirmDeleteDialog(
            title = "Delete Customer?",
            message = "Are you sure you want to delete '${customer.name}'? Existing past invoices will retain their snapshot name and data.",
            onConfirm = {
                viewModel.deleteCustomer(customer)
                customerToDelete = null
            },
            onDismiss = { customerToDelete = null }
        )
    }

    // Customer Purchase History Dialog
    customerForHistory?.let { customer ->
        val historyFlow = remember(customer.id) { viewModel.getCustomerPurchaseHistory(customer.id) }
        val history by historyFlow.collectAsStateWithLifecycle(emptyList())

        CustomerHistoryDialog(
            customer = customer,
            history = history,
            currencySymbol = uiState.currencySymbol,
            onInvoiceClick = { invoiceId ->
                customerForHistory = null
                onInvoiceClick(invoiceId)
            },
            onDismiss = { customerForHistory = null }
        )
    }
}

@Composable
fun CustomerItemCard(
    customer: CustomerEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Initials Circle
                val initials = customer.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Blue100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.uppercase(),
                        color = Blue600,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (customer.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (customer.address.isNotBlank()) {
                        Text(
                            text = customer.address,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerHistoryDialog(
    customer: CustomerEntity,
    history: List<InvoiceWithItems>,
    currencySymbol: String,
    onInvoiceClick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val totalPurchases = history.sumOf { it.invoice.grandTotal }
    val totalOutstanding = history.sumOf { it.invoice.balanceAmount }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(customer.name, fontWeight = FontWeight.Bold)
                if (customer.phone.isNotBlank()) {
                    Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                // Summary bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Spent", style = MaterialTheme.typography.labelSmall)
                            Text(
                                CurrencyFormatter.format(totalPurchases, currencySymbol),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pending Balance", style = MaterialTheme.typography.labelSmall)
                            Text(
                                CurrencyFormatter.format(totalOutstanding, currencySymbol),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (totalOutstanding > 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Invoices History (${history.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))

                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No purchase history found for this customer.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history, key = { it.invoice.id }) { item ->
                            val inv = item.invoice
                            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(inv.createdAt))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onInvoiceClick(inv.id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(inv.invoiceNumber, fontWeight = FontWeight.Bold)
                                        Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(CurrencyFormatter.format(inv.grandTotal, currencySymbol), fontWeight = FontWeight.Bold)
                                        StatusBadge(status = inv.paymentStatus)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
