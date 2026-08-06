package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BrassAccent
import com.example.ui.theme.CreamPaperBg
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.LedgerRed
import com.example.ui.theme.LightForestGreen
import com.example.ui.theme.StampBlue
import com.example.ui.theme.WarmBorderColor
import com.example.util.BengaliUtils
import com.example.util.QuickPreset

@Composable
fun QuickPresetChips(
    presets: List<QuickPreset>,
    addedItemNames: Set<String> = emptySet(),
    onPresetClick: (name: String, qty: String, rate: String, amount: String) -> Unit,
    onAddCustomPreset: (name: String, qty: String, rate: String, amount: String) -> Unit,
    onRemovePreset: (preset: QuickPreset) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showManageDialog by remember { mutableStateOf(false) }
    var promptPreset by remember { mutableStateOf<QuickPreset?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkForestGreen,
                    modifier = Modifier
                        .clickable { expandedDropdown = true }
                        .testTag("quick_preset_dropdown_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "দ্রুত প্রিসেট আইটেম ▾",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier
                        .widthIn(min = 240.dp)
                        .background(CreamPaperBg)
                ) {
                    Text(
                        text = "— প্রিসেট লিস্ট (ট্যাপ করে সিলেক্ট / আনসেলেক্ট করুন) —",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Divider(color = WarmBorderColor, thickness = 0.5.dp)

                    presets.forEach { preset ->
                        val isAdded = addedItemNames.contains(preset.name.trim())
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (isAdded) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isAdded) ForestGreenText else Color.Black
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (preset.defaultRate.isNotBlank()) {
                                            Text(
                                                text = "৳${BengaliUtils.toBengaliDigits(preset.defaultRate)}",
                                                fontSize = 12.sp,
                                                color = BrassAccent,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (isAdded) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(বাদ দিন)",
                                                fontSize = 10.sp,
                                                color = LedgerRed,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            },
                            trailingIcon = {
                                if (isAdded) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "যোগ করা আছে",
                                            tint = DarkForestGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "বাদ দিন",
                                            tint = LedgerRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                // Toggle preset item into/out of memo
                                onPresetClick(preset.name, preset.defaultQty, preset.defaultRate, preset.defaultAmount)
                            }
                        )
                    }

                    Divider(color = WarmBorderColor, thickness = 0.5.dp)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "+ কাস্টম প্রিসেট যোগ / এডিট করুন",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkForestGreen
                            )
                        },
                        onClick = {
                            expandedDropdown = false
                            showManageDialog = true
                        }
                    )
                }
            }

            // Customize / Add Button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEBE2D8),
                modifier = Modifier
                    .clickable { showManageDialog = true }
                    .testTag("manage_quick_presets_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "কাস্টমাইজ",
                        tint = DarkForestGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "কাস্টমাইজ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkForestGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(presets) { preset ->
                val isAdded = addedItemNames.contains(preset.name.trim())
                PresetChip(
                    preset = preset,
                    isAdded = isAdded,
                    onClick = {
                        onPresetClick(preset.name, preset.defaultQty, preset.defaultRate, preset.defaultAmount)
                    }
                )
            }
        }
    }

    if (showManageDialog) {
        ManageQuickPresetsDialog(
            presets = presets,
            onDismiss = { showManageDialog = false },
            onAddPreset = { name, qty, rate, amount ->
                onAddCustomPreset(name, qty, rate, amount)
            },
            onRemovePreset = onRemovePreset,
            onResetDefaults = onResetDefaults
        )
    }

    if (promptPreset != null) {
        PromptQuantityDialog(
            itemName = promptPreset!!.name,
            initialRate = promptPreset!!.defaultRate,
            initialAmount = promptPreset!!.defaultAmount,
            onDismiss = { promptPreset = null },
            onConfirm = { qty, rate, amount ->
                onPresetClick(promptPreset!!.name, qty, rate, amount)
                promptPreset = null
            }
        )
    }
}

private fun extractQtyNum(str: String): Double {
    val eng = BengaliUtils.toEnglishDigits(str)
    val match = Regex("""\d+(\.\d+)?""").find(eng)
    return match?.value?.toDoubleOrNull() ?: 0.0
}

