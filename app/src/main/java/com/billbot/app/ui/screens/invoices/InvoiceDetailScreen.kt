package com.billbot.app.ui.screens.invoices

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billbot.app.domain.model.PaymentStatus
import com.billbot.app.domain.util.CurrencyFormatter
import com.billbot.app.ui.components.RecordPaymentDialog
import com.billbot.app.ui.components.StatusBadge
import com.billbot.app.ui.theme.Emerald600
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceDetailScreen(
    viewModel: InvoiceDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRecordPaymentDialog by remember { mutableStateOf(false) }

    val invoiceWithItems = uiState.invoiceWithItems
    val currency = uiState.settings.currencySymbol

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = invoiceWithItems?.invoice?.invoiceNumber ?: "Invoice Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading || invoiceWithItems == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val invoice = invoiceWithItems.invoice
            val items = invoiceWithItems.items
            val dateStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date(invoice.createdAt))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Status & Meta Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = invoice.invoiceNumber,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = invoice.paymentStatus)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Customer Details:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = invoice.customerName.ifBlank { "Walk-in Customer" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (invoice.customerPhone.isNotBlank()) {
                                Text(
                                    text = "Phone: ${invoice.customerPhone}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Payment Method: ${invoice.paymentMethod.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. Action Buttons (Share PDF, View PDF, Record Payment)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.sharePdf(context) },
                            enabled = !uiState.isGeneratingPdf,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF")
                        }

                        OutlinedButton(
                            onClick = { viewModel.viewPdf(context) },
                            enabled = !uiState.isGeneratingPdf,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View PDF")
                        }
                    }
                }

                // Record Payment button if invoice has unpaid balance
                if (invoice.balanceAmount > 0.0) {
                    item {
                        Button(
                            onClick = { showRecordPaymentDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Record Payment (${CurrencyFormatter.format(invoice.balanceAmount, currency)} due)")
                        }
                    }
                }

                // 3. Line Items Snapshot Table
                item {
                    Text("Itemized Breakdown (${items.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(items, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                if (item.productCode.isNotBlank()) {
                                    Text("Code: ${item.productCode}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "${CurrencyFormatter.format(item.unitPrice, currency)} × ${item.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = CurrencyFormatter.format(item.totalAmount, currency),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                // 4. Financial Calculations Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Financial Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyFormatter.format(invoice.subtotal, currency))
                            }

                            if (invoice.discountAmount > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount (${invoice.discountPercent}%):", style = MaterialTheme.typography.bodyMedium)
                                    Text("- ${CurrencyFormatter.format(invoice.discountAmount, currency)}", color = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            if (invoice.taxAmount > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("GST / Tax (${invoice.taxRate}%):", style = MaterialTheme.typography.bodyMedium)
                                    Text("+ ${CurrencyFormatter.format(invoice.taxAmount, currency)}")
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    CurrencyFormatter.format(invoice.grandTotal, currency),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount Paid:", style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyFormatter.format(invoice.paidAmount, currency), color = Emerald600, fontWeight = FontWeight.SemiBold)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Balance Due:", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    CurrencyFormatter.format(invoice.balanceAmount, currency),
                                    color = if (invoice.balanceAmount > 0.0) MaterialTheme.colorScheme.error else Emerald600,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (invoice.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Notes: ${invoice.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Record Payment Dialog
    if (showRecordPaymentDialog && invoiceWithItems != null) {
        val invoice = invoiceWithItems.invoice
        RecordPaymentDialog(
            invoiceNumber = invoice.invoiceNumber,
            grandTotal = invoice.grandTotal,
            currentPaid = invoice.paidAmount,
            balance = invoice.balanceAmount,
            onDismiss = { showRecordPaymentDialog = false },
            onRecordPayment = { amount, method ->
                viewModel.recordPayment(amount, method)
                showRecordPaymentDialog = false
            }
        )
    }
}
