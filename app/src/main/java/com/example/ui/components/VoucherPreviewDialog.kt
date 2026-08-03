package com.example.ui.components

import android.webkit.WebView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BillItem
import com.example.ui.CurrentBillState
import com.example.util.BengaliUtils
import com.example.util.PrintUtils

@Composable
fun VoucherPreviewDialog(
    state: CurrentBillState,
    onDismiss: () -> Unit,
    onPrint: () -> Unit,
    onSharePdf: () -> Unit
) {
    val context = LocalContext.current
    val validItems = state.items.filter { it.name.isNotBlank() || it.amount > 0 }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("voucher_preview_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF9F6F0)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "বিল প্রিভিউ (A4 হাফ-পেজ)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonTextColor
                        )
                        Text(
                            text = "A4 কাগজের হরিজন্টাল অর্ধেক পেজে যেভাবে দেখাবে:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = WarmBorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Preview Frame with A5 Landscape / A4 Half Page Ratio (approx 1.414 ratio)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFEFE8DE), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, WarmBorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // A4 Portrait Aspect Ratio Paper Container
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_memo_paper"),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamPaperBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, MaroonHeaderColor, RoundedCornerShape(6.dp))
                        ) {
                            // Header Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaroonHeaderColor)
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = state.centerName,
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = state.subtitle,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White.copy(alpha = 0.95f)
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            SawtoothDivider()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Metadata Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "বিল নম্বর : ........................",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaroonHeaderColor
                                    )
                                    Text(
                                        text = "তারিখ: ${BengaliUtils.toBengaliDigits(state.dateString)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaroonHeaderColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                                    drawLine(
                                        color = MaroonHeaderColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // Table Grid
                                val totalRows = maxOf(12, validItems.size)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaroonHeaderColor, RoundedCornerShape(4.dp))
                                ) {
                                    // Table Header Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaroonHeaderColor)
                                            .padding(vertical = 6.dp, horizontal = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ক্রমিক নং",
                                            modifier = Modifier.weight(0.9f),
                                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                                        )
                                        Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White))
                                        Text(
                                            text = "খাবারের নাম / বিবরণ",
                                            modifier = Modifier.weight(2.6f).padding(start = 4.dp),
                                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Start)
                                        )
                                        Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White))
                                        Text(
                                            text = "পরিমাণ",
                                            modifier = Modifier.weight(1.2f),
                                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                                        )
                                        Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White))
                                        Text(
                                            text = "দর",
                                            modifier = Modifier.weight(0.9f),
                                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                                        )
                                        Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White))
                                        Text(
                                            text = "টাকা",
                                            modifier = Modifier.weight(1.2f).padding(end = 4.dp),
                                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End)
                                        )
                                    }

                                    // Table Body Rows
                                    for (r in 0 until totalRows) {
                                        val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", r + 1))
                                        val item = if (r < validItems.size) validItems[r] else null

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 5.dp, horizontal = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Sl No
                                            Text(
                                                text = slNo,
                                                modifier = Modifier.weight(0.9f),
                                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C1810), textAlign = TextAlign.Center)
                                            )
                                            Box(modifier = Modifier.width(0.8.dp).height(16.dp).background(MaroonHeaderColor))

                                            // Name
                                            Text(
                                                text = item?.name ?: "",
                                                modifier = Modifier.weight(2.6f).padding(start = 4.dp),
                                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C1810))
                                            )
                                            Box(modifier = Modifier.width(0.8.dp).height(16.dp).background(MaroonHeaderColor))

                                            // Qty
                                            Text(
                                                text = if (item != null) BengaliUtils.toBengaliDigits(item.quantity) else "",
                                                modifier = Modifier.weight(1.2f),
                                                style = TextStyle(fontSize = 11.sp, color = Color(0xFF2C1810), textAlign = TextAlign.Center)
                                            )
                                            Box(modifier = Modifier.width(0.8.dp).height(16.dp).background(MaroonHeaderColor))

                                            // Rate
                                            Text(
                                                text = if (item != null && item.rate != "0" && item.rate.isNotBlank()) BengaliUtils.toBengaliDigits(item.rate) else "",
                                                modifier = Modifier.weight(0.9f),
                                                style = TextStyle(fontSize = 11.sp, color = Color(0xFF2C1810), textAlign = TextAlign.Center)
                                            )
                                            Box(modifier = Modifier.width(0.8.dp).height(16.dp).background(MaroonHeaderColor))

                                            // Amount
                                            val bnAmount = if (item != null) {
                                                if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(item.amount.toInt().toString())}/-"
                                            } else ""
                                            Text(
                                                text = bnAmount,
                                                modifier = Modifier.weight(1.2f).padding(end = 4.dp),
                                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C1810), textAlign = TextAlign.End)
                                            )
                                        }

                                        // Dashed Bottom Row Line
                                        Canvas(modifier = Modifier.fillMaxWidth().height(0.8.dp)) {
                                            drawLine(
                                                color = Color(0xFFC8B8B8),
                                                start = Offset(0f, 0f),
                                                end = Offset(size.width, 0f),
                                                strokeWidth = 0.8.dp.toPx(),
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 3f), 0f)
                                            )
                                        }
                                    }

                                    // Table Total Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFBF4EE))
                                            .padding(vertical = 6.dp, horizontal = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "মোট —",
                                            modifier = Modifier.weight(5.6f).padding(end = 8.dp),
                                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaroonHeaderColor, textAlign = TextAlign.End)
                                        )
                                        Box(modifier = Modifier.width(1.dp).height(18.dp).background(MaroonHeaderColor))
                                        Text(
                                            text = "${BengaliUtils.formatBengaliCurrency(state.totalAmount)}/-",
                                            modifier = Modifier.weight(1.2f).padding(end = 4.dp),
                                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaroonHeaderColor, textAlign = TextAlign.End)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Footer Section
                                Text(
                                    text = "কথায় (টাকার পরিমাণ) : ................................................................................ টাকা মাত্র।",
                                    fontSize = 10.sp,
                                    color = Color(0xFF2C1810)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = "ক্রেতার স্বাক্ষর : _______________________",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2C1810)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "────────────────── ◆ ──────────────────",
                                    fontSize = 10.sp,
                                    color = MaroonHeaderColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Direct System Print Button
                    Button(
                        onClick = onPrint,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("preview_print_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonHeaderColor)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "প্রিন্ট করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share PDF / WhatsApp Button
                    Button(
                        onClick = onSharePdf,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("preview_share_pdf_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "পিডিএফ শেয়ার", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
