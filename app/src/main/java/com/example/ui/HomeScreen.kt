package com.example.ui

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppSplashScreen
import com.example.ui.components.BillHistoryList
import com.example.ui.components.DocumentScannerScreen
import com.example.ui.components.GlobalSettingsScreen
import com.example.ui.components.MemoVoucherCard
import com.example.ui.components.QuickPresetChips
import com.example.ui.components.SettingsScreen
import com.example.ui.components.ToolsHubScreen
import com.example.ui.components.VoucherPreviewDialog
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.LightForestGreen
import com.example.update.AppUpdateManager
import com.example.update.UpdateDialog
import com.example.update.UpdateInfo
import com.example.util.PrintUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FoodBillViewModel
) {
    val context = LocalContext.current
    val currentBillState by viewModel.currentBillState.collectAsStateWithLifecycle()
    val historyBills by viewModel.historyBills.collectAsStateWithLifecycle()
    val quickPresets by viewModel.quickPresets.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

    val isEn = appLanguage == "en"

    var selectedTool by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showGlobalSettings by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var isSplashLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val updateManager = remember { AppUpdateManager() }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    BackHandler(enabled = showGlobalSettings || selectedTool != null) {
        if (showGlobalSettings) {
            showGlobalSettings = false
        } else {
            selectedTool = null
        }
    }

    LaunchedEffect(Unit) {
        delay(1200)
        isSplashLoading = false
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Android System Date Picker Dialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDay = String.format("%02d", dayOfMonth)
                val formattedMonth = String.format("%02d", month + 1)
                viewModel.updateDate("$formattedDay/$formattedMonth/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Crossfade(
        targetState = isSplashLoading,
        animationSpec = tween(600),
        label = "splash_fade"
    ) { loading ->
        if (loading) {
            AppSplashScreen(
                appName = "টুলস",
                subtitle = "স্মার্ট ডিজিটাল টুলস সংগ্রহ"
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when {
                                    showGlobalSettings -> if (isEn) "Main App Settings" else "মেইন অ্যাপ সেটিংস"
                                    selectedTool == "food_bill" -> when (selectedTab) {
                                        1 -> if (isEn) "Saved Records" else "সংরক্ষিত হিসাব"
                                        2 -> if (isEn) "Memo Settings" else "মেমো সেটিংস"
                                        else -> if (currentBillState.centerName.isNotBlank())
                                            (if (isEn) "Food Bill - " else "খাবার বিল - ") + currentBillState.centerName
                                        else
                                            (if (isEn) "Food Bill Memo" else "আল বারাকা খাবার বিল")
                                    }
                                    selectedTool == "doc_scanner" -> if (isEn) "Document Scanner" else "ডকুমেন্ট স্ক্যানার"
                                    else -> if (isEn) "Digital Tools Hub" else "ডিজিটাল টুলস হাব"
                                },
                                fontFamily = HeadingFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        },
                        navigationIcon = {
                            if (showGlobalSettings || selectedTool != null) {
                                IconButton(onClick = {
                                    if (showGlobalSettings) {
                                        showGlobalSettings = false
                                    } else {
                                        selectedTool = null
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "ফিরে যান",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        actions = {
                            if (!showGlobalSettings) {
                                IconButton(
                                    onClick = { showGlobalSettings = true },
                                    modifier = Modifier.testTag("global_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "অ্যাপ সেটিংস",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkForestGreen,
                            titleContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    if (!showGlobalSettings && selectedTool == "food_bill") {
                        NavigationBar(
                            containerColor = Color(0xFFEBE2D8),
                            contentColor = DarkForestGreen
                        ) {
                            val darkUnselectedColor = Color(0xFF2C3E35)

                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Edit, contentDescription = if (isEn) "Daily Bill" else "দৈনিক বিল") },
                                label = { Text(if (isEn) "Daily Bill" else "দৈনিক বিল", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = DarkForestGreen,
                                    indicatorColor = DarkForestGreen,
                                    unselectedIconColor = darkUnselectedColor,
                                    unselectedTextColor = darkUnselectedColor
                                ),
                                modifier = Modifier.testTag("tab_daily_bill")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.History, contentDescription = if (isEn) "Saved Records" else "সংরক্ষিত হিসাব") },
                                label = { Text(if (isEn) "Saved Records" else "সংরক্ষিত হিসাব", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = DarkForestGreen,
                                    indicatorColor = DarkForestGreen,
                                    unselectedIconColor = darkUnselectedColor,
                                    unselectedTextColor = darkUnselectedColor
                                ),
                                modifier = Modifier.testTag("tab_saved_history")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = if (isEn) "Memo Settings" else "মেমো সেটিংস") },
                                label = { Text(if (isEn) "Memo Settings" else "মেমো সেটিংস", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = DarkForestGreen,
                                    indicatorColor = DarkForestGreen,
                                    unselectedIconColor = darkUnselectedColor,
                                    unselectedTextColor = darkUnselectedColor
                                ),
                                modifier = Modifier.testTag("tab_settings")
                            )
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFFBF8F3))
                ) {
                    if (showGlobalSettings) {
                        GlobalSettingsScreen(
                            themeMode = themeMode,
                            appLanguage = appLanguage,
                            onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                            onLanguageChange = { lang -> viewModel.setAppLanguage(lang) }
                        )
                    } else if (selectedTool == null) {
                        ToolsHubScreen(
                            appLanguage = appLanguage,
                            onSelectFoodBillTool = {
                                selectedTool = "food_bill"
                                selectedTab = 0
                            },
                            onSelectDocScannerTool = {
                                selectedTool = "doc_scanner"
                            },
                            onSelectAppSettings = {
                                showGlobalSettings = true
                            },
                            onSelectUpcomingTool = { toolTitle ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isEn) "'$toolTitle' tool will be added soon!" else "'$toolTitle' টুলটি খুব শীঘ্রই যোগ করা হচ্ছে!"
                                    )
                                }
                            }
                        )
                    } else if (selectedTool == "food_bill") {
                        when (selectedTab) {
                            0 -> {
                                // Daily Bill Voucher Editor
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(12.dp)
                                ) {
                                    // Memo Paper Cash Voucher
                                    MemoVoucherCard(
                                        state = currentBillState,
                                        quickPresets = quickPresets,
                                        onPresetClick = { name, qty, rate, amount ->
                                            viewModel.addQuickPresetItem(name, qty, rate, amount)
                                        },
                                        onAddCustomPreset = { name, qty, rate, amount ->
                                            viewModel.addCustomQuickPreset(name, qty, rate, amount)
                                        },
                                        onRemovePreset = { preset ->
                                            viewModel.removeQuickPreset(preset)
                                        },
                                        onResetDefaults = {
                                            viewModel.resetQuickPresetsToDefault()
                                        },
                                        onUpdateDateClick = { datePickerDialog.show() },
                                        onUpdateItemName = { id, name -> viewModel.updateItemName(id, name) },
                                        onUpdateItemQty = { id, qty -> viewModel.updateItemQuantity(id, qty) },
                                        onUpdateItemRate = { id, rate -> viewModel.updateItemRate(id, rate) },
                                        onUpdateItemAmount = { id, amount -> viewModel.updateItemAmount(id, amount) },
                                        onRemoveItem = { id -> viewModel.removeItemRow(id) },
                                        onAddItemRow = { viewModel.addItemRow() },
                                        onPurchaserLabelChange = { viewModel.updatePurchaserLabel(it) }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Action Buttons Bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Print / Preview Button (Forest Green Gradient)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(DarkForestGreen, LightForestGreen)
                                                    )
                                                )
                                                .clickable { showPreviewDialog = true }
                                                .testTag("print_bill_button")
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "প্রিন্ট / প্রিভিউ",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        // Save Button (Deep Emerald/Forest Green Gradient)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                                                    )
                                                )
                                                .clickable { viewModel.saveCurrentBill() }
                                                .testTag("save_bill_button")
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "সংরক্ষণ",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        // Clear / New Button
                                        OutlinedButton(
                                            onClick = { viewModel.resetToInitialTemplate() },
                                            modifier = Modifier
                                                .height(48.dp)
                                                .testTag("clear_bill_button"),
                                            contentPadding = PaddingValues(horizontal = 10.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "নতুন মেমো", tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // History View
                                BillHistoryList(
                                    bills = historyBills,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                    onEditBill = { bill ->
                                        viewModel.loadBillForEditing(bill)
                                        selectedTab = 0
                                    },
                                    onPrintBill = { bill ->
                                        viewModel.loadBillForEditing(bill)
                                        showPreviewDialog = true
                                    },
                                    onSharePdfBill = { bill ->
                                        viewModel.loadBillForEditing(bill)
                                        showPreviewDialog = true
                                    },
                                    onDeleteBill = { id -> viewModel.deleteBill(id) }
                                )
                            }

                            2 -> {
                                // Settings View
                                SettingsScreen(
                                    state = currentBillState,
                                    quickPresets = quickPresets,
                                    onAddCustomPreset = { name, qty, rate, amount ->
                                        viewModel.addCustomQuickPreset(name, qty, rate, amount)
                                    },
                                    onRemovePreset = { preset ->
                                        viewModel.removeQuickPreset(preset)
                                    },
                                    onResetPresetsDefault = {
                                        viewModel.resetQuickPresetsToDefault()
                                    },
                                    onCenterNameChange = { viewModel.updateCenterName(it) },
                                    onSubtitleChange = { viewModel.updateSubtitle(it) },
                                    onPurchaserLabelChange = { viewModel.updatePurchaserLabel(it) },
                                    onSaveSettings = { centerName, subtitle, purchaserLabel ->
                                        viewModel.saveSettings(centerName, subtitle, purchaserLabel)
                                    },
                                    onResetTemplate = { viewModel.resetToInitialTemplate() },
                                    onResetAllData = { viewModel.resetAllUserData() }
                                )
                            }
                        }
                    } else if (selectedTool == "doc_scanner") {
                        DocumentScannerScreen(
                            onShowSnackbar = { msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Voucher Preview Modal Dialog
    if (showPreviewDialog) {
        VoucherPreviewDialog(
            state = currentBillState,
            appLanguage = appLanguage,
            onDismiss = { showPreviewDialog = false },
            onPrint = { pos ->
                val validItems = currentBillState.items.filter { it.name.isNotBlank() || it.amount > 0 }
                PrintUtils.printFoodBill(
                    context = context,
                    centerName = currentBillState.centerName,
                    subtitle = currentBillState.subtitle,
                    dateString = currentBillState.dateString,
                    items = validItems,
                    totalAmount = currentBillState.totalAmount,
                    purchaserLabel = currentBillState.purchaserLabel,
                    position = pos,
                    isEnglish = (appLanguage == "en")
                )
            },
            onSharePdf = { pos ->
                val validItems = currentBillState.items.filter { it.name.isNotBlank() || it.amount > 0 }
                PrintUtils.shareFoodBillPdf(
                    context = context,
                    centerName = currentBillState.centerName,
                    subtitle = currentBillState.subtitle,
                    dateString = currentBillState.dateString,
                    items = validItems,
                    totalAmount = currentBillState.totalAmount,
                    purchaserLabel = currentBillState.purchaserLabel,
                    position = pos,
                    isEnglish = (appLanguage == "en")
                )
            }
        )
    }
}
