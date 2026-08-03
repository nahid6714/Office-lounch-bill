package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.BillItem
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

enum class PrintPosition(val label: String, val description: String) {
    TOP("উপরে (Top)", "A4 কাগজের উপরের অর্ধেকাংশে"),
    BOTTOM("নিচে (Bottom)", "A4 কাগজের নিচের অর্ধেকাংশে"),
    BOTH("উভয় অংশ (2 Copies)", "A4 পাতায় ২টি কপি একসাথে (উপরে ও নিচে)")
}

object PrintUtils {

    fun printFoodBill(
        context: Context,
        centerName: String = "আল বারাকা মেডিকেল সেন্টার",
        subtitle: String = "দৈনিক খাবার বিল",
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String = "ক্রয়কারীর স্বাক্ষর",
        approverLabel: String = "অনুমোদনকারীর স্বাক্ষর",
        position: PrintPosition = PrintPosition.TOP
    ) {
        val htmlContent = generateHtmlVoucher(
            centerName = centerName,
            subtitle = subtitle,
            dateString = dateString,
            items = items,
            totalAmount = totalAmount,
            purchaserLabel = purchaserLabel,
            approverLabel = approverLabel,
            position = position
        )

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "Food_Bill_${dateString.replace("/", "-")}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                
                val builder = PrintAttributes.Builder()
                builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
                builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                
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
        approverLabel: String = "অনুমোদনকারীর স্বাক্ষর",
        position: PrintPosition = PrintPosition.TOP
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val bgPaint = Paint().apply {
                color = Color.parseColor("#FFFDF9")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

            when (position) {
                PrintPosition.TOP -> {
                    drawSingleVoucherOnCanvas(canvas, 15f, centerName, subtitle, dateString, items, totalAmount, purchaserLabel)
                }
                PrintPosition.BOTTOM -> {
                    drawSingleVoucherOnCanvas(canvas, 430f, centerName, subtitle, dateString, items, totalAmount, purchaserLabel)
                }
                PrintPosition.BOTH -> {
                    drawSingleVoucherOnCanvas(canvas, 15f, centerName, subtitle, dateString, items, totalAmount, purchaserLabel)
                    drawSingleVoucherOnCanvas(canvas, 430f, centerName, subtitle, dateString, items, totalAmount, purchaserLabel)
                }
            }

            pdfDocument.finishPage(page)

            val cacheDir = File(context.cacheDir, "food_bills").apply { mkdirs() }
            val pdfFile = File(cacheDir, "Food_Bill_${dateString.replace("/", "-")}.pdf")
            if (pdfFile.exists()) pdfFile.delete()

            FileOutputStream(pdfFile).use { out -> pdfDocument.writeTo(out) }
            pdfDocument.close()

            sharePdfFile(context, pdfFile, dateString)
        } catch (e: Exception) {
            Toast.makeText(context, "পিডিএফ ফাইল তৈরি ব্যর্থ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawSingleVoucherOnCanvas(
        canvas: android.graphics.Canvas,
        startY: Float,
        centerName: String,
        subtitle: String,
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String
    ) {
        val maroonColor = Color.parseColor("#701B1B")

        canvas.save()
        val localStartY = startY

        // Header Banner
        val headerPaint = Paint().apply {
            color = maroonColor
            style = Paint.Style.FILL
        }
        val headerRect = RectF(15f, localStartY, 565f, localStartY + 44f)
        canvas.drawRect(headerRect, headerPaint)

        // Title
        val titleTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 19f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(centerName, 290f, localStartY + 25f, titleTextPaint)

        // Subtitle
        val subtitleTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle, 290f, localStartY + 39f, subtitleTextPaint)

        // Sawtooth Teeth Bar
        val toothWidth = 10f
        val toothHeight = 5f
        var toothX = 15f
        val toothPaint = Paint().apply {
            color = maroonColor
            style = Paint.Style.FILL
        }
        while (toothX < 565f) {
            val path = android.graphics.Path().apply {
                moveTo(toothX, localStartY + 44f)
                lineTo(toothX + toothWidth / 2, localStartY + 44f + toothHeight)
                lineTo(toothX + toothWidth, localStartY + 44f)
                close()
            }
            canvas.drawPath(path, toothPaint)
            toothX += toothWidth
        }

        // Metadata Row (Only Date, No Bill Number)
        val metaPaintRight = Paint().apply {
            color = maroonColor
            textSize = 11f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val bnDate = BengaliUtils.toBengaliDigits(dateString)
        canvas.drawText("তারিখ : $bnDate", 565f, localStartY + 62f, metaPaintRight)

        // TABLE GRID CONFIG
        val tableLeft = 15f
        val tableRight = 565f
        val tableTop = localStartY + 70f
        val colWidths = floatArrayOf(45f, 240f, 90f, 65f, 110f) // Total width = 550f

        // Table Header Background
        val tableHeaderRect = RectF(tableLeft, tableTop, tableRight, tableTop + 18f)
        canvas.drawRect(tableHeaderRect, headerPaint)

        // Table Header Text
        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val colTitles = arrayOf("ক্রমিক নং", "খাবারের নাম / বিবরণ", "পরিমাণ", "দর", "টাকা")
        var currentX = tableLeft
        for (i in 0 until 5) {
            val colCenterX = currentX + colWidths[i] / 2
            canvas.drawText(colTitles[i], colCenterX, tableTop + 13f, headerTextPaint)
            currentX += colWidths[i]
        }

        // Table Rows (Exactly 14 rows filling half-page height)
        val validItems = items.filter { it.name.isNotBlank() || it.amount > 0 }
        val totalRows = 14
        val rowHeight = 22.5f
        val gridBorderPaint = Paint().apply {
            color = maroonColor
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val rowDashPaint = Paint().apply {
            color = Color.parseColor("#C8A8A8")
            strokeWidth = 0.8f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 3f), 0f)
        }

        val itemTextPaint = Paint().apply {
            color = Color.parseColor("#2C1810")
            textSize = 10f
        }
        val itemBoldPaint = Paint().apply {
            color = Color.parseColor("#2C1810")
            textSize = 10f
            isFakeBoldText = true
        }

        var rowY = tableTop + 18f
        for (r in 0 until totalRows) {
            val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", r + 1))
            
            // Draw Sl No
            itemBoldPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(slNo, tableLeft + colWidths[0] / 2, rowY + 15.5f, itemBoldPaint)

            if (r < validItems.size) {
                val item = validItems[r]
                // Name
                itemBoldPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(item.name, tableLeft + colWidths[0] + 6f, rowY + 15.5f, itemBoldPaint)

                // Qty
                itemTextPaint.textAlign = Paint.Align.CENTER
                val bnQty = BengaliUtils.toBengaliDigits(item.quantity)
                canvas.drawText(bnQty, tableLeft + colWidths[0] + colWidths[1] + colWidths[2] / 2, rowY + 15.5f, itemTextPaint)

                // Rate
                val bnRate = if (item.rate == "0" || item.rate.isBlank()) "" else BengaliUtils.toBengaliDigits(item.rate)
                canvas.drawText(bnRate, tableLeft + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] / 2, rowY + 15.5f, itemTextPaint)

                // Amount
                itemBoldPaint.textAlign = Paint.Align.RIGHT
                val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(DecimalFormat("#,##0").format(item.amount))}/-"
                canvas.drawText(bnAmount, tableRight - 6f, rowY + 15.5f, itemBoldPaint)
            }

            // Row Dashed Bottom Line
            canvas.drawLine(tableLeft, rowY + rowHeight, tableRight, rowY + rowHeight, rowDashPaint)
            rowY += rowHeight
        }

