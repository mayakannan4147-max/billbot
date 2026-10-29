package com.billbot.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billbot.app.data.local.entity.BusinessSettingsEntity
import com.billbot.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val shopName: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val gstin: String = "",
    val invoicePrefix: String = "INV-",
    val nextInvoiceSequence: Int = 1001,
    val defaultTaxRate: Double = 18.0,
    val isTaxEnabled: Boolean = true,
    val currencySymbol: String = "₹",
    val termsAndConditions: String = "",
    val isSavedSuccess: Boolean = false,
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            _uiState.update {
                it.copy(
                    shopName = settings.shopName,
                    address = settings.address,
                    phone = settings.phone,
                    email = settings.email,
                    gstin = settings.gstin,
                    invoicePrefix = settings.invoicePrefix,
                    nextInvoiceSequence = settings.nextInvoiceSequence,
                    defaultTaxRate = settings.defaultTaxRate,
                    isTaxEnabled = settings.isTaxEnabled,
                    currencySymbol = settings.currencySymbol,
                    termsAndConditions = settings.termsAndConditions,
                    isLoading = false
                )
            }
        }
    }

    fun onShopNameChange(v: String) = _uiState.update { it.copy(shopName = v, isSavedSuccess = false) }
    fun onAddressChange(v: String) = _uiState.update { it.copy(address = v, isSavedSuccess = false) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phone = v, isSavedSuccess = false) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, isSavedSuccess = false) }
    fun onGstinChange(v: String) = _uiState.update { it.copy(gstin = v, isSavedSuccess = false) }
    fun onInvoicePrefixChange(v: String) = _uiState.update { it.copy(invoicePrefix = v, isSavedSuccess = false) }
    fun onNextSequenceChange(v: Int) = _uiState.update { it.copy(nextInvoiceSequence = v, isSavedSuccess = false) }
    fun onTaxRateChange(v: Double) = _uiState.update { it.copy(defaultTaxRate = v, isSavedSuccess = false) }
    fun onTaxToggle(v: Boolean) = _uiState.update { it.copy(isTaxEnabled = v, isSavedSuccess = false) }
    fun onTermsChange(v: String) = _uiState.update { it.copy(termsAndConditions = v, isSavedSuccess = false) }

    fun saveSettings() {
        val state = _uiState.value
        viewModelScope.launch {
            val entity = BusinessSettingsEntity(
                id = 1,
                shopName = state.shopName.trim().ifBlank { "My Business" },
                address = state.address.trim(),
                phone = state.phone.trim(),
                email = state.email.trim(),
                gstin = state.gstin.trim(),
                invoicePrefix = state.invoicePrefix.trim().ifBlank { "INV-" },
                nextInvoiceSequence = maxOf(1, state.nextInvoiceSequence),
                defaultTaxRate = maxOf(0.0, state.defaultTaxRate),
                isTaxEnabled = state.isTaxEnabled,
                currencySymbol = "₹",
                termsAndConditions = state.termsAndConditions.trim()
            )
            settingsRepository.updateSettings(entity)
            _uiState.update { it.copy(isSavedSuccess = true) }
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository) as T
        }
    }
}
