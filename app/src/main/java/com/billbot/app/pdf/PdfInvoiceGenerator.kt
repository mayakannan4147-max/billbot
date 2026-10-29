package com.billbot.app.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.billbot.app.data.local.entity.BusinessSettingsEntity
import com.billbot.app.data.local.relation.InvoiceWithItems
import com.billbot.app.domain.model.PaymentStatus
import com.billbot.app.domain.util.CurrencyFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfInvoiceGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    /**
     * Generates a professional A4 PDF invoice and returns the saved File.
     */
    fun generateInvoicePdf(
        invoiceWithItems: InvoiceWithItems,
        settings: BusinessSettingsEntity
    ): File {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 dimensions
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawInvoice(canvas, invoiceWithItems, settings)

        pdfDocument.finishPage(page)

        // Save PDF to cache invoices directory
        val outputDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val cleanNumber = invoice.invoiceNumber.replace("[^a-zA-Z0-9-]".toRegex(), "_")
        val outputFile = File(outputDir, "$cleanNumber.pdf")

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawInvoice(
        canvas: Canvas,
        invoiceWithItems: InvoiceWithItems,
        settings: BusinessSettingsEntity
    ) {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        val paint = Paint().apply { isAntiAlias = true }

        // --- 1. Header Banner ---
        paint.color = Color.parseColor("#0F172A") // Deep slate
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Shop Name
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(settings.shopName, 36f, 40f, paint)

        // Shop Details
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("${settings.address} | Phone: ${settings.phone}", 36f, 60f, paint)
        canvas.drawText("GSTIN: ${settings.gstin} | Email: ${settings.email}", 36f, 75f, paint)

        // "TAX INVOICE" Badge in top right
        paint.color = Color.parseColor("#3B82F6") // Blue accent
        val badgeRect = RectF(440f, 25f, 555f, 55f)
        canvas.drawRoundRect(badgeRect, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TAX INVOICE", 456f, 44f, paint)

        // --- 2. Invoice & Customer Meta Section ---
        var currentY = 125f

        // Billed To Box
        paint.color = Color.parseColor("#F1F5F9") // Light gray container
        val customerCardRect = RectF(36f, currentY, 320f, currentY + 70f)
        canvas.drawRoundRect(customerCardRect, 8f, 8f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BILLED TO:", 48f, currentY + 20f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 13f
        canvas.drawText(invoice.customerName.ifBlank { "Walk-in Customer" }, 48f, currentY + 40f, paint)

        if (invoice.customerPhone.isNotBlank()) {
            paint.color = Color.parseColor("#475569")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Phone: ${invoice.customerPhone}", 48f, currentY + 58f, paint)
        }

        // Invoice Meta (Number, Date, Status) on right side
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INVOICE NO:", 360f, currentY + 18f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 12f
        canvas.drawText(invoice.invoiceNumber, 440f, currentY + 18f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        canvas.drawText("DATE & TIME:", 360f, currentY + 36f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(dateFormat.format(Date(invoice.createdAt)), 440f, currentY + 36f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PAYMENT MODE:", 360f, currentY + 54f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        canvas.drawText(invoice.paymentMethod.displayName, 440f, currentY + 54f, paint)

        // Status Badge
        val (statusBg, statusText) = when (invoice.paymentStatus) {
            PaymentStatus.PAID -> Color.parseColor("#DCFCE7") to "#15803D"
            PaymentStatus.PARTIAL -> Color.parseColor("#FFEDD5") to "#C2410C"
            PaymentStatus.UNPAID -> Color.parseColor("#FEE2E2") to "#B91C1C"
        }
        val statusRect = RectF(360f, currentY + 62f, 470f, currentY + 80f)
        paint.color = statusBg
        canvas.drawRoundRect(statusRect, 4f, 4f, paint)

        paint.color = Color.parseColor(statusText)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(invoice.paymentStatus.displayName.uppercase(), 370f, currentY + 75f, paint)

        currentY += 105f

        // --- 3. Items Table Header ---
        paint.color = Color.parseColor("#1E293B")
        val tableHeaderRect = RectF(36f, currentY, 559f, currentY + 24f)
        canvas.drawRoundRect(tableHeaderRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText("#", 48f, currentY + 16f, paint)
        canvas.drawText("ITEM DESCRIPTION", 75f, currentY + 16f, paint)
        canvas.drawText("RATE", 350f, currentY + 16f, paint)
        canvas.drawText("QTY", 440f, currentY + 16f, paint)
        canvas.drawText("TOTAL", 500f, currentY + 16f, paint)

        currentY += 28f

        // --- 4. Items Table Rows ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var index = 1

        for (item in items) {
            // Row divider
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawLine(36f, currentY + 16f, 559f, currentY + 16f, paint)

            paint.color = Color.parseColor("#334155")
            paint.textSize = 9f
            canvas.drawText(index.toString(), 48f, currentY + 10f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val displayName = if (item.productCode.isNotBlank()) "${item.productName} (${item.productCode})" else item.productName
            canvas.drawText(displayName.take(35), 75f, currentY + 10f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.parseColor("#334155")
            canvas.drawText(CurrencyFormatter.format(item.unitPrice, settings.currencySymbol), 350f, currentY + 10f, paint)
            canvas.drawText(item.quantity.toString(), 445f, currentY + 10f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.parseColor("#0F172A")
            canvas.drawText(CurrencyFormatter.format(item.totalAmount, settings.currencySymbol), 500f, currentY + 10f, paint)

            currentY += 24f
            index++

            if (currentY > 650f) break // Protect page overflow
        }

        currentY += 16f

        // --- 5. Calculation Summary Section ---
        val summaryBoxLeft = 320f
        paint.color = Color.parseColor("#F8FAFC")
        val summaryCardRect = RectF(summaryBoxLeft, currentY, 559f, currentY + 130f)
        canvas.drawRoundRect(summaryCardRect, 8f, 8f, paint)

        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(summaryCardRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        var sumY = currentY + 20f

        // Subtotal
        drawSummaryRow(canvas, paint, "Subtotal:", CurrencyFormatter.format(invoice.subtotal, settings.currencySymbol), summaryBoxLeft + 15f, 545f, sumY, false)
        sumY += 18f

        // Discount
        if (invoice.discountAmount > 0) {
            drawSummaryRow(canvas, paint, "Discount (${invoice.discountPercent}%):", "- ${CurrencyFormatter.format(invoice.discountAmount, settings.currencySymbol)}", summaryBoxLeft + 15f, 545f, sumY, false)
            sumY += 18f
        }

        // Tax (GST)
        if (invoice.taxAmount > 0) {
            drawSummaryRow(canvas, paint, "GST / Tax (${invoice.taxRate}%):", "+ ${CurrencyFormatter.format(invoice.taxAmount, settings.currencySymbol)}", summaryBoxLeft + 15f, 545f, sumY, false)
            sumY += 18f
        }

        // Divider
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(summaryBoxLeft + 10f, sumY, 550f, sumY, paint)
        sumY += 16f

        // Grand Total
        drawSummaryRow(canvas, paint, "Grand Total:", CurrencyFormatter.format(invoice.grandTotal, settings.currencySymbol), summaryBoxLeft + 15f, 545f, sumY, true)
        sumY += 20f

        // Paid & Balance
        drawSummaryRow(canvas, paint, "Paid Amount:", CurrencyFormatter.format(invoice.paidAmount, settings.currencySymbol), summaryBoxLeft + 15f, 545f, sumY, false)
        sumY += 16f
        drawSummaryRow(canvas, paint, "Balance Due:", CurrencyFormatter.format(invoice.balanceAmount, settings.currencySymbol), summaryBoxLeft + 15f, 545f, sumY, invoice.balanceAmount > 0)

        // --- 6. Notes & Terms Section ---
        paint.color = Color.parseColor("#475569")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        if (invoice.notes.isNotBlank()) {
            canvas.drawText("Notes: ${invoice.notes}", 36f, currentY + 25f, paint)
        }

        canvas.drawText("Terms & Conditions:", 36f, 755f, paint)
        paint.textSize = 7.5f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText(settings.termsAndConditions, 36f, 770f, paint)

        // Footer
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Thank you for your business! Powered by BillBot", 185f, 815f, paint)
    }

    private fun drawSummaryRow(
        canvas: Canvas,
        paint: Paint,
        label: String,
        value: String,
        leftX: Float,
        rightX: Float,
        y: Float,
        isBold: Boolean
    ) {
        paint.textSize = if (isBold) 11f else 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
        paint.color = if (isBold) Color.parseColor("#0F172A") else Color.parseColor("#475569")
        canvas.drawText(label, leftX, y, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, rightX, y, paint)
        paint.textAlign = Paint.Align.LEFT
    }
}