        // Table Total Row
        val totalRowY = rowY
        canvas.drawLine(tableLeft, totalRowY, tableRight, totalRowY, gridBorderPaint)

        // Total Label
        val totalLabelPaint = Paint().apply {
            color = maroonColor
            textSize = 10.5f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("মোট —", tableLeft + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] - 8f, totalRowY + 13f, totalLabelPaint)

        // Total Value
        val totalValPaint = Paint().apply {
            color = maroonColor
            textSize = 11f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val bnTotal = if (totalAmount <= 0) "0/-" else "${BengaliUtils.formatBengaliCurrency(totalAmount)}/-"
        canvas.drawText(bnTotal, tableRight - 6f, totalRowY + 13f, totalValPaint)

        val tableBottomY = totalRowY + 16f
        canvas.drawLine(tableLeft, tableBottomY, tableRight, tableBottomY, gridBorderPaint)

        // Outer Table Rect Border & Vertical Column Grid Lines
        canvas.drawRect(tableLeft, tableTop, tableRight, tableBottomY, gridBorderPaint)
        
        var lineX = tableLeft
        for (i in 0 until 4) {
            lineX += colWidths[i]
            canvas.drawLine(lineX, tableTop, lineX, tableBottomY, gridBorderPaint)
        }

        // FOOTER BELOW TABLE
        val footerStartY = tableBottomY + 14f
        val footerPaint = Paint().apply {
            color = Color.parseColor("#2C1810")
            textSize = 9.5f
        }

        canvas.drawText("কথায় (টাকার পরিমাণ) : .......................................................................................... টাকা মাত্র।", 15f, footerStartY, footerPaint)

        // Signature Row
        val sigY = footerStartY + 24f
        canvas.drawText("(ক্রেতার স্বাক্ষর : _______________________", 15f, sigY, footerPaint)

        canvas.restore()
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
        approverLabel: String,
        position: PrintPosition = PrintPosition.TOP
    ): String {
        val bengaliDate = BengaliUtils.toBengaliDigits(dateString)
        val bengaliTotal = if (totalAmount <= 0) "0/-" else "${BengaliUtils.formatBengaliCurrency(totalAmount)}/-"

        val validItems = items.filter { it.name.isNotBlank() || it.amount > 0 }
        val totalRowsCount = 14

        val rowsHtml = StringBuilder()
        for (i in 0 until totalRowsCount) {
            val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", i + 1))
            if (i < validItems.size) {
                val item = validItems[i]
                val bnQty = BengaliUtils.toBengaliDigits(item.quantity)
                val bnRate = if (item.rate == "0" || item.rate.isBlank()) "" else BengaliUtils.toBengaliDigits(item.rate)
                val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(DecimalFormat("#,##0").format(item.amount))}/-"

                rowsHtml.append("""
                    <tr>
                        <td class="sl-col">$slNo</td>
                        <td class="item-col">${item.name}</td>
                        <td class="qty-col">$bnQty</td>
                        <td class="rate-col">$bnRate</td>
                        <td class="amount-col">$bnAmount</td>
                    </tr>
                """.trimIndent())
            } else {
                rowsHtml.append("""
                    <tr>
                        <td class="sl-col">$slNo</td>
                        <td class="item-col"></td>
                        <td class="qty-col"></td>
                        <td class="rate-col"></td>
                        <td class="amount-col"></td>
                    </tr>
                """.trimIndent())
            }
        }

