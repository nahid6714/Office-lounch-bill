package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BillItem
import com.example.ui.CurrentBillState
import com.example.ui.theme.CreamPaperBg
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.StampBlue
import com.example.util.BengaliUtils
import com.example.util.PrintPosition

@Composable
fun VoucherPreviewDialog(
    state: CurrentBillState,
    onDismiss: () -> Unit,
    onPrint: (PrintPosition) -> Unit,
    onSharePdf: (PrintPosition) -> Unit
) {
    val validItems = state.items.filter { it.name.isNotBlank() || it.amount > 0 }
    var selectedPosition by remember { mutableStateOf(PrintPosition.TOP) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.96f)
                .testTag("voucher_preview_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = CreamPaperBg
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "বিল প্রিভিউ & A4 পজিশন",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenText
                        )
                        Text(
                            text = "A4 পেপারে প্রিন্ট/পিডিএফ পজিশন সিলেক্ট করুন:",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_preview_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = ForestGreenText)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Position Selection Bar (Segmented Controls)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8E0D5), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PrintPosition.values().forEach { pos ->
                        val isSelected = selectedPosition == pos
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    if (isSelected) DarkForestGreen else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPosition = pos }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pos.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else ForestGreenText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive A4 Paper Representation Scroll Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFEDE7DC), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        when (selectedPosition) {
                            PrintPosition.TOP -> {
                                SingleMemoVoucherCard(state = state, validItems = validItems)
                                Spacer(modifier = Modifier.height(10.dp))
                                EmptyPaperHalfPlaceholder()
                            }
                            PrintPosition.BOTTOM -> {
                                EmptyPaperHalfPlaceholder()
                                Spacer(modifier = Modifier.height(10.dp))
                                SingleMemoVoucherCard(state = state, validItems = validItems)
                            }
                            PrintPosition.BOTH -> {
                                SingleMemoVoucherCard(state = state, validItems = validItems)
                                Spacer(modifier = Modifier.height(10.dp))
                                SingleMemoVoucherCard(state = state, validItems = validItems)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Direct System Print Button
                    Button(
                        onClick = { onPrint(selectedPosition) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("preview_print_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen, contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "প্রিন্ট করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Share PDF / WhatsApp Button
                    Button(
                        onClick = { onSharePdf(selectedPosition) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("preview_share_pdf_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "পিডিএফ শেয়ার", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleMemoVoucherCard(
    state: CurrentBillState,
    validItems: List<BillItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = CreamPaperBg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaroonHeaderColor)
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
                    Text(
                        text = state.subtitle,
                        style = TextStyle(
                            fontSize = 11.sp,
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
                    .padding(10.dp)
            ) {
                // Metadata Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "তারিখ: ${BengaliUtils.toBengaliDigits(state.dateString)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaroonHeaderColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Table Grid
                val totalRows = maxOf(14, validItems.size.coerceAtMost(18))
                val rowPaddingY = if (totalRows > 14) 3.5.dp else 7.5.dp
                val dividerBoxHeight = if (totalRows > 14) 14.dp else 19.dp
                val itemTextSize = if (totalRows > 14) 9.sp else 10.sp
                val memoThemeBorder = DarkForestGreen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, memoThemeBorder, RoundedCornerShape(4.dp))
                ) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(memoThemeBorder)
                            .padding(vertical = 5.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ক্র. নং",
                            modifier = Modifier.weight(1.0f),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center),
                            maxLines = 1,
                            softWrap = false
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(12.dp).background(Color.White))
                        Text(
                            text = "খাবারের নাম / বিবরণ",
                            modifier = Modifier.weight(2.5f).padding(start = 4.dp),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Start),
                            maxLines = 1,
                            softWrap = false
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(12.dp).background(Color.White))
                        Text(
                            text = "পরিমাণ",
                            modifier = Modifier.weight(1.2f),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(12.dp).background(Color.White))
                        Text(
                            text = "দর",
                            modifier = Modifier.weight(0.9f),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(12.dp).background(Color.White))
                        Text(
                            text = "টাকা",
                            modifier = Modifier.weight(1.2f).padding(end = 4.dp),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End)
                        )
                    }

                    // Table Body Rows
                    for (r in 0 until totalRows) {
                        val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", r + 1))
                        val item = if (r < validItems.size) validItems[r] else null

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = rowPaddingY, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = slNo,
                                modifier = Modifier.weight(1.0f),
                                style = TextStyle(fontSize = itemTextSize, fontWeight = FontWeight.Bold, color = Color(0xFF2C1810), textAlign = TextAlign.Center)
                            )
                            Box(modifier = Modifier.width(1.2.dp).height(dividerBoxHeight).background(memoThemeBorder))

                            val rawName = item?.name ?: ""
                            val dynamicFontSize = when {
                                rawName.length > 35 -> (itemTextSize.value - 2.5f).sp
                                rawName.length > 20 -> (itemTextSize.value - 1.5f).sp
                                else -> itemTextSize
                            }
                            Text(
                                text = rawName,
                                modifier = Modifier.weight(2.5f).padding(horizontal = 4.dp),
                                style = TextStyle(
                                    fontSize = dynamicFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C1810),
                                    lineHeight = (dynamicFontSize.value * 1.2f).sp
                                ),
                                softWrap = true
                            )
                            Box(modifier = Modifier.width(1.2.dp).height(dividerBoxHeight).background(memoThemeBorder))

                            Text(
                                text = if (item != null) BengaliUtils.toBengaliDigits(item.quantity) else "",
                                modifier = Modifier.weight(1.2f),
                                style = TextStyle(fontSize = itemTextSize, color = Color(0xFF2C1810), textAlign = TextAlign.Center)
                            )
                            Box(modifier = Modifier.width(1.2.dp).height(dividerBoxHeight).background(memoThemeBorder))

                            Text(
                                text = if (item != null && item.rate != "0" && item.rate.isNotBlank()) BengaliUtils.toBengaliDigits(item.rate) else "",
                                modifier = Modifier.weight(0.9f),
                                style = TextStyle(fontSize = itemTextSize, color = Color(0xFF2C1810), textAlign = TextAlign.Center)
                            )
                            Box(modifier = Modifier.width(1.2.dp).height(dividerBoxHeight).background(memoThemeBorder))

                            val bnAmount = if (item != null) {
                                if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(item.amount.toInt().toString())}/-"
                            } else ""
                            Text(
                                text = bnAmount,
                                modifier = Modifier.weight(1.2f).padding(end = 4.dp),
                                style = TextStyle(fontSize = itemTextSize, fontWeight = FontWeight.Bold, color = Color(0xFF2C1810), textAlign = TextAlign.End)
                            )
                        }

                        Canvas(modifier = Modifier.fillMaxWidth().height(0.8.dp)) {
                            drawLine(
                                color = Color(0xFFC2D4CC),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 0.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f), 0f)
                            )
                        }
                    }

                    // Table Total Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F5F2))
                            .padding(vertical = 5.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট —",
                            modifier = Modifier.weight(5.6f).padding(end = 6.dp),
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = memoThemeBorder, textAlign = TextAlign.End)
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(16.dp).background(memoThemeBorder))
                        Text(
                            text = "${BengaliUtils.formatBengaliCurrency(state.totalAmount)}/-",
                            modifier = Modifier.weight(1.2f).padding(end = 4.dp),
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = memoThemeBorder, textAlign = TextAlign.End)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(130.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF2C1810))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.purchaserLabel.ifBlank { "ক্রয়কারীর স্বাক্ষর" },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C1810),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPaperHalfPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFFFBF9F4), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "খালি পেজ (ফাঁকা থাকবে)",
            fontSize = 11.sp,
            color = Color.LightGray,
            fontWeight = FontWeight.Bold
        )
    }
}