private fun formatNum(value: Double): String {
    val str = if (value % 1.0 == 0.0) value.toLong().toString() else String.format(java.util.Locale.US, "%.1f", value)
    return BengaliUtils.toBengaliDigits(str)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageQuickPresetsDialog(
    presets: List<QuickPreset>,
    onDismiss: () -> Unit,
    onAddPreset: (name: String, qty: String, rate: String, amount: String) -> Unit,
    onRemovePreset: (preset: QuickPreset) -> Unit,
    onResetDefaults: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusName = remember { FocusRequester() }
    val focusQty = remember { FocusRequester() }
    val focusRate = remember { FocusRequester() }
    val focusAmount = remember { FocusRequester() }

    var editingPreset by remember { mutableStateOf<QuickPreset?>(null) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQtyVal by remember { mutableStateOf("") }
    var newItemRateVal by remember { mutableStateOf("") }
    var newItemAmountVal by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("") }

    val commonUnits = listOf("কেজি", "লিটার", "পোয়া", "পিস", "প্যাকেট", "গ্রাম", "ডজন", "আঁটি")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CreamPaperBg,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.96f),
        title = {
            Text(
                text = "দ্রুত প্রিসেট আইটেম ব্যবস্থাপনা",
                fontFamily = HeadingFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = DarkForestGreen
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (editingPreset != null) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✏️ ‘${editingPreset!!.name}’ আইটেম সম্পাদনা করছেন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkForestGreen
                            )
                            TextButton(
                                onClick = {
                                    editingPreset = null
                                    newItemName = ""
                                    newItemQtyVal = ""
                                    newItemRateVal = ""
                                    newItemAmountVal = ""
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("এডিট বাতিল", fontSize = 11.sp, color = LedgerRed)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "নতুন প্রিসেট আইটেম যোগ করুন:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("আইটেমের নাম (যেমন: পেঁয়াজ, সয়াবিন তেল)", fontSize = 12.sp) },
                    textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = DarkForestGreen
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusQty.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusName)
                        .testTag("custom_preset_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Unit Selector Options
                Text(
                    text = "ডিফল্ট একক সিলেক্ট করুন:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenText
                )

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    commonUnits.forEach { unit ->
                        val isSelected = selectedUnit == unit
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) DarkForestGreen else Color(0xFFE8E0D5),
                            modifier = Modifier.clickable {
                                selectedUnit = if (isSelected) "" else unit
                            }
                        ) {
                            Text(
                                text = unit,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newItemQtyVal,
                        onValueChange = { newVal ->
                            val bnVal = BengaliUtils.toBengaliDigits(newVal)
                            newItemQtyVal = bnVal
                            val rateNum = BengaliUtils.parseBengaliNumber(newItemRateVal)
                            val amtNum = BengaliUtils.parseBengaliNumber(newItemAmountVal)
                            val qtyNum = extractQtyNum(bnVal)
                            if (rateNum > 0 && qtyNum > 0) {
                                newItemAmountVal = formatNum(rateNum * qtyNum)
                            } else if (amtNum > 0 && qtyNum > 0 && rateNum <= 0) {
                                newItemRateVal = formatNum(amtNum / qtyNum)
                            }
                        },
                        label = { Text("পরিমাণ", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = DarkForestGreen
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusRate.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusQty)
                            .testTag("custom_preset_qty_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newItemRateVal,
                        onValueChange = { newVal ->
                            val bnVal = BengaliUtils.toBengaliDigits(newVal)
                            newItemRateVal = bnVal
                            val rateNum = BengaliUtils.parseBengaliNumber(bnVal)
                            val qtyNum = extractQtyNum(newItemQtyVal)
                            if (rateNum > 0 && qtyNum > 0) {
                                newItemAmountVal = formatNum(rateNum * qtyNum)
                            }
                        },
                        label = { Text("দর (টাকা)", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = DarkForestGreen
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusAmount.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRate)
                            .testTag("custom_preset_rate_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newItemAmountVal,
                        onValueChange = { newVal ->
                            val bnVal = BengaliUtils.toBengaliDigits(newVal)
                            newItemAmountVal = bnVal
                            val amtNum = BengaliUtils.parseBengaliNumber(bnVal)
                            val qtyNum = extractQtyNum(newItemQtyVal)
                            if (amtNum > 0 && qtyNum > 0) {
                                newItemRateVal = formatNum(amtNum / qtyNum)
                            }
                        },
                        label = { Text("মোট টাকা", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = DarkForestGreen
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusAmount)
                            .testTag("custom_preset_amount_input"),
                        singleLine = true
                    )
                }

                // Computed final preset preview
                val bnQty = BengaliUtils.toBengaliDigits(newItemQtyVal.trim())
                val bnRate = BengaliUtils.toBengaliDigits(newItemRateVal.trim())
                val bnAmount = BengaliUtils.toBengaliDigits(newItemAmountVal.trim())
                val formattedQty = when {
                    bnQty.isNotBlank() && selectedUnit.isNotBlank() -> {
                        if (bnQty.endsWith(selectedUnit)) bnQty else "$bnQty $selectedUnit"
                    }
                    bnQty.isNotBlank() -> bnQty
                    else -> ""
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (newItemName.isNotBlank()) {
                    Surface(
                        color = Color(0xFFF5ECE4),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val previewStr = buildString {
                            append("প্রিভিউ: $newItemName")
                            if (formattedQty.isNotBlank()) append(" ($formattedQty)")
                            if (bnRate.isNotBlank()) append(" [দর: $bnRate ৳]")
                            if (bnAmount.isNotBlank()) append(" [টাকা: $bnAmount ৳]")
                        }
                        Text(
                            text = previewStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreenText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (editingPreset != null) {
                        Button(
                            onClick = {
                                editingPreset = null
                                newItemName = ""
                                newItemQtyVal = ""
                                newItemRateVal = ""
                                newItemAmountVal = ""
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("বাতিল", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (newItemName.isNotBlank()) {
                                if (editingPreset != null && editingPreset!!.name != newItemName.trim()) {
                                    onRemovePreset(editingPreset!!)
                                }
                                onAddPreset(newItemName.trim(), formattedQty, newItemRateVal.trim(), newItemAmountVal.trim())
                                editingPreset = null
                                newItemName = ""
                                newItemQtyVal = ""
                                newItemRateVal = ""
                                newItemAmountVal = ""
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("add_custom_preset_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            if (editingPreset != null) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (editingPreset != null) "আইটেম আপডেট করুন" else "তালিকায় যোগ করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "বর্তমান দ্রুত আইটেমসমূহ (${presets.size} টি) [যেকোনো আইটেমে ট্যাপ করে এডিট করুন]:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        val isEditingThis = editingPreset == preset
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isEditingThis) DarkForestGreen else Color(0xFFF2E6DC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingPreset = preset
                                    newItemName = preset.name
                                    newItemQtyVal = preset.defaultQty
                                    newItemRateVal = preset.defaultRate
                                    newItemAmountVal = preset.defaultAmount
                                }
                                .testTag("manage_preset_chip_${preset.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val labelText = buildString {
                                        append(preset.name)
                                        val hasDetails = preset.defaultQty.isNotBlank() || preset.defaultRate.isNotBlank() || preset.defaultAmount.isNotBlank()
                                        if (hasDetails) {
                                            append(" (")
                                            var first = true
                                            if (preset.defaultQty.isNotBlank()) {
                                                append(preset.defaultQty)
                                                first = false
                                            }
                                            if (preset.defaultRate.isNotBlank()) {
                                                if (!first) append(" | ")
                                                append("দর: ${preset.defaultRate}৳")
                                                first = false
                                            }
                                            if (preset.defaultAmount.isNotBlank()) {
                                                if (!first) append(" | ")
                                                append("টাকা: ${preset.defaultAmount}৳")
                                            }
                                            append(")")
                                        }
                                    }
                                    Text(
                                        text = labelText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isEditingThis) Color.White else Color.Black
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Edit button
                                    IconButton(
                                        onClick = {
                                            editingPreset = preset
                                            newItemName = preset.name
                                            newItemQtyVal = preset.defaultQty
                                            newItemRateVal = preset.defaultRate
                                            newItemAmountVal = preset.defaultAmount
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "সম্পাদনা",
                                            tint = if (isEditingThis) Color.White else DarkForestGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete button
                                    IconButton(
                                        onClick = {
                                            if (editingPreset == preset) {
                                                editingPreset = null
                                                newItemName = ""
                                                newItemQtyVal = ""
                                                newItemRateVal = ""
                                                newItemAmountVal = ""
                                            }
                                            onRemovePreset(preset)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "ডিলিট",
                                            tint = if (isEditingThis) Color(0xFFFF8A80) else LedgerRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reset Defaults
                TextButton(
                    onClick = { onResetDefaults() },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("reset_presets_default")
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ডিফল্ট তালিকায় ফেরত যান",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen, contentColor = Color.White)
            ) {
                Text("বন্ধ করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptQuantityDialog(
    itemName: String,
    initialRate: String = "",
    initialAmount: String = "",
    onDismiss: () -> Unit,
    onConfirm: (qty: String, rate: String, amount: String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusQty = remember { FocusRequester() }
    val focusRate = remember { FocusRequester() }
    val focusAmount = remember { FocusRequester() }

    var qtyVal by remember { mutableStateOf("") }
    var rateVal by remember { mutableStateOf(initialRate) }
    var amountVal by remember { mutableStateOf(initialAmount) }
    var selectedUnit by remember { mutableStateOf("") }

    val commonUnits = listOf("কেজি", "লিটার", "পোয়া", "পিস", "প্যাকেট", "গ্রাম", "ডজন", "আঁটি")
    val quickQtyOptions = listOf("১", "২", "৩", "৫", "১০", "২৫০ গ্রাম", "৫০০ গ্রাম")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CreamPaperBg,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.96f),
        title = {
            Column {
                Text(
                    text = "‘$itemName’ এর পরিমাণ, দর বা টাকা লিখুন",
                    fontFamily = HeadingFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DarkForestGreen
                )
                Text(
                    text = "পরিমাণ (কেজি/লিটার/পিস), দর অথবা মোট টাকা লিখুন:",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Unit Selection Chips
                Text(
                    text = "একক নির্বাচন করুন (Unit):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenText
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    commonUnits.forEach { unit ->
                        val isSelected = selectedUnit == unit
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) DarkForestGreen else Color(0xFFE8E0D5),
                            modifier = Modifier.clickable {
                                selectedUnit = if (isSelected) "" else unit
                            }
                        ) {
                            Text(
                                text = unit,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else ForestGreenText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quantity Input
                    OutlinedTextField(
                        value = qtyVal,
                        onValueChange = { newVal ->
                            val bnVal = BengaliUtils.toBengaliDigits(newVal)
                            qtyVal = bnVal
                            val rateNum = BengaliUtils.parseBengaliNumber(rateVal)
                            val amtNum = BengaliUtils.parseBengaliNumber(amountVal)
                            val qtyNum = extractQtyNum(bnVal)
                            if (rateNum > 0 && qtyNum > 0) {
                                amountVal = formatNum(rateNum * qtyNum)
                            } else if (amtNum > 0 && qtyNum > 0 && rateNum <= 0) {
                                rateVal = formatNum(amtNum / qtyNum)
                            }
                        },
                        label = { Text("পরিমাণ (১,২)", fontSize = 10.sp) },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = DarkForestGreen
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusRate.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusQty)
                            .testTag("prompt_qty_input")
                    )

                    // Rate Input
                    OutlinedTextField(
                        value = rateVal,
                        onValueChange = { newVal ->
                            val bnVal = BengaliUtils.toBengaliDigits(newVal)
                            rateVal = bnVal
                            val rateNum = BengaliUtils.parseBengaliNumber(bnVal)
                            val qtyNum = extractQtyNum(qtyVal)
                            if (rateNum > 0 && qtyNum > 0) {
                                amountVal = formatNum(rateNum * qtyNum)
                            }
                        },
                        label = { Text("দর (টাকা)", fontSize = 10.sp) },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = DarkForestGreen
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusAmount.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRate)
                            .testTag("prompt_rate_input")
                    )

                    // Total Amount Input
                    OutlinedTextField(
                        value = amountVal,
                        onValueChange = { newVal ->
                            val bnVal = BengaliUtils.toBengaliDigits(newVal)
                            amountVal = bnVal
                            val amtNum = BengaliUtils.parseBengaliNumber(bnVal)
                            val qtyNum = extractQtyNum(qtyVal)
                            if (amtNum > 0 && qtyNum > 0) {
                                rateVal = formatNum(amtNum / qtyNum)
                            }
                        },
                        label = { Text("মোট টাকা", fontSize = 10.sp) },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = DarkForestGreen
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusAmount)
                            .testTag("prompt_amount_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Quantity Preset Buttons
                Text(
                    text = "দ্রুত পরিমাণ নির্বাচন:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickQtyOptions.forEach { option ->
                        val optionText = if (option.contains("গ্রাম")) option else if (selectedUnit.isNotBlank()) "$option $selectedUnit" else option
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0E4D8),
                            modifier = Modifier.clickable {
                                qtyVal = optionText
                                val rateNum = BengaliUtils.parseBengaliNumber(rateVal)
                                val amtNum = BengaliUtils.parseBengaliNumber(amountVal)
                                val qtyNum = extractQtyNum(optionText)
                                if (rateNum > 0 && qtyNum > 0) {
                                    amountVal = formatNum(rateNum * qtyNum)
                                } else if (amtNum > 0 && qtyNum > 0 && rateNum <= 0) {
                                    rateVal = formatNum(amtNum / qtyNum)
                                }
                            }
                        ) {
                            Text(
                                text = optionText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreenText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bnQty = BengaliUtils.toBengaliDigits(qtyVal.trim())
                    val finalQty = when {
                        bnQty.isNotBlank() && selectedUnit.isNotBlank() && !bnQty.contains(selectedUnit) -> "$bnQty $selectedUnit"
                        bnQty.isNotBlank() -> bnQty
                        else -> ""
                    }
                    onConfirm(finalQty, rateVal.trim(), amountVal.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen, contentColor = Color.White)
            ) {
                Text("তালিকায় যোগ করুন", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onConfirm("", rateVal.trim(), amountVal.trim()) }
            ) {
                Text("পরিমাণ ছাড়া যোগ", color = Color.Gray)
            }
        }
    )
}

@Composable
fun PresetChip(
    preset: QuickPreset,
    isAdded: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (isAdded) Color(0xFFD4EDDA) else Color(0xFFF2E6DC)
    val textColor = if (isAdded) DarkForestGreen else ForestGreenText
    val subTextColor = if (isAdded) Color(0xFF1B5E20) else Color.Gray
    val iconColor = if (isAdded) DarkForestGreen else DarkForestGreen

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        shadowElevation = if (isAdded) 0.dp else 1.dp,
        border = if (isAdded) androidx.compose.foundation.BorderStroke(1.dp, DarkForestGreen) else null,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("preset_chip_${preset.name}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .height(14.dp)
                    .width(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = preset.name,
                fontSize = 13.sp,
                fontWeight = if (isAdded) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor
            )
            val hasDetails = preset.defaultQty.isNotBlank() || preset.defaultRate.isNotBlank() || preset.defaultAmount.isNotBlank()
            if (hasDetails) {
                Spacer(modifier = Modifier.width(4.dp))
                val subText = buildString {
                    var first = true
                    if (preset.defaultQty.isNotBlank()) {
                        append(preset.defaultQty)
                        first = false
                    }
                    if (preset.defaultRate.isNotBlank()) {
                        if (!first) append(" | ")
                        append("দর: ${preset.defaultRate}৳")
                        first = false
                    }
                    if (preset.defaultAmount.isNotBlank()) {
                        if (!first) append(" | ")
                        append("টাকা: ${preset.defaultAmount}৳")
                    }
                }
                Text(
                    text = "($subText)",
                    fontSize = 11.sp,
                    color = subTextColor
                )
            }
            if (isAdded) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "বাদ দিন",
                    tint = LedgerRed,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
