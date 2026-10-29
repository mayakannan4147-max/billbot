package com.billbot.app.ui.screens.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billbot.app.data.local.entity.CustomerEntity
import com.billbot.app.data.local.entity.InvoiceEntity
import com.billbot.app.data.local.entity.InvoiceItemEntity
import com.billbot.app.data.local.entity.ProductEntity
import com.billbot.app.data.repository.CustomerRepository
import com.billbot.app.data.repository.InvoiceRepository
import com.billbot.app.data.repository.ProductRepository
import com.billbot.app.data.repository.SettingsRepository
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.domain.model.PaymentStatus
import com.billbot.app.domain.util.BillingCalculator
import com.billbot.app.domain.util.InvoiceNumberGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CartLineItem(
    val tempId: String = UUID.randomUUID().toString(),
    val productId: Long? = null,
    val productCode: String = "",
    val productName: String,
    val unitPrice: Double,
    val quantity: Int = 1,
    val availableStock: Int? = null
) {
    val totalAmount: Double
        get() = BillingCalculator.calculateItemTotal(unitPrice, quantity)
}

data class CreateInvoiceUiState(
    val isWalkIn: Boolean = true,
    val selectedCustomer: CustomerEntity? = null,
    val walkInName: String = "Walk-in Customer",
    val walkInPhone: String = "",
    val cartItems: List<CartLineItem> = emptyList(),
    val discountPercent: Double = 0.0,
    val taxRate: Double = 18.0,
    val isTaxEnabled: Boolean = true,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paidAmountText: String = "",
    val notes: String = "",
    val currencySymbol: String = "₹",
    val nextInvoiceNumberPreview: String = "INV-1001",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val subtotal: Double
        get() = BillingCalculator.calculateSubtotal(
            cartItems.map {
                InvoiceItemEntity(
                    productName = it.productName,
                    unitPrice = it.unitPrice,
                    quantity = it.quantity,
                    totalAmount = it.totalAmount
                )
            }
        )

    val discountAmount: Double
        get() = BillingCalculator.calculateDiscountAmount(subtotal, discountPercent)

    val taxAmount: Double
        get() = if (isTaxEnabled) BillingCalculator.calculateTaxAmount(subtotal - discountAmount, taxRate) else 0.0

    val grandTotal: Double
        get() = BillingCalculator.calculateGrandTotal(subtotal, discountAmount, taxAmount)

    val paidAmount: Double
        get() = paidAmountText.toDoubleOrNull() ?: 0.0

    val balanceAmount: Double
        get() = BillingCalculator.calculateBalance(grandTotal, paidAmount)

    val paymentStatus: PaymentStatus
        get() = BillingCalculator.determinePaymentStatus(grandTotal, paidAmount)
}

class CreateInvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateInvoiceUiState())
    val uiState: StateFlow<CreateInvoiceUiState> = _uiState.asStateFlow()

    val availableProducts: StateFlow<List<ProductEntity>> = productRepository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCustomers: StateFlow<List<CustomerEntity>> = customerRepository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            _uiState.update { current ->
                current.copy(
                    taxRate = settings.defaultTaxRate,
                    isTaxEnabled = settings.isTaxEnabled,
                    currencySymbol = settings.currencySymbol,
                    nextInvoiceNumberPreview = InvoiceNumberGenerator.generateInvoiceNumber(
                        prefix = settings.invoicePrefix,
                        sequence = settings.nextInvoiceSequence
                    )
                )
            }
        }
    }

    fun setWalkIn(isWalkIn: Boolean) {
        _uiState.update { it.copy(isWalkIn = isWalkIn, selectedCustomer = if (isWalkIn) null else it.selectedCustomer) }
    }

    fun setCustomer(customer: CustomerEntity?) {
        _uiState.update { it.copy(selectedCustomer = customer, isWalkIn = customer == null) }
    }

    fun setWalkInName(name: String) {
        _uiState.update { it.copy(walkInName = name) }
    }

    fun setWalkInPhone(phone: String) {
        _uiState.update { it.copy(walkInPhone = phone) }
    }

    fun addProductToCart(product: ProductEntity) {
        _uiState.update { current ->
            val existingIndex = current.cartItems.indexOfFirst { it.productId == product.id }
            val updatedItems = if (existingIndex != -1) {
                current.cartItems.mapIndexed { index, item ->
                    if (index == existingIndex) {
                        item.copy(quantity = item.quantity + 1)
                    } else item
                }
            } else {
                current.cartItems + CartLineItem(
                    productId = product.id,
                    productCode = product.productCode,
                    productName = product.name,
                    unitPrice = product.sellingPrice,
                    quantity = 1,
                    availableStock = product.stockQuantity
                )
            }
            current.copy(cartItems = updatedItems, errorMessage = null)
        }
    }

    fun addCustomItem(name: String, price: Double, quantity: Int) {
        if (name.isBlank() || price < 0.0 || quantity <= 0) return
        _uiState.update { current ->
            val newItem = CartLineItem(
                productName = name.trim(),
                unitPrice = price,
                quantity = quantity
            )
            current.copy(cartItems = current.cartItems + newItem, errorMessage = null)
        }
    }

    fun updateQuantity(tempId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(tempId)
            return
        }
        _uiState.update { current ->
            current.copy(
                cartItems = current.cartItems.map {
                    if (it.tempId == tempId) it.copy(quantity = newQuantity) else it
                }
            )
        }
    }

    fun updatePrice(tempId: String, newPrice: Double) {
        if (newPrice < 0.0) return
        _uiState.update { current ->
            current.copy(
                cartItems = current.cartItems.map {
                    if (it.tempId == tempId) it.copy(unitPrice = newPrice) else it
                }
            )
        }
    }

    fun removeItem(tempId: String) {
        _uiState.update { current ->
            current.copy(cartItems = current.cartItems.filterNot { it.tempId == tempId })
        }
    }

    fun setDiscountPercent(percent: Double) {
        _uiState.update { it.copy(discountPercent = percent.coerceIn(0.0, 100.0)) }
    }

    fun setTaxRate(taxRate: Double) {
        _uiState.update { it.copy(taxRate = taxRate.coerceAtLeast(0.0)) }
    }

    fun toggleTaxEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isTaxEnabled = enabled) }
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun setPaidAmount(text: String) {
        _uiState.update { it.copy(paidAmountText = text) }
    }

    fun setPayFull() {
        val total = _uiState.value.grandTotal
        _uiState.update { it.copy(paidAmountText = total.toString()) }
    }

    fun setPayZero() {
        _uiState.update { it.copy(paidAmountText = "0") }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun submitInvoice(onSuccess: (Long) -> Unit) {
        val state = _uiState.value
        if (state.isSubmitting) return

        if (state.cartItems.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please add at least one product to the invoice") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val settings = settingsRepository.getSettings()
                val invoiceNumber = InvoiceNumberGenerator.generateInvoiceNumber(
                    prefix = settings.invoicePrefix,
                    sequence = settings.nextInvoiceSequence
                )

                val customerName = if (state.isWalkIn) {
                    state.walkInName.ifBlank { "Walk-in Customer" }
                } else {
                    state.selectedCustomer?.name ?: "Customer"
                }

                val customerPhone = if (state.isWalkIn) {
                    state.walkInPhone
                } else {
                    state.selectedCustomer?.phone ?: ""
                }

                val invoiceEntity = InvoiceEntity(
                    invoiceNumber = invoiceNumber,
                    customerId = if (state.isWalkIn) null else state.selectedCustomer?.id,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    subtotal = state.subtotal,
                    discountPercent = state.discountPercent,
                    discountAmount = state.discountAmount,
                    taxRate = if (state.isTaxEnabled) state.taxRate else 0.0,
                    taxAmount = state.taxAmount,
                    grandTotal = state.grandTotal,
                    paidAmount = state.paidAmount,
                    balanceAmount = state.balanceAmount,
                    paymentStatus = state.paymentStatus,
                    paymentMethod = state.selectedPaymentMethod,
                    notes = state.notes
                )

                val snapshotItems = state.cartItems.map { cartItem ->
                    InvoiceItemEntity(
                        productId = cartItem.productId,
                        productCode = cartItem.productCode,
                        productName = cartItem.productName,
                        unitPrice = cartItem.unitPrice,
                        quantity = cartItem.quantity,
                        totalAmount = cartItem.totalAmount
                    )
                }

                val invoiceId = invoiceRepository.createInvoice(invoiceEntity, snapshotItems)
                _uiState.update { it.copy(isSubmitting = false) }
                onSuccess(invoiceId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Failed to create invoice: ${e.localizedMessage}") }
            }
        }
    }

    class Factory(
        private val invoiceRepository: InvoiceRepository,
        private val productRepository: ProductRepository,
        private val customerRepository: CustomerRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateInvoiceViewModel(
                invoiceRepository,
                productRepository,
                customerRepository,
                settingsRepository
            ) as T
        }
    }
}
