package com.billbot.app.ui.screens.invoices

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billbot.app.data.local.entity.BusinessSettingsEntity
import com.billbot.app.data.local.relation.InvoiceWithItems
import com.billbot.app.data.repository.InvoiceRepository
import com.billbot.app.data.repository.SettingsRepository
import com.billbot.app.domain.model.PaymentMethod
import com.billbot.app.pdf.InvoiceSharer
import com.billbot.app.pdf.PdfInvoiceGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class InvoiceDetailUiState(
    val invoiceWithItems: InvoiceWithItems? = null,
    val settings: BusinessSettingsEntity = BusinessSettingsEntity(),
    val isGeneratingPdf: Boolean = false,
    val generatedPdfFile: File? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class InvoiceDetailViewModel(
    private val invoiceId: Long,
    private val invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository,
    private val pdfGenerator: PdfInvoiceGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceDetailUiState())
    val uiState: StateFlow<InvoiceDetailUiState> = _uiState.asStateFlow()

    init {
        loadInvoice()
    }

    fun loadInvoice() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val invoice = invoiceRepository.getInvoiceById(invoiceId)
            val settings = settingsRepository.getSettings()
            _uiState.update {
                it.copy(
                    invoiceWithItems = invoice,
                    settings = settings,
                    isLoading = false
                )
            }
        }
    }

    fun recordPayment(amount: Double, method: PaymentMethod) {
        viewModelScope.launch {
            invoiceRepository.recordPayment(invoiceId, amount, method)
            loadInvoice()
        }
    }

    fun sharePdf(context: Context) {
        val invoice = _uiState.value.invoiceWithItems ?: return
        val settings = _uiState.value.settings
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true) }
            try {
                val pdfFile = pdfGenerator.generateInvoicePdf(invoice, settings)
                _uiState.update { it.copy(isGeneratingPdf = false, generatedPdfFile = pdfFile) }
                InvoiceSharer.shareInvoicePdf(context, pdfFile, invoice.invoice.invoiceNumber)
            } catch (e: Exception) {
                _uiState.update { it.copy(isGeneratingPdf = false, errorMessage = "Failed to generate PDF: ${e.localizedMessage}") }
            }
        }
    }

    fun viewPdf(context: Context) {
        val invoice = _uiState.value.invoiceWithItems ?: return
        val settings = _uiState.value.settings
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true) }
            try {
                val pdfFile = pdfGenerator.generateInvoicePdf(invoice, settings)
                _uiState.update { it.copy(isGeneratingPdf = false, generatedPdfFile = pdfFile) }
                InvoiceSharer.viewInvoicePdf(context, pdfFile)
            } catch (e: Exception) {
                _uiState.update { it.copy(isGeneratingPdf = false, errorMessage = "Failed to open PDF: ${e.localizedMessage}") }
            }
        }
    }

    class Factory(
        private val invoiceId: Long,
        private val invoiceRepository: InvoiceRepository,
        private val settingsRepository: SettingsRepository,
        private val pdfGenerator: PdfInvoiceGenerator
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InvoiceDetailViewModel(invoiceId, invoiceRepository, settingsRepository, pdfGenerator) as T
        }
    }
}
