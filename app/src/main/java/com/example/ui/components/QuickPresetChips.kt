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
import androidx.compose.material.icons.filled.Delete
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
import com.example.util.QuickPreset

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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_preset_chips_row")
        ) {
            items(presets) { preset ->
                PresetChip(
                    preset = preset,
                    onClick = { onPresetClick(preset.name, preset.defaultQty) }
                )
            }

            // Quick Add Chip at the end
            item {
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
    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "দ্রুত আইটেম কাস্টমাইজ করুন",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
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
                    text = "নতুন আইটেম যোগ করুন:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("আইটেমের নাম (যেমন: পেঁয়াজ)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("custom_preset_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newItemQty,
                        onValueChange = { newItemQty = it },
                        label = { Text("পরিমাণ (যেমন: ১ কেজি)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_preset_qty_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            onAddPreset(newItemName, newItemQty)
                            newItemName = ""
                            newItemQty = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("add_custom_preset_submit"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonHeaderColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("তালিকায় যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "বর্তমান দ্রুত আইটেমসমূহ (${presets.size} টি):",
                    fontSize = 13.sp,
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
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF2E6DC),
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
                                    color = MaroonTextColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onRemovePreset(preset) },
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(20.dp)
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

