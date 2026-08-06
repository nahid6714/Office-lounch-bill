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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BillItem
import com.example.ui.CurrentBillState
import com.example.ui.theme.BrassAccent
import com.example.ui.theme.CreamPaperBg
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.LedgerRed
import com.example.ui.theme.LightForestGreen
import com.example.ui.theme.WarmBorderColor
import com.example.util.BengaliUtils

val MaroonHeaderColor = DarkForestGreen
val MaroonTextColor = ForestGreenText

@Composable
fun MemoVoucherCard(
    state: CurrentBillState,
    onUpdateDateClick: () -> Unit,
    onUpdateItemName: (id: String, name: String) -> Unit,
    onUpdateItemQty: (id: String, qty: String) -> Unit,
    onUpdateItemRate: (id: String, rate: String) -> Unit,
    onUpdateItemAmount: (id: String, amount: String) -> Unit,
    onRemoveItem: (id: String) -> Unit,
    onAddItemRow: () -> Unit,
    onPurchaserLabelChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("memo_voucher_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CreamPaperBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Banner with Forest Green Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(DarkForestGreen, LightForestGreen)
                        )
                    )
                    .padding(vertical = 18.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.centerName,
                        style = TextStyle(
                            fontFamily = HeadingFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.subtitle,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.92f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Sawtooth / Decorative teeth line with double rule
            SawtoothDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Date Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onUpdateDateClick() }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "তারিখ:",
                            style = TextStyle(
                                fontFamily = HeadingFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenText
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = BengaliUtils.toBengaliDigits(state.dateString),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "তারিখ পরিবর্তন",
                            tint = DarkForestGreen,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }

                Divider(color = WarmBorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0E8DF), shape = RoundedCornerShape(4.dp))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বিবরণ",
                        modifier = Modifier.weight(2.0f),
                        style = TextStyle(
                            fontFamily = HeadingFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenText
                        )
                    )
                    Text(
                        text = "পরিমাণ",
                        modifier = Modifier.weight(1.4f),
                        style = TextStyle(
                            fontFamily = HeadingFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenText,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = "দর",
                        modifier = Modifier.weight(0.9f),
                        style = TextStyle(
                            fontFamily = HeadingFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenText,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = "টাকা",
                        modifier = Modifier.weight(1.1f),
                        style = TextStyle(
                            fontFamily = HeadingFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenText,
                            textAlign = TextAlign.End
                        )
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Item Rows
                state.items.forEachIndexed { index, item ->
                    MemoItemRow(
                        index = index,
                        item = item,
                        onNameChange = { onUpdateItemName(item.id, it) },
                        onQtyChange = { onUpdateItemQty(item.id, it) },
                        onRateChange = { onUpdateItemRate(item.id, it) },
                        onAmountChange = { onUpdateItemAmount(item.id, it) },
                        onRemove = { onRemoveItem(item.id) }
                    )
                    Divider(color = Color(0xFFECE3D8), thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // + Add Item Button
                val canAddItem = state.items.size < 18
                OutlinedButton(
                    onClick = onAddItemRow,
                    enabled = canAddItem,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_item_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DarkForestGreen
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DarkForestGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (canAddItem) "+ নতুন আইটেম যোগ করুন" else "সর্বোচ্চ ১৮ টি আইটেম সীমাবদ্ধ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkForestGreen
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total Summary Row with Brass Dashed/Stitched Border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val stroke = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                            )
                            drawRoundRect(
                                color = BrassAccent,
                                style = stroke,
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                        }
                        .background(Color(0xFFFFFBF2), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট — ",
                            style = TextStyle(
                                fontFamily = HeadingFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenText
                            )
                        )
                        Text(
                            text = BengaliUtils.formatBengaliCurrency(state.totalAmount),
                            style = TextStyle(
                                fontFamily = HeadingFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkForestGreen
                            ),
                            modifier = Modifier.testTag("total_amount_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Signatures Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(160.dp)
                    ) {
                        Divider(color = ForestGreenText, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (onPurchaserLabelChange != null) {
                            BasicTextField(
                                value = state.purchaserLabel,
                                onValueChange = onPurchaserLabelChange,
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x10000000), RoundedCornerShape(4.dp))
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                                    .testTag("purchaser_label_input"),
                                cursorBrush = SolidColor(DarkForestGreen),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.Center) {
                                        if (state.purchaserLabel.isEmpty()) {
                                            Text(
                                                text = "স্বাক্ষরের টাইটেল...",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        } else {
                            Text(
                                text = state.purchaserLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoItemRow(
    index: Int,
    item: BillItem,
    onNameChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Description (Name)
        Box(
            modifier = Modifier
                .weight(2.0f)
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                .border(0.5.dp, WarmBorderColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            if (item.name.isEmpty()) {
                Text(
                    text = "আইটেমের নাম",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
            val nameFontSize = when {
                item.name.length > 35 -> 11.sp
                item.name.length > 20 -> 12.5.sp
                else -> 14.sp
            }
            BasicTextField(
                value = item.name,
                onValueChange = onNameChange,
                textStyle = TextStyle(
                    fontSize = nameFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                cursorBrush = SolidColor(DarkForestGreen),
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("item_name_input_$index")
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Quantity
        var showUnitMenu by remember { mutableStateOf(false) }
        val commonUnits = listOf("কেজি", "লিটার", "পোয়া", "পিস", "প্যাকেট", "গ্রাম", "ডজন", "আঁটি")

        Box(
            modifier = Modifier
                .weight(1.4f)
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                .border(0.5.dp, WarmBorderColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (item.quantity.isEmpty()) {
                        Text(
                            text = "পরিমাণ",
                            color = Color.Gray.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    BasicTextField(
                        value = item.quantity,
                        onValueChange = onQtyChange,
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(DarkForestGreen),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_qty_input_$index")
                    )
                }

                // Small dropdown unit selector button
                Box {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFFEBE2D8),
                        modifier = Modifier
                            .clickable { showUnitMenu = true }
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "▼",
                            fontSize = 8.sp,
                            color = DarkForestGreen,
                            modifier = Modifier.padding(2.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showUnitMenu,
                        onDismissRequest = { showUnitMenu = false }
                    ) {
                        commonUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit, fontSize = 12.sp) },
                                onClick = {
                                    showUnitMenu = false
                                    val digits = item.quantity.filter { it.isDigit() || it == '.' || it in '০'..'৯' }
                                    val bnDigits = BengaliUtils.toBengaliDigits(digits)
                                    val newQty = if (bnDigits.isNotBlank()) "$bnDigits $unit" else "১ $unit"
                                    onQtyChange(newQty)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Rate
        Box(
            modifier = Modifier
                .weight(0.9f)
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                .border(0.5.dp, WarmBorderColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            BasicTextField(
                value = if (item.rate == "0") "" else item.rate,
                onValueChange = onRateChange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(DarkForestGreen),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("item_rate_input_$index")
            )
            if (item.rate == "0" || item.rate.isEmpty()) {
                Text(
                    text = "০",
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Amount
        val displayAmount = if (item.amount <= 0) "" else BengaliUtils.toBengaliDigits(item.amount.toInt().toString())
        Box(
            modifier = Modifier
                .weight(1.1f)
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                .border(0.5.dp, WarmBorderColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            BasicTextField(
                value = displayAmount,
                onValueChange = onAmountChange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.End
                ),
                cursorBrush = SolidColor(DarkForestGreen),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("item_amount_input_$index")
            )
            if (displayAmount.isEmpty()) {
                Text(
                    text = "০",
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Delete Row Icon in LedgerRed
        IconButton(
            onClick = onRemove,
            modifier = Modifier.testTag("remove_item_button_$index")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "আইটেম মুছুন",
                tint = LedgerRed
            )
        }
    }
}

@Composable
fun SawtoothDivider() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clipToBounds()
        ) {
            val width = size.width
            val numTeeth = (width / 16f).toInt().coerceAtLeast(1)
            val toothWidth = width / numTeeth
            val toothHeight = size.height
            val path = Path()

            path.moveTo(0f, 0f)
            var x = 0f
            var isUp = false
            for (i in 0 until numTeeth) {
                x += toothWidth
                val y = if (isUp) 0f else toothHeight
                path.lineTo(x, y)
                isUp = !isUp
            }
            path.lineTo(width, 0f)
            path.close()

            drawPath(path = path, color = DarkForestGreen)
        }

        // Double rule line (Brass Accent + Ledger Red)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(BrassAccent)
        )
        Spacer(modifier = Modifier.height(1.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LedgerRed)
        )
    }
}

