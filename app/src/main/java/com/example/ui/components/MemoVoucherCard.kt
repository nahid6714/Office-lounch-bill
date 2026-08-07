package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
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
import com.example.util.QuickPreset

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button

val MaroonHeaderColor = DarkForestGreen
val MaroonTextColor = ForestGreenText

@Composable
fun MemoVoucherCard(
    state: CurrentBillState,
    quickPresets: List<QuickPreset> = emptyList(),
    onPresetClick: (name: String, qty: String, rate: String, amount: String) -> Unit = { _, _, _, _ -> },
    onAddCustomPreset: (name: String, qty: String, rate: String, amount: String) -> Unit = { _, _, _, _ -> },
    onRemovePreset: (preset: QuickPreset) -> Unit = {},
    onResetDefaults: () -> Unit = {},
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showManagePresetsDialog by remember { mutableStateOf(false) }

    val itemFocusRequesters = remember(state.items.size) {
        List(state.items.size) {
            List(4) { FocusRequester() }
        }
    }
    val purchaserLabelFocusRequester = remember { FocusRequester() }

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
                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row (Left: নতুন আইটেম যোগ করুন, Right: দ্রুত আইটেম যোগ করুন)
                val canAddItem = state.items.size < 18
                val addedItemNames = remember(state.items) {
                    state.items.map { it.name.trim() }.filter { it.isNotBlank() }.toSet()
                }

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isNarrow = maxWidth < 340.dp
                    if (isNarrow) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onAddItemRow,
                                enabled = canAddItem,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("add_item_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DarkForestGreen
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = DarkForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (canAddItem) "নতুন আইটেম যোগ করুন" else "সর্বোচ্চ ১৮ টি",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkForestGreen,
                                    maxLines = 1
                                )
                            }

                            QuickItemSelectorButton(
                                presets = quickPresets,
                                addedItemNames = addedItemNames,
                                onPresetClick = onPresetClick,
                                onManagePresetsClick = { showManagePresetsDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onAddItemRow,
                                enabled = canAddItem,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("add_item_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DarkForestGreen
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = DarkForestGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (canAddItem) "নতুন আইটেম যোগ করুন" else "সর্বোচ্চ ১৮ টি",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkForestGreen,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            QuickItemSelectorButton(
                                presets = quickPresets,
                                addedItemNames = addedItemNames,
                                onPresetClick = onPresetClick,
                                onManagePresetsClick = { showManagePresetsDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (showManagePresetsDialog) {
                    ManageQuickPresetsDialog(
                        presets = quickPresets,
                        onDismiss = { showManagePresetsDialog = false },
                        onAddPreset = onAddCustomPreset,
                        onRemovePreset = onRemovePreset,
                        onResetDefaults = onResetDefaults
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                    val rowRequesters = itemFocusRequesters.getOrNull(index)
                    val nextItemRowNameRequester = itemFocusRequesters.getOrNull(index + 1)?.getOrNull(0)
                    val nextTargetRequester = nextItemRowNameRequester
                        ?: if (onPurchaserLabelChange != null) purchaserLabelFocusRequester else null

                    MemoItemRow(
                        index = index,
                        item = item,
                        nameFocusRequester = rowRequesters?.getOrNull(0) ?: remember { FocusRequester() },
                        qtyFocusRequester = rowRequesters?.getOrNull(1) ?: remember { FocusRequester() },
                        rateFocusRequester = rowRequesters?.getOrNull(2) ?: remember { FocusRequester() },
                        amountFocusRequester = rowRequesters?.getOrNull(3) ?: remember { FocusRequester() },
                        nextTargetRequester = nextTargetRequester,
                        onNameChange = { onUpdateItemName(item.id, it) },
                        onQtyChange = { onUpdateItemQty(item.id, it) },
                        onRateChange = { onUpdateItemRate(item.id, it) },
                        onAmountChange = { onUpdateItemAmount(item.id, it) },
                        onRemove = { onRemoveItem(item.id) }
                    )
                    Divider(color = Color(0xFFECE3D8), thickness = 0.5.dp)
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
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(purchaserLabelFocusRequester)
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
    nameFocusRequester: FocusRequester,
    qtyFocusRequester: FocusRequester,
    rateFocusRequester: FocusRequester,
    amountFocusRequester: FocusRequester,
    nextTargetRequester: FocusRequester?,
    onNameChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { qtyFocusRequester.requestFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocusRequester)
                    .testTag("item_name_input_$index")
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
                        onValueChange = { onQtyChange(BengaliUtils.toBengaliDigits(it)) },
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(DarkForestGreen),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { rateFocusRequester.requestFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(qtyFocusRequester)
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
            val displayRate = if (item.rate == "0") "" else BengaliUtils.toBengaliDigits(item.rate)
            BasicTextField(
                value = displayRate,
                onValueChange = { onRateChange(BengaliUtils.toBengaliDigits(it)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { amountFocusRequester.requestFocus() }),
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(DarkForestGreen),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(rateFocusRequester)
                    .testTag("item_rate_input_$index")
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
        val displayAmount = if (item.amount <= 0) "" else BengaliUtils.toBengaliDigits(if (item.amount % 1.0 == 0.0) item.amount.toLong().toString() else String.format(java.util.Locale.US, "%.1f", item.amount))
        Box(
            modifier = Modifier
                .weight(1.1f)
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                .border(0.5.dp, WarmBorderColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            BasicTextField(
                value = displayAmount,
                onValueChange = { onAmountChange(BengaliUtils.toBengaliDigits(it)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (nextTargetRequester != null) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = if (nextTargetRequester != null) {
                    KeyboardActions(onNext = { nextTargetRequester.requestFocus() })
                } else {
                    KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                },
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.End
                ),
                cursorBrush = SolidColor(DarkForestGreen),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(amountFocusRequester)
                    .testTag("item_amount_input_$index")
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

@Composable
fun QuickItemSelectorButton(
    presets: List<QuickPreset>,
    addedItemNames: Set<String>,
    onPresetClick: (name: String, qty: String, rate: String, amount: String) -> Unit,
    onManagePresetsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    val transitionState = remember {
        MutableTransitionState(false).apply { targetState = false }
    }

    LaunchedEffect(expandedDropdown) {
        transitionState.targetState = expandedDropdown
    }

    val isPopupVisible = transitionState.currentState || transitionState.targetState

    Box(modifier = modifier) {
        Button(
            onClick = { expandedDropdown = !expandedDropdown },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("quick_item_select_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkForestGreen,
                contentColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (expandedDropdown) "দ্রুত আইটেম বন্ধ করুন ▴" else "দ্রুত আইটেম যোগ করুন ▾",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        if (isPopupVisible) {
            Popup(
                onDismissRequest = { expandedDropdown = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true
                )
            ) {
                // Dimmed Backdrop
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { expandedDropdown = false },
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Shutter animation container
                    AnimatedVisibility(
                        visibleState = transitionState,
                        enter = expandVertically(
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                            expandFrom = Alignment.Top
                        ) + fadeIn(animationSpec = tween(durationMillis = 220)),
                        exit = shrinkVertically(
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                            shrinkTowards = Alignment.Top
                        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
                        modifier = Modifier
                            .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                            .widthIn(max = 460.dp)
                            .fillMaxWidth()
                            .clipToBounds()
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamPaperBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            border = BorderStroke(1.5.dp, DarkForestGreen.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {} // block clicks from closing backdrop
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 14.dp)
                            ) {
                                // Header Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = DarkForestGreen.copy(alpha = 0.12f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = DarkForestGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "দ্রুত প্রিসেট নির্বাচন করুন",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkForestGreen,
                                            fontFamily = HeadingFontFamily
                                        )
                                    }

                                    IconButton(
                                        onClick = { expandedDropdown = false },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "বন্ধ করুন",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Divider(color = WarmBorderColor, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(10.dp))

                                // Item List
                                if (presets.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "কোনো প্রিসেট আইটেম সংরক্ষিত নেই",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "নিচের বাটনে ট্যাপ করে নতুন প্রিসেট যোগ করুন",
                                                fontSize = 11.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 320.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        presets.forEach { preset ->
                                            val isAdded = addedItemNames.contains(preset.name.trim())
                                            val formattedText = BengaliUtils.formatPresetDisplayText(preset)

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isAdded) DarkForestGreen.copy(alpha = 0.10f) else Color.White,
                                                border = BorderStroke(
                                                    width = if (isAdded) 1.2.dp else 1.dp,
                                                    color = if (isAdded) DarkForestGreen.copy(alpha = 0.45f) else WarmBorderColor
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onPresetClick(
                                                            preset.name,
                                                            preset.defaultQty,
                                                            preset.defaultRate,
                                                            preset.defaultAmount
                                                        )
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = formattedText,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = if (isAdded) FontWeight.Bold else FontWeight.SemiBold,
                                                        color = if (isAdded) DarkForestGreen else Color(0xFF222222),
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )

                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    if (isAdded) {
                                                        Surface(
                                                            shape = RoundedCornerShape(12.dp),
                                                            color = DarkForestGreen
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = null,
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = "যোগ করা আছে",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Surface(
                                                            shape = RoundedCornerShape(12.dp),
                                                            color = DarkForestGreen.copy(alpha = 0.08f),
                                                            border = BorderStroke(0.8.dp, DarkForestGreen.copy(alpha = 0.2f))
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Add,
                                                                    contentDescription = null,
                                                                    tint = DarkForestGreen,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = "যোগ করুন",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = DarkForestGreen
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = WarmBorderColor, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Footer action button
                                Button(
                                    onClick = {
                                        expandedDropdown = false
                                        onManagePresetsClick()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEAE3D9),
                                        contentColor = DarkForestGreen
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = DarkForestGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "প্রিসেট ম্যানেজ / এডিট করুন",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkForestGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

