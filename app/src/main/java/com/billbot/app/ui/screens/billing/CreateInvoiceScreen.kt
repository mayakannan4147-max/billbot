package com.billbot.app.ui.screens.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billbot.app.data.local.entity.CustomerEntity
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.model.PaymentStatus
import com.billbot.app.domain.util.CurrencyFormatter
import com.billbot.app.ui.components.QuantityStepper
import com.billbot.app.ui.components.SectionHeader
import com.billbot.app.ui.components.StatusBadge
import com.billbot.app.ui.theme.Emerald100
import com.billbot.app.ui.theme.Emerald600

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateInvoiceScreen(
    viewModel: CreateInvoiceViewModel,
    onInvoiceCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val products by viewModel.availableProducts.collectAsStateWithLifecycle()
    val customers by viewModel.availableCustomers.collectAsStateWithLifecycle()

    var showCustomItemDialog by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header with invoice number preview
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New Invoice",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fill in the details to generate bill",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.nextInvoiceNumberPreview,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 2. Customer Selection Section
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
                            Text(
                                text = "Customer Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (uiState.isWalkIn) "Walk-in" else "Registered",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = !uiState.isWalkIn,
                                    onCheckedChange = { isRegistered ->
                                        viewModel.setWalkIn(!isRegistered)
                                        if (isRegistered && customers.isNotEmpty() && uiState.selectedCustomer == null) {
                                            viewModel.setCustomer(customers.first())
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (uiState.isWalkIn) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.walkInName,
                                    onValueChange = { viewModel.setWalkInName(it) },
                                    label = { Text("Name") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = uiState.walkInPhone,
                                    onValueChange = { viewModel.setWalkInPhone(it) },
                                    label = { Text("Phone (Optional)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            if (uiState.selectedCustomer != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = uiState.selectedCustomer?.name ?: "",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            if (uiState.selectedCustomer?.phone?.isNotBlank() == true) {
                                                Text(
                                                    text = uiState.selectedCustomer?.phone ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        TextButton(onClick = { showCustomerPicker = true }) {
                                            Text("Change")
                                        }
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { showCustomerPicker = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Select Customer")
                                }
                            }
                        }
                    }
                }
            }

            // 3. Quick Add from Product Catalog
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Add Products",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showCustomItemDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Custom Item")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (products.isEmpty()) {
                        Text(
                            text = "No products in inventory. Tap '+ Custom Item' to add items manually.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(products, key = { it.id }) { product ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.addProductToCart(product) }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${CurrencyFormatter.format(product.sellingPrice, uiState.currencySymbol)} • Stock: ${product.stockQuantity}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Cart Items Section
            item {
                SectionHeader(title = "Bill Items (${uiState.cartItems.size})")
            }

            if (uiState.cartItems.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Cart is empty",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select products from above or add custom item",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.cartItems, key = { it.tempId }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                Text(
                                    text = item.productName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${CurrencyFormatter.format(item.unitPrice, uiState.currencySymbol)} each",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuantityStepper(
                                    quantity = item.quantity,
                                    onQuantityChange = { newQty -> viewModel.updateQuantity(item.tempId, newQty) },
                                    min = 1
                                )

                                Text(
                                    text = CurrencyFormatter.format(item.totalAmount, uiState.currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.width(70.dp)
                                )

                                IconButton(
                                    onClick = { viewModel.removeItem(item.tempId) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Bill Summary & Taxes
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Summary & Taxes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Subtotal
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyFormatter.format(uiState.subtotal, uiState.currencySymbol), fontWeight = FontWeight.SemiBold)
                        }

                        // Discount selection
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount (${uiState.discountPercent.toInt()}%)", style = MaterialTheme.typography.bodyMedium)
                                Text("- ${CurrencyFormatter.format(uiState.discountAmount, uiState.currencySymbol)}", color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.0, 5.0, 10.0, 15.0, 20.0).forEach { disc ->
                                    FilterChip(
                                        selected = uiState.discountPercent == disc,
                                        onClick = { viewModel.setDiscountPercent(disc) },
                                        label = { Text("${disc.toInt()}%") }
                                    )
                                }
                            }
                        }

                        // GST selection
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("GST / Applicable Tax", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = uiState.isTaxEnabled,
                                        onCheckedChange = { viewModel.toggleTaxEnabled(it) }
                                    )
                                }

                                if (uiState.isTaxEnabled) {
                                    Text("+ ${CurrencyFormatter.format(uiState.taxAmount, uiState.currencySymbol)}", fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text("Tax Disabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            if (uiState.isTaxEnabled) {
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(0.0, 5.0, 12.0, 18.0, 28.0).forEach { rate ->
                                        FilterChip(
                                            selected = uiState.taxRate == rate,
                                            onClick = { viewModel.setTaxRate(rate) },
                                            label = { Text("${rate.toInt()}%") }
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        // Grand Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grand Total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = CurrencyFormatter.format(uiState.grandTotal, uiState.currencySymbol),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 6. Payment Settlement Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Payment Settlement",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(status = uiState.paymentStatus)
                        }

                        Text("Payment Method:", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(PaymentMethod.CASH, PaymentMethod.UPI, PaymentMethod.CARD).forEach { method ->
                                FilterChip(
                                    selected = uiState.selectedPaymentMethod == method,
                                    onClick = { viewModel.setPaymentMethod(method) },
                                    label = { Text(method.displayName) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = uiState.paidAmountText,
                            onValueChange = { viewModel.setPaidAmount(it) },
                            label = { Text("Amount Paid (${uiState.currencySymbol})") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setPayFull() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Full Paid")
                            }

                            OutlinedButton(
                                onClick = { viewModel.setPayZero() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Unpaid (Due)")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance Due:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = CurrencyFormatter.format(uiState.balanceAmount, uiState.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.balanceAmount > 0.0) MaterialTheme.colorScheme.error else Emerald600
                            )
                        }

                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.setNotes(it) },
                            label = { Text("Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Error display if any
            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 7. Save & Generate Bill Button
            item {
                Button(
                    onClick = {
                        viewModel.submitInvoice { invoiceId ->
                            onInvoiceCreated(invoiceId)
                        }
                    },
                    enabled = !uiState.isSubmitting && uiState.cartItems.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save & Generate Invoice (${CurrencyFormatter.format(uiState.grandTotal, uiState.currencySymbol)})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Custom Item Dialog
    if (showCustomItemDialog) {
        var customName by remember { mutableStateOf("") }
        var customPrice by remember { mutableStateOf("") }
        var customQty by remember { mutableStateOf("1") }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showCustomItemDialog = false },
            title = { Text("Add Custom Line Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it; dialogError = null },
                        label = { Text("Item Name *") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customPrice,
                        onValueChange = { customPrice = it; dialogError = null },
                        label = { Text("Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customQty,
                        onValueChange = { customQty = it; dialogError = null },
                        label = { Text("Quantity *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    if (dialogError != null) {
                        Text(dialogError ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = customPrice.toDoubleOrNull()
                        val qty = customQty.toIntOrNull()
                        if (customName.isBlank() || price == null || price < 0.0 || qty == null || qty <= 0) {
                            dialogError = "Please enter valid item name, price and quantity"
                        } else {
                            viewModel.addCustomItem(customName, price, qty)
                            showCustomItemDialog = false
                        }
                    }
                ) {
                    Text("Add to Bill")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("Select Customer") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCustomer(customer)
                                    showCustomerPicker = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(customer.name, fontWeight = FontWeight.Bold)
                                if (customer.phone.isNotBlank()) {
                                    Text(customer.phone, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCustomerPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}
