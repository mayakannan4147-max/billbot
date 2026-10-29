package com.billbot.app.ui.screens.products

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billbot.app.data.local.entity.ProductEntity
import com.billbot.app.domain.util.CurrencyFormatter
import com.billbot.app.ui.components.AddEditProductDialog
import com.billbot.app.ui.components.BillBotSearchBar
import com.billbot.app.ui.components.ConfirmDeleteDialog
import com.billbot.app.ui.components.EmptyState
import com.billbot.app.ui.theme.Amber100
import com.billbot.app.ui.theme.Amber600
import com.billbot.app.ui.theme.Emerald100
import com.billbot.app.ui.theme.Emerald600
import com.billbot.app.ui.theme.Rose100
import com.billbot.app.ui.theme.Rose600

@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
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
                        text = "Product Inventory",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.products.size} items in catalog",
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
                placeholder = "Search by product name or SKU/code..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.filterLowStockOnly,
                    onClick = { viewModel.toggleLowStockFilter() },
                    leadingIcon = {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Low Stock Only (≤ 5)") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Products List
            if (uiState.products.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Inventory2,
                    title = "No Products Found",
                    description = if (uiState.searchQuery.isNotBlank()) "No products matched '${uiState.searchQuery}'" else "Your inventory is empty. Add items to start billing.",
                    actionButtonText = "+ Add New Product",
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            currencySymbol = uiState.currencySymbol,
                            onEdit = { productToEdit = product },
                            onDelete = { productToDelete = product }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddDialog) {
        AddEditProductDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, code, price, stock, unit ->
                viewModel.addProduct(name, code, price, stock, unit)
                showAddDialog = false
            }
        )
    }

    // Edit Product Dialog
    productToEdit?.let { product ->
        AddEditProductDialog(
            initialProduct = product,
            onDismiss = { productToEdit = null },
            onSave = { name, code, price, stock, unit ->
                viewModel.updateProduct(
                    product.copy(
                        name = name,
                        productCode = code,
                        sellingPrice = price,
                        stockQuantity = stock,
                        unit = unit
                    )
                )
                productToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { product ->
        ConfirmDeleteDialog(
            title = "Delete Product?",
            message = "Are you sure you want to delete '${product.name}'? Existing past invoices will retain their snapshot data.",
            onConfirm = {
                viewModel.deleteProduct(product)
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (stockBg, stockColor, stockLabel) = when {
        product.stockQuantity <= 0 -> Triple(Rose100, Rose600, "Out of Stock")
        product.stockQuantity <= 5 -> Triple(Amber100, Amber600, "Low: ${product.stockQuantity} ${product.unit}")
        else -> Triple(Emerald100, Emerald600, "In Stock: ${product.stockQuantity} ${product.unit}")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Code: ${product.productCode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = CurrencyFormatter.format(product.sellingPrice, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        color = stockBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stockLabel,
                            color = stockColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
