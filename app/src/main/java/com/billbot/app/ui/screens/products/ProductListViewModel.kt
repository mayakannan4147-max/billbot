package com.billbot.app.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billbot.app.data.local.entity.ProductEntity
import com.billbot.app.data.repository.ProductRepository
import com.billbot.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductListUiState(
    val products: List<ProductEntity> = emptyList(),
    val searchQuery: String = "",
    val filterLowStockOnly: Boolean = false,
    val currencySymbol: String = "₹",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProductListViewModel(
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterLowStock = MutableStateFlow(false)

    val uiState: StateFlow<ProductListUiState> = combine(
        _searchQuery.flatMapLatest { query -> productRepository.searchProducts(query) },
        _searchQuery,
        _filterLowStock,
        settingsRepository.settingsFlow
    ) { products, query, lowStockOnly, settings ->
        val filtered = if (lowStockOnly) products.filter { it.stockQuantity in 0..5 } else products
        ProductListUiState(
            products = filtered,
            searchQuery = query,
            filterLowStockOnly = lowStockOnly,
            currencySymbol = settings?.currencySymbol ?: "₹",
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductListUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleLowStockFilter() {
        _filterLowStock.update { !it }
    }

    fun addProduct(name: String, code: String, price: Double, stock: Int, unit: String) {
        viewModelScope.launch {
            try {
                val product = ProductEntity(
                    productCode = code,
                    name = name,
                    sellingPrice = price,
                    stockQuantity = stock,
                    unit = unit
                )
                productRepository.insertProduct(product)
            } catch (e: Exception) {
                // Ignore or log unique code constraint
            }
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.updateProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductListViewModel(productRepository, settingsRepository) as T
        }
    }
}