        val memoCardHtml = """
            <div class="memo-half">
                <div class="memo-container">
                    <div class="header-banner">
                        <h1>$centerName</h1>
                        <p>$subtitle</p>
                    </div>
                    <div class="sawtooth-bar"></div>

                    <div class="meta-row">
                        <div>তারিখ : $bengaliDate</div>
                    </div>

                    <div class="table-wrapper">
                        <table class="memo-table">
                            <thead>
                                <tr>
                                    <th style="width: 10%;">ক্রমিক নং</th>
                                    <th style="width: 44%;">খাবারের নাম / বিবরণ</th>
                                    <th style="width: 16%;">পরিমাণ</th>
                                    <th style="width: 12%;">দর</th>
                                    <th style="width: 18%;">টাকা</th>
                                </tr>
                            </thead>
                            <tbody>
                                $rowsHtml
                                <tr class="total-row">
                                    <td colspan="4" class="total-label">মোট —</td>
                                    <td class="total-amount">$bengaliTotal</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="footer-section">
                        <div class="words-row">
                            কথায় (টাকার পরিমাণ) : .......................................................................................... টাকা মাত্র।
                        </div>
                        <div class="signatures-row">
                            <div class="sig-box">
                                (ক্রেতার স্বাক্ষর : <span class="sig-line"></span>)
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        """.trimIndent()

        val emptyHalfHtml = """
            <div class="empty-half"></div>
        """.trimIndent()

