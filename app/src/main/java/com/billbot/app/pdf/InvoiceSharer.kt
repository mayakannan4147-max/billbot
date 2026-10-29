package com.billbot.app.pdf

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object InvoiceSharer {

    /**
     * Opens the native Android share sheet to share the generated invoice PDF.
     */
    fun shareInvoicePdf(context: Context, pdfFile: File, invoiceNumber: String) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice $invoiceNumber")
            putExtra(Intent.EXTRA_TEXT, "Here is your invoice $invoiceNumber from BillBot.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Invoice $invoiceNumber via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Opens the PDF in a viewer app installed on the device.
     */
    fun viewInvoicePdf(context: Context, pdfFile: File) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(viewIntent, "Open Invoice PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
