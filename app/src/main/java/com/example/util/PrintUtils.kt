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
        purchaserLabel: String = "ক্রেতার স্বাক্ষর",
        approverLabel: String = "অনুমোদনকারীর স্বাক্ষর"
    ) {
        try {
            val pdfDocument = PdfDocument()
            // A4 Portrait dimensions in points: 595 x 842 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val maroonColor = Color.parseColor("#701B1B")

            // Paper Background
            val bgPaint = Paint().apply {
                color = Color.parseColor("#FFFDF9")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

            // Outer Container Border
            val borderPaint = Paint().apply {
                color = maroonColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val containerRect = RectF(20f, 20f, 575f, 822f)
            canvas.drawRoundRect(containerRect, 8f, 8f, borderPaint)

            // Header Banner
            val headerPaint = Paint().apply {
                color = maroonColor
                style = Paint.Style.FILL
            }
            val headerRect = RectF(20f, 20f, 575f, 90f)
            canvas.drawRect(headerRect, headerPaint)

            // Title
            val titleTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(centerName, 297f, 55f, titleTextPaint)

            // Subtitle
            val subtitleTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 13f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(subtitle, 297f, 78f, subtitleTextPaint)

            // Sawtooth / Teeth Bar
            val toothWidth = 10f
            val toothHeight = 6f
            var toothX = 20f
            val toothPaint = Paint().apply {
                color = maroonColor
                style = Paint.Style.FILL
            }
            val whitePaint = Paint().apply {
                color = Color.parseColor("#FFFDF9")
                style = Paint.Style.FILL
            }
            while (toothX < 575f) {
                // draw inverted triangle tooth
                val path = android.graphics.Path().apply {
                    moveTo(toothX, 90f)
                    lineTo(toothX + toothWidth / 2, 90f + toothHeight)
                    lineTo(toothX + toothWidth, 90f)
                    close()
                }
                canvas.drawPath(path, toothPaint)
                toothX += toothWidth
            }

            // Metadata Row (Bill No & Date)
            val metaPaint = Paint().apply {
                color = maroonColor
                textSize = 13f
                isFakeBoldText = true
            }
            val bnDate = BengaliUtils.toBengaliDigits(dateString)
            canvas.drawText("বিল নম্বর : ........................", 35f, 122f, metaPaint)
            
            metaPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("তারিখ : $bnDate", 560f, 122f, metaPaint)

            // Dashed Separator Line
            val dashPaint = Paint().apply {
                color = maroonColor
                strokeWidth = 1.2f
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f, 4f), 0f)
            }
            canvas.drawLine(35f, 134f, 560f, 134f, dashPaint)

            // TABLE GRID CONFIG
            val tableLeft = 35f
            val tableRight = 560f
            val tableTop = 150f
            val colWidths = floatArrayOf(50f, 225f, 85f, 65f, 100f) // Sl, Item Name, Qty, Rate, Amount = 525f total width

            // Table Header Background
            val tableHeaderRect = RectF(tableLeft, tableTop, tableRight, tableTop + 28f)
            canvas.drawRect(tableHeaderRect, headerPaint)

            // Table Header Text
            val headerTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 12f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val colTitles = arrayOf("ক্রমিক নং", "খাবারের নাম / বিবরণ", "পরিমাণ", "দর", "টাকা")
            var currentX = tableLeft
            for (i in 0 until 5) {
                val colCenterX = currentX + colWidths[i] / 2
                canvas.drawText(colTitles[i], colCenterX, tableTop + 18f, headerTextPaint)
                currentX += colWidths[i]
            }

            // Table Rows (12-15 rows)
            val validItems = items.filter { it.name.isNotBlank() || it.amount > 0 }
            val totalRows = maxOf(14, validItems.size)
            val rowHeight = 26f
            val gridBorderPaint = Paint().apply {
                color = maroonColor
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val rowDashPaint = Paint().apply {
                color = Color.parseColor("#C8A8A8")
                strokeWidth = 0.8f
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 3f), 0f)
            }

            val itemTextPaint = Paint().apply {
                color = Color.parseColor("#2C1810")
                textSize = 12f
            }
            val itemBoldPaint = Paint().apply {
                color = Color.parseColor("#2C1810")
                textSize = 12f
                isFakeBoldText = true
            }

            var rowY = tableTop + 28f
            for (r in 0 until totalRows) {
                val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", r + 1))
                
                // Draw Sl No
                itemBoldPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(slNo, tableLeft + colWidths[0] / 2, rowY + 18f, itemBoldPaint)

                if (r < validItems.size) {
                    val item = validItems[r]
                    // Name
                    itemBoldPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(item.name, tableLeft + colWidths[0] + 8f, rowY + 18f, itemBoldPaint)

                    // Qty
                    itemTextPaint.textAlign = Paint.Align.CENTER
                    val bnQty = BengaliUtils.toBengaliDigits(item.quantity)
                    canvas.drawText(bnQty, tableLeft + colWidths[0] + colWidths[1] + colWidths[2] / 2, rowY + 18f, itemTextPaint)

                    // Rate
                    val bnRate = if (item.rate == "0" || item.rate.isBlank()) "" else BengaliUtils.toBengaliDigits(item.rate)
                    canvas.drawText(bnRate, tableLeft + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] / 2, rowY + 18f, itemTextPaint)

                    // Amount
                    itemBoldPaint.textAlign = Paint.Align.RIGHT
                    val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(DecimalFormat("#,##0").format(item.amount))}/-"
                    canvas.drawText(bnAmount, tableRight - 8f, rowY + 18f, itemBoldPaint)
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
                textSize = 14f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("মোট —", tableLeft + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] - 12f, totalRowY + 20f, totalLabelPaint)

            // Total Value
            val totalValPaint = Paint().apply {
                color = maroonColor
                textSize = 15f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            val bnTotal = if (totalAmount <= 0) "0/-" else "${BengaliUtils.formatBengaliCurrency(totalAmount)}/-"
            canvas.drawText(bnTotal, tableRight - 8f, totalRowY + 20f, totalValPaint)

            val tableBottomY = totalRowY + 28f
            canvas.drawLine(tableLeft, tableBottomY, tableRight, tableBottomY, gridBorderPaint)

            // Outer Table Rect Border & Vertical Column Grid Lines
            canvas.drawRect(tableLeft, tableTop, tableRight, tableBottomY, gridBorderPaint)
            
            var lineX = tableLeft
            for (i in 0 until 4) {
                lineX += colWidths[i]
                // For last row (total row), only draw up to totalRowY for column 1 & 2
                val lineEndY = if (i < 3) tableBottomY else tableBottomY
                canvas.drawLine(lineX, tableTop, lineX, lineEndY, gridBorderPaint)
            }

            // FOOTER BELOW TABLE
            val footerStartY = tableBottomY + 30f
            val footerPaint = Paint().apply {
                color = Color.parseColor("#2C1810")
                textSize = 13f
            }

            canvas.drawText("কথায় (টাকার পরিমাণ) : .......................................................................................... টাকা মাত্র।", 35f, footerStartY, footerPaint)

            // Signature Row
            val sigY = footerStartY + 60f
            canvas.drawText("ক্রেতার স্বাক্ষর : _______________________", 35f, sigY, footerPaint)

            // Bottom Diamond Decor
            val diamondPaint = Paint().apply {
                color = maroonColor
                textSize = 12f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("────────────────── ◆ ──────────────────", 297f, sigY + 45f, diamondPaint)

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
        val bengaliTotal = if (totalAmount <= 0) "0/-" else "${BengaliUtils.formatBengaliCurrency(totalAmount)}/-"

        val validItems = items.filter { it.name.isNotBlank() || it.amount > 0 }
        val totalRowsCount = maxOf(14, validItems.size)

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

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    @page {
                        size: A4 portrait;
                        margin: 6mm;
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
                    .memo-container {
                        width: 100%;
                        max-width: 195mm;
                        margin: 0 auto;
                        border: 2px solid #701B1B;
                        border-radius: 6px;
                        background: #FFFDF9;
                        padding-bottom: 12px;
                        box-sizing: border-box;
                    }
                    .header-banner {
                        background-color: #701B1B;
                        color: #FFFFFF;
                        text-align: center;
                        padding: 12px 8px 8px 8px;
                    }
                    .header-banner h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: bold;
                        letter-spacing: 0.5px;
                    }
                    .header-banner p {
                        margin: 4px 0 0 0;
                        font-size: 13px;
                        font-weight: normal;
                    }
                    .sawtooth-bar {
                        height: 8px;
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
                        margin-bottom: 8px;
                    }
                    .meta-row {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding: 8px 16px;
                        font-size: 13px;
                        font-weight: bold;
                        color: #701B1B;
                    }
                    .dashed-divider {
                        border-bottom: 1.5px dashed #701B1B;
                        margin: 0 16px 12px 16px;
                    }
                    .table-wrapper {
                        padding: 0 16px;
                    }
                    .memo-table {
                        width: 100%;
                        border-collapse: collapse;
                        border: 1.5px solid #701B1B;
                        border-radius: 4px;
                        overflow: hidden;
                    }
                    .memo-table th {
                        background-color: #701B1B;
                        color: #FFFFFF;
                        font-weight: bold;
                        font-size: 13px;
                        padding: 6px 4px;
                        border-right: 1px solid #FFFFFF;
                        text-align: center;
                    }
                    .memo-table th:last-child {
                        border-right: none;
                    }
                    .memo-table td {
                        border-right: 1px solid #701B1B;
                        border-bottom: 1px dashed #C8B8B8;
                        padding: 5px 6px;
                        font-size: 13px;
                        color: #2C1810;
                        height: 22px;
                    }
                    .memo-table td:last-child {
                        border-right: none;
                    }
                    .sl-col { text-align: center; font-weight: bold; width: 10%; }
                    .item-col { text-align: left; font-weight: bold; width: 45%; }
                    .qty-col { text-align: center; width: 15%; }
                    .rate-col { text-align: center; width: 12%; }
                    .amount-col { text-align: right; font-weight: bold; width: 18%; }

                    .total-row td {
                        border-top: 1.5px solid #701B1B;
                        border-bottom: none;
                        font-weight: bold;
                    }
                    .total-label {
                        text-align: right;
                        font-size: 14px;
                        color: #701B1B;
                        padding-right: 12px;
                        border-right: 1px solid #701B1B;
                    }
                    .total-amount {
                        text-align: right;
                        font-size: 15px;
                        color: #701B1B;
                        font-weight: bold;
                    }
                    .footer-section {
                        padding: 16px 16px 0 16px;
                        font-size: 13px;
                        color: #2C1810;
                    }
                    .words-row {
                        margin-bottom: 24px;
                        font-weight: 500;
                    }
                    .signatures-row {
                        display: flex;
                        justify-content: space-between;
                        align-items: flex-end;
                        margin-top: 20px;
                    }
                    .sig-box {
                        font-weight: bold;
                        color: #2C1810;
                    }
                    .sig-line {
                        display: inline-block;
                        width: 160px;
                        border-bottom: 1px solid #701B1B;
                        margin-left: 6px;
                    }
                    .bottom-diamond {
                        text-align: center;
                        margin-top: 20px;
                        color: #701B1B;
                        font-size: 12px;
                        letter-spacing: 2px;
                    }
                </style>
            </head>
            <body>
                <div class="memo-container">
                    <div class="header-banner">
                        <h1>$centerName</h1>
                        <p>$subtitle</p>
                    </div>
                    <div class="sawtooth-bar"></div>

                    <div class="meta-row">
                        <div>বিল নম্বর : ........................</div>
                        <div>তারিখ : $bengaliDate</div>
                    </div>
                    <div class="dashed-divider"></div>

                    <div class="table-wrapper">
                        <table class="memo-table">
                            <thead>
                                <tr>
                                    <th style="width: 10%;">ক্রমিক নং</th>
                                    <th style="width: 45%;">খাবারের নাম / বিবরণ</th>
                                    <th style="width: 15%;">পরিমাণ</th>
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
                                ক্রেতার স্বাক্ষর : <span class="sig-line"></span>
                            </div>
                        </div>
                        <div class="bottom-diamond">
                            ────────────────── ◆ ──────────────────
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
