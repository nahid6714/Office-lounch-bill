package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.BillItem
import java.io.File
import java.text.DecimalFormat

enum class PrintPosition(val label: String, val description: String) {
    TOP("উপরে (Top)", "A4 কাগজের উপরের অর্ধেকাংশে"),
    BOTTOM("নিচে (Bottom)", "A4 কাগজের নিচের অর্ধেকাংশে"),
    BOTH("উভয় অংশ (2 Copies)", "A4 পাতায় ২টি কপি একসাথে (উপরে ও নিচে)")
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
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
                val jobName = "Food_Bill_${dateString.replace("/", "-")}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)

                val builder = PrintAttributes.Builder()
                builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
                builder.setMinMargins(PrintAttributes.Margins(0, 0, 0, 0))

                printManager?.print(jobName, printAdapter, builder.build())
            }
        }

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    /**
     * Generates the share PDF using the SAME rendering engine as printFoodBill
     * (WebView.createPrintDocumentAdapter -> onLayout -> onWrite), instead of the
     * old manual PdfDocument + view.draw(canvas) approach. This guarantees the
     * shared PDF is pixel-identical to the Print Preview, with no cropping.
     */
    fun shareFoodBillPdf(
        context: Context,
        centerName: String = "আল বারাকা মেডিকেল সেন্টার",
        subtitle: String = "দৈনিক খাবার বিল",
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String = "ক্রেতার স্বাক্ষর",
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
                try {
                    val jobName = "Food_Bill_${dateString.replace("/", "-")}"
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)

                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
                        .setMinMargins(PrintAttributes.Margins(0, 0, 0, 0))
                        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                        .build()

                    printAdapter.onLayout(
                        null,
                        attributes,
                        CancellationSignal(),
                        object : PrintDocumentAdapter.LayoutResultCallback() {
                            override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
                                writeAdapterToPdfFile(context, printAdapter, dateString)
                            }

                            override fun onLayoutFailed(error: CharSequence?) {
                                Toast.makeText(
                                    context,
                                    "পিডিএফ লেআউট ব্যর্থ: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onLayoutCancelled() {
                                // No-op: user or system cancelled layout.
                            }
                        },
                        null
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "পিডিএফ ফাইল তৈরি ব্যর্থ: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun writeAdapterToPdfFile(
        context: Context,
        printAdapter: PrintDocumentAdapter,
        dateString: String
    ) {
        try {
            val cacheDir = File(context.cacheDir, "food_bills").apply { mkdirs() }
            val pdfFile = File(cacheDir, "Food_Bill_${dateString.replace("/", "-")}.pdf")
            if (pdfFile.exists()) pdfFile.delete()

            val pfd = ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_CREATE
                    or ParcelFileDescriptor.MODE_WRITE_ONLY
                    or ParcelFileDescriptor.MODE_TRUNCATE
            )

            printAdapter.onWrite(
                arrayOf(PageRange.ALL_PAGES),
                pfd,
                CancellationSignal(),
                object : PrintDocumentAdapter.WriteResultCallback() {
                    override fun onWriteFinished(pages: Array<out PageRange>) {
                        try {
                            pfd.close()
                        } catch (_: Exception) {
                        }
                        sharePdfFile(context, pdfFile, dateString)
                    }

                    override fun onWriteFailed(error: CharSequence?) {
                        try {
                            pfd.close()
                        } catch (_: Exception) {
                        }
                        Toast.makeText(
                            context,
                            "পিডিএফ লেখা ব্যর্থ: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onWriteCancelled() {
                        try {
                            pfd.close()
                        } catch (_: Exception) {
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "পিডিএফ ফাইল তৈরি ব্যর্থ: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
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

            val chooser = Intent.createChooser(shareIntent, "পিডিএফ শেয়ার করুন (WhatsApp / আদার্স)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
                <div class="memo-rotated">
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
                                (ক্রেতার স্বাক্ষর : <span class="sig-line"></span>
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
                    .memo-rotated {
                        width: 142mm;
                        height: 202mm;
                        position: absolute;
                        top: 3mm;
                        left: 205mm;
                        transform: rotate(90deg);
                        transform-origin: top left;
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
                    .dashed-divider {
                        border-bottom: 1.2px dashed #701B1B;
                        margin: 0 16px 8px 16px;
                    }
                    .table-wrapper {
                        padding: 0 16px;
                    }
                    .memo-table {
                        width: 100%;
                        border-collapse: collapse;
                        border: 1.8px solid #5A0000;
                        border-radius: 4px;
                        overflow: hidden;
                    }
                    .memo-table th {
                        background-color: #5A0000;
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
                        border-right: 1.5px solid #5A0000;
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
                    .item-col { text-align: left; font-weight: bold; width: 45%; }
                    .qty-col { text-align: center; width: 15%; }
                    .rate-col { text-align: center; width: 12%; }
                    .amount-col { text-align: right; font-weight: bold; width: 18%; }

                    .total-row td {
                        border-top: 1.8px solid #5A0000;
                        border-bottom: none;
                        font-weight: bold;
                    }
                    .total-label {
                        text-align: right;
                        font-size: 12px;
                        color: #5A0000;
                        padding-right: 10px;
                        border-right: 1.5px solid #5A0000;
                    }
                    .total-amount {
                        text-align: right;
                        font-size: 13px;
                        color: #5A0000;
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
