package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.BillItem
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object PrintUtils {

    fun printFoodBill(
        context: Context,
        centerName: String = "আল বারাকা মেডিকেল সেন্টার",
        subtitle: String = "দৈনিক খাবার বিল",
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String = "ক্রয়কারীর স্বাক্ষর",
        approverLabel: String = "অনুমোদনকারীর স্বাক্ষর"
    ) {
        val htmlContent = generateHtmlVoucher(
            centerName = centerName,
            subtitle = subtitle,
            dateString = dateString,
            items = items,
            totalAmount = totalAmount,
            purchaserLabel = purchaserLabel,
            approverLabel = approverLabel
        )

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "Food_Bill_${dateString.replace("/", "-")}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                
                val builder = PrintAttributes.Builder()
                builder.setMediaSize(PrintAttributes.MediaSize.ISO_A5.asLandscape())
                builder.setMinMargins(PrintAttributes.Margins(0, 0, 0, 0))
                
                printManager?.print(jobName, printAdapter, builder.build())
            }
        }

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    fun shareFoodBillPdf(
        context: Context,
        centerName: String = "আল বারাকা মেডিকেল সেন্টার",
        subtitle: String = "দৈনিক খাবার বিল",
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String = "ক্রয়কারীর স্বাক্ষর",
        approverLabel: String = "অনুমোদনকারীর স্বাক্ষর"
    ) {
        try {
            val pdfDocument = PdfDocument()
            // A5 Landscape dimensions in points: 210mm x 148mm ~ 595 x 420 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 420, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paper Background
            val bgPaint = Paint().apply {
                color = Color.parseColor("#FFFDF9")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, 595f, 420f, bgPaint)

            // Outer Border
            val borderPaint = Paint().apply {
                color = Color.parseColor("#802B2B")
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val containerRect = RectF(15f, 15f, 580f, 405f)
            canvas.drawRoundRect(containerRect, 8f, 8f, borderPaint)

            // Header Banner
            val headerPaint = Paint().apply {
                color = Color.parseColor("#802B2B")
                style = Paint.Style.FILL
            }
            val headerRect = RectF(15f, 15f, 580f, 60f)
            canvas.drawRect(headerRect, headerPaint)

            // Center Name Title
            val titleTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 17f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(centerName, 297f, 38f, titleTextPaint)

            // Subtitle Text
            val subtitleTextPaint = Paint().apply {
                color = Color.parseColor("#F5EBE6")
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(subtitle, 297f, 52f, subtitleTextPaint)

            // Date Row (Top Right)
            val dateTextPaint = Paint().apply {
                color = Color.parseColor("#2C1810")
                textSize = 12f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            val bnDate = BengaliUtils.toBengaliDigits(dateString)
            canvas.drawText("তারিখ: $bnDate", 565f, 80f, dateTextPaint)

            // Line Divider below Date
            val linePaint = Paint().apply {
                color = Color.parseColor("#802B2B")
                strokeWidth = 1f
            }
            canvas.drawLine(25f, 88f, 570f, 88f, linePaint)

            // Item Paints
            val bulletPaint = Paint().apply {
                color = Color.parseColor("#802B2B")
                textSize = 13f
                isFakeBoldText = true
            }
            val itemTextPaint = Paint().apply {
                color = Color.parseColor("#2C1810")
                textSize = 12f
                isFakeBoldText = true
            }
            val qtyTextPaint = Paint().apply {
                color = Color.parseColor("#5C1F1F")
                textSize = 11f
            }
            val amountTextPaint = Paint().apply {
                color = Color.parseColor("#2C1810")
                textSize = 12f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            val dotLeaderPaint = Paint().apply {
                color = Color.parseColor("#B0A090")
                textSize = 10f
            }

            var currentY = 112f
            val validItems = items.filter { it.name.isNotBlank() || it.amount > 0 }

            validItems.forEach { item ->
                // Bullet
                canvas.drawText("*", 30f, currentY, bulletPaint)

                // Item Name
                canvas.drawText(item.name, 45f, currentY, itemTextPaint)

                val nameWidth = itemTextPaint.measureText(item.name)
                var startLeaderX = 50f + nameWidth

                // Quantity (if present)
                if (item.quantity.isNotBlank()) {
                    val qtyText = "  (${BengaliUtils.toBengaliDigits(item.quantity)})"
                    canvas.drawText(qtyText, startLeaderX, currentY, qtyTextPaint)
                    startLeaderX += qtyTextPaint.measureText(qtyText) + 8f
                } else {
                    startLeaderX += 12f
                }

                // Price text with /- suffix
                val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(DecimalFormat("#,##0").format(item.amount))}/-"
                val priceWidth = amountTextPaint.measureText(bnAmount)
                val endLeaderX = 565f - priceWidth - 10f

                // Draw Leader Dots / Lines (--------------------)
                if (endLeaderX > startLeaderX + 15f) {
                    canvas.drawLine(startLeaderX, currentY - 4f, endLeaderX, currentY - 4f, Paint().apply {
                        color = Color.parseColor("#C8B8A8")
                        strokeWidth = 0.8f
                    })
                }

                // Price on Right
                canvas.drawText(bnAmount, 565f, currentY, amountTextPaint)

                currentY += 22f
            }

            // Total Amount Section
            val totalY = 328f
            canvas.drawLine(350f, totalY - 12f, 565f, totalY - 12f, linePaint)

            val totalTextPaint = Paint().apply {
                color = Color.parseColor("#802B2B")
                textSize = 15f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            val bnTotal = "${BengaliUtils.formatBengaliCurrency(totalAmount)}/-"
            canvas.drawText("মোট ➔ $bnTotal", 565f, totalY, totalTextPaint)

            // Signatures
            val sigLinePaint = Paint().apply {
                color = Color.parseColor("#802B2B")
                strokeWidth = 1f
            }
            val sigTextPaint = Paint().apply {
                color = Color.parseColor("#3C2A21")
                textSize = 11f
                textAlign = Paint.Align.CENTER
            }

            // Purchaser Sig
            canvas.drawLine(40f, 375f, 180f, 375f, sigLinePaint)
            canvas.drawText(purchaserLabel, 110f, 388f, sigTextPaint)

            // Approver Sig
            canvas.drawLine(415f, 375f, 555f, 375f, sigLinePaint)
            canvas.drawText(approverLabel, 485f, 388f, sigTextPaint)

            pdfDocument.finishPage(page)

            val cacheDir = File(context.cacheDir, "food_bills").apply { mkdirs() }
            val pdfFile = File(cacheDir, "Food_Bill_${dateString.replace("/", "-")}.pdf")
            if (pdfFile.exists()) pdfFile.delete()

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            sharePdfFile(context, pdfFile, dateString)

        } catch (e: Exception) {
            Toast.makeText(context, "পিডিএফ ফাইল তৈরি ব্যর্থ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePdfFile(context: Context, pdfFile: File, dateString: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "খাবার বিল - $dateString")
                putExtra(Intent.EXTRA_TEXT, "আল বারাকা মেডিকেল সেন্টার - $dateString তারিখের খাবার বিল।")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "পিডিএফ শেয়ার করুন (WhatsApp / আদার্স)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateHtmlVoucher(
        centerName: String,
        subtitle: String,
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String,
        approverLabel: String
    ): String {
        val bengaliDate = BengaliUtils.toBengaliDigits(dateString)
        val bengaliTotal = "${BengaliUtils.formatBengaliCurrency(totalAmount)}/-"

        val listHtml = StringBuilder()
        items.forEach { item ->
            if (item.name.isNotBlank() || item.amount > 0) {
                val bnQty = if (item.quantity.isNotBlank()) " (${BengaliUtils.toBengaliDigits(item.quantity)})" else ""
                val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(DecimalFormat("#,##0").format(item.amount))}/-"

                listHtml.append("""
                    <div class="item-row">
                        <span class="bullet">*</span>
                        <span class="item-name">${item.name}<span class="item-qty">$bnQty</span></span>
                        <span class="leader"></span>
                        <span class="item-price">$bnAmount</span>
                    </div>
                """.trimIndent())
            }
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    @page {
                        size: 210mm 148mm;
                        margin: 4mm;
                    }
                    body {
                        font-family: 'SolaimanLipi', 'Kalpurush', 'Noto Sans Bengali', Arial, sans-serif;
                        margin: 0;
                        padding: 0;
                        background-color: #FFFDF9;
                        color: #2C1810;
                        -webkit-print-color-adjust: exact;
                        print-color-adjust: exact;
                    }
                    .container {
                        width: 100%;
                        max-width: 202mm;
                        min-height: 138mm;
                        margin: 0 auto;
                        border: 2px solid #802B2B;
                        border-radius: 6px;
                        background: #FFFDF9;
                        padding: 0 0 10px 0;
                        box-sizing: border-box;
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                    }
                    .header {
                        background-color: #802B2B;
                        color: #FFFFFF;
                        text-align: center;
                        padding: 10px 8px 8px 8px;
                        border-top-left-radius: 4px;
                        border-top-right-radius: 4px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 20px;
                        font-weight: bold;
                        letter-spacing: 0.5px;
                    }
                    .header p {
                        margin: 2px 0 0 0;
                        font-size: 13px;
                        opacity: 0.95;
                    }
                    .meta-info {
                        display: flex;
                        justify-content: flex-end;
                        align-items: center;
                        padding: 8px 18px 6px 18px;
                        font-size: 14px;
                        font-weight: bold;
                        color: #2C1810;
                        border-bottom: 1.5px solid #802B2B;
                    }
                    .items-container {
                        padding: 12px 20px;
                        flex-grow: 1;
                    }
                    .item-row {
                        display: flex;
                        align-items: baseline;
                        margin-bottom: 10px;
                        font-size: 15px;
                    }
                    .bullet {
                        color: #802B2B;
                        font-weight: bold;
                        font-size: 16px;
                        margin-right: 8px;
                    }
                    .item-name {
                        font-weight: bold;
                        color: #2C1810;
                        white-space: nowrap;
                    }
                    .item-qty {
                        font-weight: normal;
                        color: #5C1F1F;
                        font-size: 13px;
                    }
                    .leader {
                        flex-grow: 1;
                        border-bottom: 1px dashed #C8B8A8;
                        margin: 0 12px;
                        height: 1px;
                        align-self: center;
                    }
                    .item-price {
                        font-weight: bold;
                        color: #2C1810;
                        white-space: nowrap;
                        font-size: 15px;
                    }
                    .total-container {
                        text-align: right;
                        padding: 10px 20px 6px 16px;
                        font-size: 18px;
                        font-weight: bold;
                        color: #802B2B;
                        border-top: 1.5px solid #802B2B;
                        margin-top: 10px;
                    }
                    .signatures {
                        display: flex;
                        justify-content: space-between;
                        margin-top: 25px;
                        padding: 0 20px;
                    }
                    .sig-box {
                        width: 38%;
                        text-align: center;
                    }
                    .sig-line {
                        border-top: 1px solid #5C1F1F;
                        margin-bottom: 4px;
                    }
                    .sig-title {
                        font-size: 12px;
                        color: #3C2A21;
                        font-weight: 600;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>$centerName</h1>
                        <p>$subtitle</p>
                    </div>
                    <div class="meta-info">
                        <div>তারিখ: $bengaliDate</div>
                    </div>

                    <div class="items-container">
                        $listHtml
                    </div>

                    <div class="total-container">
                        মোট ➔ $bengaliTotal
                    </div>

                    <div class="signatures">
                        <div class="sig-box">
                            <div class="sig-line"></div>
                            <div class="sig-title">$purchaserLabel</div>
                        </div>
                        <div class="sig-box">
                            <div class="sig-line"></div>
                            <div class="sig-title">$approverLabel</div>
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
