package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.BengaliUtils
import com.example.util.QuickPreset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickPresetChips(
    presets: List<QuickPreset>,
    onPresetClick: (name: String, qty: String) -> Unit,
    onAddCustomPreset: (name: String, qty: String) -> Unit,
    onRemovePreset: (preset: QuickPreset) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showManageDialog by remember { mutableStateOf(false) }
    var promptPreset by remember { mutableStateOf<QuickPreset?>(null) }

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
            Text(
                text = "দ্রুত আইটেম যোগ করুন:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaroonTextColor
            )

            // Customize / Add Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaroonHeaderColor,
                modifier = Modifier
                    .clickable { showManageDialog = true }
                    .testTag("manage_quick_presets_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "কাস্টমাইজ",
                        tint = Color.White,
                        modifier = Modifier
                            .height(12.dp)
                            .width(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "কাস্টমাইজ করুন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("quick_preset_chips_flow")
        ) {
            presets.forEach { preset ->
                PresetChip(
                    preset = preset,
                    onClick = {
                        if (preset.defaultQty.isBlank()) {
                            promptPreset = preset
                        } else {
                            onPresetClick(preset.name, preset.defaultQty)
                        }
                    }
                )
            }

            // Quick Add Chip at the end
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE8D7C8),
                modifier = Modifier
                    .clickable { showManageDialog = true }
                    .testTag("add_custom_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "নতুন আইটেম",
                        tint = MaroonHeaderColor,
                        modifier = Modifier
                            .height(14.dp)
                            .width(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ নতুন আইটেম",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaroonHeaderColor
                    )
                }
            }
        }
    }

    if (showManageDialog) {
        ManageQuickPresetsDialog(
            presets = presets,
            onDismiss = { showManageDialog = false },
            onAddPreset = { name, qty ->
                onAddCustomPreset(name, qty)
            },
            onRemovePreset = onRemovePreset,
            onResetDefaults = onResetDefaults
        )
    }

    if (promptPreset != null) {
        PromptQuantityDialog(
            itemName = promptPreset!!.name,
            onDismiss = { promptPreset = null },
            onConfirm = { qty ->
                onPresetClick(promptPreset!!.name, qty)
                promptPreset = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageQuickPresetsDialog(
    presets: List<QuickPreset>,
    onDismiss: () -> Unit,
    onAddPreset: (name: String, qty: String) -> Unit,
    onRemovePreset: (preset: QuickPreset) -> Unit,
    onResetDefaults: () -> Unit
) {
    var editingPreset by remember { mutableStateOf<QuickPreset?>(null) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQtyVal by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("কেজি") }

    val commonUnits = listOf("কেজি", "লিটার", "পিস", "প্যাকেট", "গ্রাম", "ডজন", "আঁটি")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "দ্রুত আইটেম ব্যবস্থাপনা (যোগ/সম্পাদনা/ডিলিট)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaroonHeaderColor
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (editingPreset != null) "‘${editingPreset!!.name}’ আইটেম সম্পাদনা করুন:" else "নতুন দ্রুত আইটেম যোগ করুন:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (editingPreset != null) MaroonHeaderColor else Color.DarkGray
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("আইটেমের নাম (যেমন: পেঁয়াজ, সয়াবিন তেল)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_preset_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Unit Selector Options
                Text(
                    text = "একক সিলেক্ট করুন (কেজি / লিটার / পিস):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaroonTextColor
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
                            color = if (isSelected) MaroonHeaderColor else Color(0xFFE8E0D5),
                            modifier = Modifier.clickable {
                                selectedUnit = if (isSelected) "" else unit
                            }
                        ) {
                            Text(
                                text = unit,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaroonTextColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newItemQtyVal,
                    onValueChange = { newItemQtyVal = it },
                    label = { Text("পরিমাণ (যেমন: ১, ২, ১.৫ - ঐচ্ছিক)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_preset_qty_input"),
                    singleLine = true
                )

                // Computed final preset preview
                val bnQty = BengaliUtils.toBengaliDigits(newItemQtyVal.trim())
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
                        Text(
                            text = "প্রিভিউ: $newItemName${if (formattedQty.isNotBlank()) " ($formattedQty)" else " (পরিমাণ পরে দেওয়া হবে)"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaroonTextColor,
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
                                onAddPreset(newItemName.trim(), formattedQty)
                                editingPreset = null
                                newItemName = ""
                                newItemQtyVal = ""
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("add_custom_preset_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonHeaderColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            if (editingPreset != null) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.height(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (editingPreset != null) "আইটেম আপডেট করুন" else "তালিকায় যোগ করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "বর্তমান দ্রুত আইটেমসমূহ (${presets.size} টি) [সম্পাদনা বা ডিলিট করতে ক্লিক করুন]:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        val isEditingThis = editingPreset == preset
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isEditingThis) MaroonHeaderColor else Color(0xFFF2E6DC),
                            modifier = Modifier.testTag("manage_preset_chip_${preset.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${preset.name}${if (preset.defaultQty.isNotBlank()) " (${preset.defaultQty})" else ""}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isEditingThis) Color.White else MaroonTextColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        editingPreset = preset
                                        newItemName = preset.name
                                        newItemQtyVal = preset.defaultQty
                                    },
                                    modifier = Modifier
                                        .height(22.dp)
                                        .width(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "সম্পাদনা",
                                        tint = if (isEditingThis) Color.White else MaroonHeaderColor,
                                        modifier = Modifier.height(13.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (editingPreset == preset) {
                                            editingPreset = null
                                            newItemName = ""
                                            newItemQtyVal = ""
                                        }
                                        onRemovePreset(preset)
                                    },
                                    modifier = Modifier
                                        .height(22.dp)
                                        .width(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "মুছে ফেলুন",
                                        tint = Color.Red,
                                        modifier = Modifier.height(14.dp)
                                    )
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
                        modifier = Modifier.height(16.dp)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("বন্ধ করুন")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptQuantityDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: (qty: String) -> Unit
) {
    var qtyVal by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("কেজি") }

    val commonUnits = listOf("কেজি", "লিটার", "পিস", "প্যাকেট", "গ্রাম", "ডজন", "আঁটি")
    val quickQtyOptions = listOf("১", "২", "৩", "৫", "১০", "২৫০ গ্রাম", "৫০০ গ্রাম")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "‘$itemName’ এর পরিমাণ লিখুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaroonHeaderColor
                )
                Text(
                    text = "কত কেজি/লিটার/পিস যোগ করতে চান?",
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
                    color = MaroonTextColor
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
                            color = if (isSelected) MaroonHeaderColor else Color(0xFFE8E0D5),
                            modifier = Modifier.clickable {
                                selectedUnit = if (isSelected) "" else unit
                            }
                        ) {
                            Text(
                                text = unit,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaroonTextColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity Input
                OutlinedTextField(
                    value = qtyVal,
                    onValueChange = { qtyVal = it },
                    label = { Text("পরিমাণ (যেমন: ১.৫, ২, ৩)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_qty_input")
                )

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
                            }
                        ) {
                            Text(
                                text = optionText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaroonTextColor,
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
                        selectedUnit.isNotBlank() -> selectedUnit
                        else -> ""
                    }
                    onConfirm(finalQty)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonHeaderColor)
            ) {
                Text("তালিকায় যোগ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onConfirm("") }
            ) {
                Text("পরিমাণ ছাড়া যোগ", color = Color.Gray)
            }
        }
    )
}

@Composable
fun PresetChip(
    preset: QuickPreset,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF2E6DC),
        shadowElevation = 1.dp,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("preset_chip_${preset.name}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaroonHeaderColor,
                modifier = Modifier
                    .height(14.dp)
                    .width(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = preset.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaroonTextColor
            )
            if (preset.defaultQty.isNotBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${preset.defaultQty})",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
