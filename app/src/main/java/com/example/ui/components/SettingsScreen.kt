package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.BuildConfig
import com.example.update.AppUpdateManager
import com.example.update.UpdateDialog
import com.example.update.UpdateInfo
import com.example.util.BengaliUtils
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CurrentBillState
import com.example.ui.theme.CreamPaperBg
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.MaroonHeaderColor
import com.example.ui.theme.WarmBorderColor
import com.example.util.QuickPreset

@Composable
fun SettingsScreen(
    state: CurrentBillState,
    quickPresets: List<QuickPreset> = emptyList(),
    onAddCustomPreset: (name: String, qty: String, rate: String, amount: String) -> Unit = { _, _, _, _ -> },
    onRemovePreset: (preset: QuickPreset) -> Unit = {},
    onResetPresetsDefault: () -> Unit = {},
    onCenterNameChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onPurchaserLabelChange: (String) -> Unit = {},
    onSaveSettings: (centerName: String, subtitle: String, purchaserLabel: String) -> Unit,
    onResetTemplate: () -> Unit,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusCenterName = remember { FocusRequester() }
    val focusSubtitle = remember { FocusRequester() }
    val focusPurchaserLabel = remember { FocusRequester() }

    val signatureOptions = listOf("স্বাক্ষর", "ক্রয়কারীর স্বাক্ষর", "অনুমোদনকারীর স্বাক্ষর")
    val customOption = "কাস্টম..."

    val initialIsPreset = state.purchaserLabel in signatureOptions
    var selectedSignatureOption by remember(state.purchaserLabel) {
        mutableStateOf(if (initialIsPreset) state.purchaserLabel else if (state.purchaserLabel.isBlank()) "স্বাক্ষর" else customOption)
    }
    var customSignatureText by remember(state.purchaserLabel) {
        mutableStateOf(if (initialIsPreset) "" else state.purchaserLabel)
    }
    var expandedSignatureDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "ক্যাশ মেমো ও অ্যাপ সেটিংস",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreenText
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CreamPaperBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "মেমোর হেডার তথ্য পরিবর্তন ও সংরক্ষণ:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.centerName,
                    onValueChange = onCenterNameChange,
                    label = { Text("মেডিকেল বা প্রতিষ্ঠানের নাম") },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = DarkForestGreen
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusSubtitle.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusCenterName)
                        .testTag("center_name_setting_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.subtitle,
                    onValueChange = onSubtitleChange,
                    label = { Text("মেমোর সাবটাইটেল / শিরোনাম") },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = DarkForestGreen
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { expandedSignatureDropdown = true }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusSubtitle)
                        .testTag("subtitle_setting_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "স্বাক্ষরের শিরোনাম নির্বাচন:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedSignatureOption,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("স্বাক্ষরের টাইটেল অপশন") },
                        trailingIcon = {
                            Text(
                                text = "▾",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkForestGreen,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = DarkForestGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signature_title_dropdown")
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedSignatureDropdown = true }
                    )

                    DropdownMenu(
                        expanded = expandedSignatureDropdown,
                        onDismissRequest = { expandedSignatureDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(CreamPaperBg)
                    ) {
                        signatureOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedSignatureOption == option) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedSignatureOption == option) DarkForestGreen else Color.Black
                                    )
                                },
                                onClick = {
                                    selectedSignatureOption = option
                                    expandedSignatureDropdown = false
                                    onPurchaserLabelChange(option)
                                }
                            )
                        }

                        Divider(color = WarmBorderColor, thickness = 0.5.dp)

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = customOption,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedSignatureOption == customOption) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedSignatureOption == customOption) DarkForestGreen else Color.Black
                                )
                            },
                            onClick = {
                                selectedSignatureOption = customOption
                                expandedSignatureDropdown = false
                                if (customSignatureText.isNotBlank()) {
                                    onPurchaserLabelChange(customSignatureText)
                                }
                            }
                        )
                    }
                }

                if (selectedSignatureOption == customOption) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customSignatureText,
                        onValueChange = { newText ->
                            customSignatureText = newText
                            onPurchaserLabelChange(newText)
                        },
                        label = { Text("কাস্টম স্বাক্ষরের শিরোনাম লিখুন") },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = DarkForestGreen
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusPurchaserLabel)
                            .testTag("purchaser_label_setting_input")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        val finalLabel = if (selectedSignatureOption == customOption) customSignatureText.trim() else selectedSignatureOption
                        onSaveSettings(state.centerName, state.subtitle, finalLabel)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("save_settings_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সেটিংস সংরক্ষণ করুন (Save)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Update Section Card
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val updateManager = remember { AppUpdateManager() }
        var isCheckingUpdate by remember { mutableStateOf(false) }
        var noUpdateMessage by remember { mutableStateOf<String?>(null) }
        var updateErrorMessage by remember { mutableStateOf<String?>(null) }
        var updateInfoToShow by remember { mutableStateOf<UpdateInfo?>(null) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CreamPaperBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "অ্যাপ আপডেট (Check for Updates):",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "বর্তমান ইনস্টলকৃত ভার্সন: v${BengaliUtils.toBengaliDigits(BuildConfig.VERSION_NAME)} (Code: ${BengaliUtils.toBengaliDigits(BuildConfig.VERSION_CODE.toString())})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ForestGreenText
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (noUpdateMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DarkForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = noUpdateMessage!!,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkForestGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (updateErrorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = updateErrorMessage!!,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (!isCheckingUpdate) {
                            isCheckingUpdate = true
                            noUpdateMessage = null
                            updateErrorMessage = null
                            coroutineScope.launch {
                                val info = updateManager.checkForUpdate(context)
                                isCheckingUpdate = false
                                if (info.errorMessage != null) {
                                    updateErrorMessage = info.errorMessage
                                } else if (info.hasUpdate) {
                                    updateInfoToShow = info
                                } else {
                                    noUpdateMessage = "আপনি সর্বশেষ সংস্করণ ব্যবহার করছেন।"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("check_for_updates_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("আপডেট চেক করা হচ্ছে...", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("আপডেট চেক করুন", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        if (updateInfoToShow != null) {
            UpdateDialog(
                updateInfo = updateInfoToShow!!,
                updateManager = updateManager,
                onDismiss = { updateInfoToShow = null }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CreamPaperBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "রিসেট অপশন (Reset Data):",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "রিসেট চাপলে সমস্ত সেভ করা তথ্য ও প্রিসেট মুছে গিয়ে অ্যাপ সম্পূর্ণ খালি অবস্থায় ফিরে যাবে। কোনো ডিফল্ট ডাটা আসবে না।",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onResetAllData,
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonHeaderColor, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 46.dp)
                        .testTag("reset_template_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "সমস্ত তথ্য সম্পূর্ণ খালি অবস্থায় রিসেট করুন",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
