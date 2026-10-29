package com.billbot.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.billbot.app.data.local.entity.CustomerEntity
import com.billbot.app.data.local.entity.ProductEntity
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.util.CurrencyFormatter

@Composable
fun AddEditProductDialog(
    initialProduct: ProductEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, price: Double, stock: Int, unit: String) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var code by remember { mutableStateOf(initialProduct?.productCode ?: "") }
    var priceText by remember { mutableStateOf(initialProduct?.sellingPrice?.toString() ?: "") }
    var stockText by remember { mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "10") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "pcs") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) "Add New Product" else "Edit Product",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it; errorMessage = null },
                    label = { Text("Product Code / SKU *") },
                    placeholder = { Text("e.g. PRD-101") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it; errorMessage = null },
                        label = { Text("Selling Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it; errorMessage = null },
                        label = { Text("Stock Qty *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g. pcs, kg, litre, pack)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    val stock = stockText.toIntOrNull()

                    when {
                        name.isBlank() -> errorMessage = "Product name cannot be empty"
                        code.isBlank() -> errorMessage = "Product code cannot be empty"
                        price == null || price < 0.0 -> errorMessage = "Enter a valid, non-negative price"
                        stock == null || stock < 0 -> errorMessage = "Enter a valid stock quantity"
                        else -> {
                            onSave(name.trim(), code.trim(), price, stock, unit.trim().ifBlank { "pcs" })
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditCustomerDialog(
    initialCustomer: CustomerEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var email by remember { mutableStateOf(initialCustomer?.email ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCustomer == null) "Add Customer" else "Edit Customer",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Customer name is required"
                    } else {
                        onSave(name.trim(), phone.trim(), email.trim(), address.trim())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    invoiceNumber: String,
    grandTotal: Double,
    currentPaid: Double,
    balance: Double,
    onDismiss: () -> Unit,
    onRecordPayment: (amount: Double, method: PaymentMethod) -> Unit
) {
    var amountText by remember { mutableStateOf(balance.toString()) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Record Payment for $invoiceNumber", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Invoice:", style = MaterialTheme.typography.bodyMedium)
                    Text(CurrencyFormatter.format(grandTotal), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Already Paid:", style = MaterialTheme.typography.bodyMedium)
                    Text(CurrencyFormatter.format(currentPaid), color = MaterialTheme.colorScheme.secondary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Outstanding Balance:", style = MaterialTheme.typography.bodyMedium)
                    Text(CurrencyFormatter.format(balance), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMessage = null },
                    label = { Text("Payment Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { amountText = balance.toString() }) {
                        Text("Pay Full Balance (${CurrencyFormatter.format(balance)})")
                    }
                }

                Text("Payment Method:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(PaymentMethod.CASH, PaymentMethod.UPI, PaymentMethod.CARD).forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method.displayName) }
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    when {
                        amount == null || amount <= 0.0 -> errorMessage = "Enter a valid positive payment amount"
                        amount > balance + 0.01 -> errorMessage = "Amount cannot exceed outstanding balance"
                        else -> onRecordPayment(amount, selectedMethod)
                    }
                }
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
