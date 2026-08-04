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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
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
import com.example.util.BengaliUtils

val MaroonHeaderColor = Color(0xFF802B2B)
val CreamPaperBg = Color(0xFFFFFDF9)
val WarmBorderColor = Color(0xFFD3C2B2)
val MaroonTextColor = Color(0xFF5C1F1F)

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
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaroonHeaderColor)
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.centerName,
                        style = TextStyle(
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
                            color = Color.White.copy(alpha = 0.9f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Sawtooth / Decorative teeth line
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaroonTextColor
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
                            tint = MaroonHeaderColor,
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
                        .background(Color(0xFFF7EFE8), shape = RoundedCornerShape(4.dp))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বিবরণ",
                        modifier = Modifier.weight(2.0f),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonTextColor
                        )
                    )
                    Text(
                        text = "পরিমাণ",
                        modifier = Modifier.weight(1.4f),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonTextColor,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = "দর",
                        modifier = Modifier.weight(0.9f),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonTextColor,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = "টাকা",
                        modifier = Modifier.weight(1.1f),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonTextColor,
                            textAlign = TextAlign.End
                        )
                    )
                    Spacer(modifier = Modifier.width(32.dp)) // Reserve space for delete button
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
                    Divider(color = Color(0xFFF0E6DD), thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // + Add Item Button
                OutlinedButton(
                    onClick = onAddItemRow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_item_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaroonHeaderColor
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ নতুন আইটেম যোগ করুন",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaroonHeaderColor, thickness = 2.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Total Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মোট — ",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonTextColor
                        )
                    )
                    Text(
                        text = BengaliUtils.formatBengaliCurrency(state.totalAmount),
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaroonHeaderColor
                        ),
                        modifier = Modifier.testTag("total_amount_text")
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Signatures Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(140.dp)
                    ) {
                        Divider(color = MaroonTextColor, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(4.dp))
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
            BasicTextField(
                value = item.name,
                onValueChange = onNameChange,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                ),
                cursorBrush = SolidColor(MaroonHeaderColor),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("item_name_input_$index")
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Quantity
        var showUnitMenu by remember { mutableStateOf(false) }
        val commonUnits = listOf("কেজি", "লিটার", "পিস", "প্যাকেট", "গ্রাম", "বস্তা", "ডজন", "আঁটি", "টিন")

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
                            text = "পরিমাণ (কেজি)",
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
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(MaroonHeaderColor),
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
                        color = Color(0xFFF2E6DC),
                        modifier = Modifier
                            .clickable { showUnitMenu = true }
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "▼",
                            fontSize = 8.sp,
                            color = MaroonHeaderColor,
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
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(MaroonHeaderColor),
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
                    color = MaroonTextColor,
                    textAlign = TextAlign.End
                ),
                cursorBrush = SolidColor(MaroonHeaderColor),
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

        // Delete Row Icon
        IconButton(
            onClick = onRemove,
            modifier = Modifier.testTag("remove_item_button_$index")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "আইটেম মুছুন",
                tint = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SawtoothDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val width = size.width
        val toothWidth = 16f
        val toothHeight = size.height
        val path = Path()

        path.moveTo(0f, 0f)
        var x = 0f
        var isUp = false
        while (x < width) {
            x += toothWidth
            val y = if (isUp) 0f else toothHeight
            path.lineTo(x, y)
            isUp = !isUp
        }
        path.lineTo(width, 0f)
        path.close()

        drawPath(path = path, color = MaroonHeaderColor)
    }
}