        val contentBodyHtml = when (position) {
            PrintPosition.TOP -> """
                $memoCardHtml
                $emptyHalfHtml
            """.trimIndent()
            PrintPosition.BOTTOM -> """
                $emptyHalfHtml
                $memoCardHtml
            """.trimIndent()
            PrintPosition.BOTH -> """
                $memoCardHtml
                $memoCardHtml
            """.trimIndent()
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    @page {
                        size: A4 portrait;
                        margin: 0;
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
                    .memo-half {
                        width: 210mm;
                        height: 148mm;
                        position: relative;
                        box-sizing: border-box;
                        overflow: hidden;
                    }
                    .empty-half {
                        width: 210mm;
                        height: 148mm;
                    }
                    .memo-container {
                        width: 198mm;
                        height: 142mm;
                        margin: 3mm auto;
                        background: #FFFDF9;
                        box-sizing: border-box;
                    }
                    .header-banner {
                        background-color: #701B1B;
                        color: #FFFFFF;
                        text-align: center;
                        padding: 8px 6px 6px 6px;
                    }
                    .header-banner h1 {
                        margin: 0;
                        font-size: 22px;
                        font-weight: bold;
                        letter-spacing: 0.5px;
                    }
                    .header-banner p {
                        margin: 2px 0 0 0;
                        font-size: 12px;
                        font-weight: normal;
                    }
                    .sawtooth-bar {
                        height: 6px;
                        background-color: #701B1B;
                        clip-path: polygon(
                            0% 0%, 100% 0%,
                            100% 50%, 98% 100%, 96% 50%, 94% 100%, 92% 50%, 90% 100%,
                            88% 50%, 86% 100%, 84% 50%, 82% 100%, 80% 50%, 78% 100%,
                            76% 50%, 74% 100%, 72% 50%, 70% 100%, 68% 50%, 66% 100%,
                            64% 50%, 62% 100%, 60% 50%, 58% 100%, 56% 50%, 54% 100%,
                            52% 50%, 50% 100%, 48% 50%, 46% 100%, 44% 50%, 42% 100%,
                            40% 50%, 38% 100%, 36% 50%, 34% 100%, 32% 50%, 30% 100%,
                            28% 50%, 26% 100%, 24% 50%, 22% 100%, 20% 50%, 18% 100%,
                            16% 50%, 14% 100%, 12% 50%, 10% 100%, 8% 50%, 6% 100%,
                            4% 50%, 2% 100%, 0% 50%
                        );
                        margin-bottom: 6px;
                    }
                    .meta-row {
                        display: flex;
                        justify-content: flex-end;
                        align-items: center;
                        padding: 4px 16px;
                        font-size: 12px;
                        font-weight: bold;
                        color: #701B1B;
                    }
                    .table-wrapper {
                        padding: 0 16px;
                    }
                    .memo-table {
                        width: 100%;
                        border-collapse: collapse;
                        border: 1.5px solid #701B1B;
                        border-radius: 2px;
                        overflow: hidden;
                    }
                    .memo-table th {
                        background-color: #701B1B;
                        color: #FFFFFF;
                        font-weight: bold;
                        font-size: 11px;
                        padding: 4px 4px;
                        border-right: 1.5px solid #FFFFFF;
                        text-align: center;
                    }
                    .memo-table th:last-child {
                        border-right: none;
                    }
                    .memo-table td {
                        border-right: 1.5px solid #701B1B;
                        border-bottom: 1px dashed #C8B8B8;
                        padding: 5px 5px;
                        font-size: 11.5px;
                        color: #2C1810;
                        height: 24px;
                    }
                    .memo-table td:last-child {
                        border-right: none;
                    }
                    .sl-col { text-align: center; font-weight: bold; width: 10%; }
                    .item-col { text-align: left; font-weight: bold; width: 44%; }
                    .qty-col { text-align: center; width: 16%; }
                    .rate-col { text-align: center; width: 12%; }
                    .amount-col { text-align: right; font-weight: bold; width: 18%; }

                    .total-row td {
                        border-top: 1.5px solid #701B1B;
                        border-bottom: none;
                        font-weight: bold;
                    }
                    .total-label {
                        text-align: right;
                        font-size: 12px;
                        color: #701B1B;
                        padding-right: 10px;
                        border-right: 1.5px solid #701B1B;
                    }
                    .total-amount {
                        text-align: right;
                        font-size: 13px;
                        color: #701B1B;
                        font-weight: bold;
                    }
                    .footer-section {
                        padding: 10px 16px 0 16px;
                        font-size: 11px;
                        color: #2C1810;
                    }
                    .words-row {
                        margin-bottom: 16px;
                        font-weight: 500;
                    }
                    .signatures-row {
                        display: flex;
                        justify-content: space-between;
                        align-items: flex-end;
                        margin-top: 12px;
                    }
                    .sig-box {
                        font-weight: bold;
                        color: #2C1810;
                    }
                    .sig-line {
                        display: inline-block;
                        width: 140px;
                        border-bottom: 1px solid #701B1B;
                        margin-left: 6px;
                    }
                </style>
            </head>
            <body>
                $contentBodyHtml
            </body>
            </html>
        """.trimIndent()
    }
}
