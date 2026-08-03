package com.example.ui.components

import android.webkit.WebView
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
import androidx.compose.ui.graphics.Color
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
                    // A5 Landscape Ratio Paper Container
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.418f) // Standard A5 Landscape / A4 Half-Page Aspect Ratio
                            .testTag("preview_memo_paper"),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamPaperBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Header Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaroonHeaderColor, shape = RoundedCornerShape(4.dp))
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = state.centerName,
                                        style = TextStyle(
                                            fontSize = 18.sp,
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
                                            color = Color.White.copy(alpha = 0.9f)
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Date Row (Top Right)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "তারিখ: ${BengaliUtils.toBengaliDigits(state.dateString)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonTextColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Divider(color = WarmBorderColor, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Clean Bullet List Items with Leaders (matching paper memo)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                validItems.forEach { item ->
                                    val bnQty = if (item.quantity.isNotBlank()) " (${BengaliUtils.toBengaliDigits(item.quantity)})" else ""
                                    val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(item.amount.toInt().toString())}/-"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "* ",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaroonHeaderColor
                                        )
                                        Text(
                                            text = item.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2C1810)
                                        )
                                        if (bnQty.isNotEmpty()) {
                                            Text(
                                                text = bnQty,
                                                fontSize = 13.sp,
                                                color = Color(0xFF5C1F1F)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Dashed Leader Line
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(1.dp)
                                                .background(Color(0xFFC8B8A8))
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = bnAmount,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2C1810)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaroonHeaderColor, thickness = 1.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Total Amount (Right aligned with Arrow)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "মোট ➔ ",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonTextColor
                                )
                                Text(
                                    text = "${BengaliUtils.formatBengaliCurrency(state.totalAmount)}/-",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaroonHeaderColor
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Signatures Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(110.dp)
                                ) {
                                    Divider(color = MaroonTextColor, thickness = 0.8.dp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = state.purchaserLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.DarkGray
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(110.dp)
                                ) {
                                    Divider(color = MaroonTextColor, thickness = 0.8.dp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = state.approverLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.DarkGray
                                    )
                                }
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
