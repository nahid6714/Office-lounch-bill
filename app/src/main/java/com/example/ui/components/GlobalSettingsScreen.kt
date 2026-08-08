package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.ui.theme.CreamPaperBg
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.HeadingFontFamily
import com.example.update.AppUpdateManager
import com.example.update.UpdateDialog
import com.example.update.UpdateInfo
import com.example.util.BengaliUtils
import kotlinx.coroutines.launch

@Composable
fun GlobalSettingsScreen(
    themeMode: String = "system",
    appLanguage: String = "bn",
    onThemeModeChange: (String) -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // App Update States
    val updateManager = remember { AppUpdateManager() }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var noUpdateMessage by remember { mutableStateOf<String?>(null) }
    var updateErrorMessage by remember { mutableStateOf<String?>(null) }
    var updateInfoToShow by remember { mutableStateOf<UpdateInfo?>(null) }

    // Permissions check
    val hasCameraPermission = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    val hasStoragePermission = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    val isEn = appLanguage == "en"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4EE))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isEn) "App Settings & Management" else "অ্যাপ সেটিংস ও ব্যবস্থাপনা",
                    fontFamily = HeadingFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen
                )
                Text(
                    text = if (isEn) "App update, theme, language & configurations" else "অ্যাপ আপডেট, থিম, ভাষা ও অন্যান্য কনফিগারেশন",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // 1. App Update Section
        SettingsCard(
            title = if (isEn) "App Updates" else "অ্যাপ আপডেট (App Updates)",
            icon = Icons.Default.SystemUpdate
        ) {
            Text(
                text = if (isEn)
                    "Current Version: v${BuildConfig.VERSION_NAME} (Build: ${BuildConfig.VERSION_CODE})"
                else
                    "বর্তমান ভার্সন: v${BengaliUtils.toBengaliDigits(BuildConfig.VERSION_NAME)} (বিল্ড কোড: ${BengaliUtils.toBengaliDigits(BuildConfig.VERSION_CODE.toString())})",
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
                                noUpdateMessage = if (isEn) "You are using the latest version." else "আপনি সর্বশেষ সংস্করণ ব্যবহার করছেন।"
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("global_check_updates_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEn) "Checking updates..." else "আপডেট চেক করা হচ্ছে...", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEn) "Check for Updates" else "আপডেট চেক করুন", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Theme Settings Section
        SettingsCard(
            title = if (isEn) "App Theme (Theme Mode)" else "অ্যাপ থিম (Theme Mode)",
            icon = Icons.Default.ColorLens
        ) {
            val themes = listOf(
                "system" to if (isEn) "System Default" else "সিস্টেম ডিফল্ট (System Default)",
                "light" to if (isEn) "Light Mode" else "লাইট মোড (Light Mode)",
                "dark" to if (isEn) "Dark Mode" else "ডার্ক মোড (Dark Mode)"
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                themes.forEach { (key, label) ->
                    val isSelected = (themeMode == key)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onThemeModeChange(key) }
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onThemeModeChange(key) },
                            colors = RadioButtonDefaults.colors(selectedColor = DarkForestGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 13.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) DarkForestGreen else Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Language Settings Section
        SettingsCard(
            title = if (isEn) "Language" else "ভাষা (Language)",
            icon = Icons.Default.Language
        ) {
            val languages = listOf(
                "bn" to if (isEn) "Bangla (বাংলা)" else "বাংলা (Bangla) - ডিফল্ট",
                "en" to if (isEn) "English (Active)" else "English (ইংরেজি) - সক্রিয়"
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                languages.forEach { (key, label) ->
                    val isSelected = (appLanguage == key)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLanguageChange(key) }
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onLanguageChange(key) },
                            colors = RadioButtonDefaults.colors(selectedColor = DarkForestGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 13.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) DarkForestGreen else Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Privacy & Permissions
        SettingsCard(
            title = "প্রাইভেসি ও সিস্টেম পারমিশনস",
            icon = Icons.Default.Security
        ) {
            // Camera Permission Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = DarkForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ক্যামেরা পারমিশন:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text(
                    text = if (hasCameraPermission) "অনুমতি দেওয়া হয়েছে ✓" else "অনুমতি প্রয়োজন ✕",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasCameraPermission) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Storage Permission Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = DarkForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("স্টোরেজ/ফটোস পারমিশন:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text(
                    text = if (hasStoragePermission) "অনুমতি দেওয়া হয়েছে ✓" else "স্বয়ংক্রিয়/প্রয়োজন নেই",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Ignore
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("ডিভাইস অ্যাপ সেটিংস ওপেন করুন", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Backup & Restore (Future Ready)
        SettingsCard(
            title = "ব্যাকআপ ও রিস্টোর (Cloud Ready)",
            icon = Icons.Default.CloudDownload
        ) {
            Text(
                text = "আপনার সংরক্ষিত সকল মেমো হিসাব ও স্ক্যানকৃত ফাইল নিরাপদ রাখতে অটোমেটিক ক্লাউড সিঙ্ক ও লোকাল ব্যাকআপ ফিচারটি পরবর্তীতে যোগ করা হবে।",
                fontSize = 12.5.sp,
                color = Color.Gray,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { },
                enabled = false,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FolderZip,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("লোকাল ডাটা এক্সপোর্ট (শীঘ্রই আসছে)", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. About App Section
        SettingsCard(
            title = "অ্যাপ সম্পর্কে (About App)",
            icon = Icons.Default.Info
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ডিজিটাল টুলস হাব (Digital Tools Hub)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen
                )
                Text(
                    text = "আপনার দৈনন্দিন মেমো তৈরি, খাবার বাজার হিসাব সংরক্ষণ এবং NID বা আইডি কার্ড দ্রুত স্ক্যান ও ফিল্টার করার জন্য একটি স্মার্ট বাংলা টুলস অ্যাপ।",
                    fontSize = 12.5.sp,
                    color = Color.DarkGray,
                    lineHeight = 17.sp
                )

                Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFE0E0E0))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("অ্যাপ ভার্সন:", fontSize = 12.5.sp, color = Color.Gray)
                    Text("v${BuildConfig.VERSION_NAME}", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("বিল্ড কোড:", fontSize = 12.5.sp, color = Color.Gray)
                    Text("${BuildConfig.VERSION_CODE}", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("বিল্ড স্ট্যাটাস:", fontSize = 12.5.sp, color = Color.Gray)
                    Text("অফিশিয়াল স্টেবল রিলিজ", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = DarkForestGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Developer Info
        SettingsCard(
            title = "ডেভেলপার তথ্য (Developer Info)",
            icon = Icons.Default.DeveloperMode
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ডিজিটাল টুলস ডেভেলপমেন্ট টিম",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen
                )
                Text(
                    text = "গিটহাব রিপোজিটরি: github.com/nahid6714/tools",
                    fontSize = 12.5.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "যেকোনো মতামত বা পরামর্শের জন্য যোগাযোগ করতে পারেন।",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 8. Open Source Licenses
        SettingsCard(
            title = "ওপেন সোর্স লাইসেন্স",
            icon = Icons.Default.Code
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "• Jetpack Compose & Material 3 (Apache 2.0)",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "• Google ML Kit Document Scanner API",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "• Room Persistence Library (AndroidX)",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Update Dialog if new release detected
    if (updateInfoToShow != null) {
        UpdateDialog(
            updateInfo = updateInfoToShow!!,
            updateManager = updateManager,
            onDismiss = { updateInfoToShow = null }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen,
                    fontFamily = HeadingFontFamily
                )
            }

            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            content()
        }
    }
}
