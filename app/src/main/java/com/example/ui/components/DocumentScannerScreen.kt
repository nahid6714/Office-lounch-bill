package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.LightForestGreen
import com.example.ui.theme.WarmBorderColor
import com.example.util.BengaliUtils
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class ScannedDocItem(
    val file: File,
    val name: String,
    val formattedDate: String,
    val formattedSize: String,
    val isPdf: Boolean,
    val lastModifiedMs: Long
)

enum class ScanType {
    GENERAL,
    CARD_FRONT,
    CARD_BACK
}

@Composable
fun DocumentScannerScreen(
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) { context.findActivity() }

    var scannedDocs by remember { mutableStateOf<List<ScannedDocItem>>(emptyList()) }
    var previewDoc by remember { mutableStateOf<ScannedDocItem?>(null) }
    var deleteConfirmDoc by remember { mutableStateOf<ScannedDocItem?>(null) }

    // Card Scan state
    var pendingScanType by remember { mutableStateOf(ScanType.GENERAL) }
    var showCardScanDialog by remember { mutableStateOf(false) }
    var cardFrontUri by remember { mutableStateOf<Uri?>(null) }
    var cardBackUri by remember { mutableStateOf<Uri?>(null) }
    var showCardLayoutEditor by remember { mutableStateOf(false) }

    fun loadScannedFiles() {
        coroutineScope.launch(Dispatchers.IO) {
            val dir = File(context.filesDir, "scanned_documents")
            if (dir.exists()) {
                val files = dir.listFiles()?.filter {
                    it.isFile && (it.extension.equals("jpg", ignoreCase = true) ||
                            it.extension.equals("jpeg", ignoreCase = true) ||
                            it.extension.equals("pdf", ignoreCase = true))
                }?.sortedByDescending { it.lastModified() } ?: emptyList()

                val items = files.map { file ->
                    val dateStr = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date(file.lastModified()))
                    val sizeKb = file.length() / 1024
                    val sizeStr = if (sizeKb > 1024) {
                        String.format(Locale.US, "%.1f MB", sizeKb / 1024f)
                    } else {
                        "$sizeKb KB"
                    }
                    ScannedDocItem(
                        file = file,
                        name = file.name,
                        formattedDate = BengaliUtils.toBengaliDigits(dateStr),
                        formattedSize = BengaliUtils.toBengaliDigits(sizeStr),
                        isPdf = file.extension.equals("pdf", ignoreCase = true),
                        lastModifiedMs = file.lastModified()
                    )
                }
                withContext(Dispatchers.Main) {
                    scannedDocs = items
                }
            } else {
                withContext(Dispatchers.Main) {
                    scannedDocs = emptyList()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadScannedFiles()
    }

    // ML Kit Scanner Launcher
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            if (scanningResult != null) {
                when (pendingScanType) {
                    ScanType.GENERAL -> {
                        coroutineScope.launch(Dispatchers.IO) {
                            val savedCount = saveScanningResult(context, scanningResult)
                            withContext(Dispatchers.Main) {
                                loadScannedFiles()
                                if (savedCount > 0) {
                                    onShowSnackbar("$savedCount টি ডকুমেন্ট সেভ করা হয়েছে!")
                                } else {
                                    onShowSnackbar("ডকুমেন্ট সেভ করা সম্ভব হয়নি")
                                }
                            }
                        }
                    }
                    ScanType.CARD_FRONT -> {
                        val uri = scanningResult.pages?.firstOrNull()?.imageUri
                        if (uri != null) {
                            cardFrontUri = uri
                            onShowSnackbar("সামনের পাশ স্ক্যান সম্পন্ন!")
                        }
                    }
                    ScanType.CARD_BACK -> {
                        val uri = scanningResult.pages?.firstOrNull()?.imageUri
                        if (uri != null) {
                            cardBackUri = uri
                            onShowSnackbar("পেছনের পাশ স্ক্যান সম্পন্ন!")
                        }
                    }
                }
            }
        }
    }

    val startScan = { type: ScanType ->
        pendingScanType = type
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(if (type == ScanType.GENERAL) 20 else 1)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        if (activity != null) {
            GmsDocumentScanning.getClient(options)
                .getStartScanIntent(activity)
                .addOnSuccessListener { intentSender ->
                    scannerLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                }
                .addOnFailureListener { e ->
                    onShowSnackbar("স্ক্যানার চালু করতে সমস্যা: ${e.localizedMessage}")
                }
        } else {
            onShowSnackbar("অ্যাক্টিভিটি কনটেক্সট পাওয়া যায়নি")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4EE))
            .padding(14.dp)
    ) {
        // Scanner Banner Card with 2 Options (General Scan & Card Scan)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DarkForestGreen,
                                Color(0xFF1B4D3E)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "স্মার্ট ডকুমেন্ট স্ক্যানার",
                                fontFamily = HeadingFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "NID, স্টুডেন্ট কার্ড ও যেকোনো ডকুমেন্ট অটো-ক্রপ স্ক্যান করুন",
                                fontSize = 12.sp,
                                color = Color(0xFFE2D6C5)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: "সাধারণ স্ক্যান" (Single/Multi Page)
                    Button(
                        onClick = { startScan(ScanType.GENERAL) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_scan_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF28A745),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সাধারণ স্ক্যান (এক/একাধিক পেজ)",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 2: "কার্ড স্ক্যান (সামনে + পেছনে)"
                    OutlinedButton(
                        onClick = { showCardScanDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_card_scan_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "কার্ড স্ক্যান (সামনে + পেছনে)",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Saved Documents Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = DarkForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "স্ক্যান করা ডকুমেন্টসমূহ",
                    fontFamily = HeadingFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenText
                )
            }

            if (scannedDocs.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkForestGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "মোট: ${BengaliUtils.toBengaliDigits(scannedDocs.size.toString())} টি",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkForestGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (scannedDocs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, WarmBorderColor, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DarkForestGreen.copy(alpha = 0.08f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = DarkForestGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "এখনো কোনো ডকুমেন্ট স্ক্যান করা হয়নি",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "NID কার্ড, আইডি কার্ড বা বিলের ছবি তুলতে উপরে স্ক্যান অপশনে ট্যাপ করুন",
                        fontSize = 12.5.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(
                    items = scannedDocs,
                    key = { it.file.absolutePath }
                ) { doc ->
                    ScannedDocCard(
                        doc = doc,
                        onPreview = { previewDoc = doc },
                        onShare = { shareScannedDoc(context, doc.file) },
                        onDelete = { deleteConfirmDoc = doc }
                    )
                }
            }
        }
    }

    // Card Scan Dialog (Front & Back capture steps)
    if (showCardScanDialog) {
        Dialog(
            onDismissRequest = { showCardScanDialog = false }
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = DarkForestGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "কার্ড স্ক্যান (সামনে + পেছনে)",
                                fontFamily = HeadingFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkForestGreen
                            )
                        }

                        IconButton(onClick = { showCardScanDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ")
                        }
                    }

                    Text(
                        text = "NID/আইডি কার্ডের উভয় পাশ আলাদাভাবে স্ক্যান করে এক পেজে সুন্দরভাবে সাজান।",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Step 1: Front Side Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (cardFrontUri != null) Color(0xFFE8F5E9) else Color(0xFFF9F9F9)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (cardFrontUri != null) Color(0xFF28A745) else WarmBorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (cardFrontUri != null) {
                                    AsyncImage(
                                        model = cardFrontUri,
                                        contentDescription = "Front",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkForestGreen.copy(alpha = 0.1f),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("১ম", fontWeight = FontWeight.Bold, color = DarkForestGreen)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "১. সামনের পাশ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (cardFrontUri != null) "স্ক্যান সম্পন্ন হয়েছে" else "স্ক্যান করা হয়নি",
                                        fontSize = 11.5.sp,
                                        color = if (cardFrontUri != null) Color(0xFF28A745) else Color.Gray
                                    )
                                }
                            }

                            Button(
                                onClick = { startScan(ScanType.CARD_FRONT) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cardFrontUri != null) Color.Gray else DarkForestGreen
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (cardFrontUri != null) "পুনরায়" else "স্ক্যান")
                            }
                        }
                    }

                    // Step 2: Back Side Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (cardBackUri != null) Color(0xFFE8F5E9) else Color(0xFFF9F9F9)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (cardBackUri != null) Color(0xFF28A745) else WarmBorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (cardBackUri != null) {
                                    AsyncImage(
                                        model = cardBackUri,
                                        contentDescription = "Back",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkForestGreen.copy(alpha = 0.1f),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("২য়", fontWeight = FontWeight.Bold, color = DarkForestGreen)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "২. পেছনের পাশ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (cardBackUri != null) "স্ক্যান সম্পন্ন হয়েছে" else "স্ক্যান করা হয়নি",
                                        fontSize = 11.5.sp,
                                        color = if (cardBackUri != null) Color(0xFF28A745) else Color.Gray
                                    )
                                }
                            }

                            Button(
                                onClick = { startScan(ScanType.CARD_BACK) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cardBackUri != null) Color.Gray else DarkForestGreen
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (cardBackUri != null) "পুনরায়" else "স্ক্যান")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val canProceed = cardFrontUri != null && cardBackUri != null

                    Button(
                        onClick = {
                            if (canProceed) {
                                showCardScanDialog = false
                                showCardLayoutEditor = true
                            }
                        },
                        enabled = canProceed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkForestGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "লেআউট এডিটরে যান (এডিট ও সেভ)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    }
                }
            }
        }
    }

    // Full Screen Card Layout Editor
    if (showCardLayoutEditor && cardFrontUri != null && cardBackUri != null) {
        CardLayoutEditorDialog(
            frontUri = cardFrontUri!!,
            backUri = cardBackUri!!,
            onDismiss = { showCardLayoutEditor = false },
            onSave = { asPdf, frontRect, backRect, canvasSize ->
                coroutineScope.launch(Dispatchers.IO) {
                    val savedFile = renderAndSaveCardLayout(
                        context = context,
                        frontUri = cardFrontUri!!,
                        backUri = cardBackUri!!,
                        frontRect = frontRect,
                        backRect = backRect,
                        canvasSizeDp = canvasSize,
                        asPdf = asPdf
                    )
                    withContext(Dispatchers.Main) {
                        if (savedFile != null) {
                            onShowSnackbar(if (asPdf) "PDF কার্ড সেভ করা হয়েছে!" else "ছবি কার্ড সেভ করা হয়েছে!")
                            loadScannedFiles()
                            showCardLayoutEditor = false
                            cardFrontUri = null
                            cardBackUri = null
                        } else {
                            onShowSnackbar("সেভ করতে ব্যর্থ হয়েছে")
                        }
                    }
                }
            }
        )
    }

    // Image Preview Full Screen Modal
    previewDoc?.let { doc ->
        Dialog(
            onDismissRequest = { previewDoc = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = doc.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${doc.formattedDate} • ${doc.formattedSize}",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { shareScannedDoc(context, doc.file) }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "শেয়ার করুন",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { previewDoc = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "বন্ধ করুন",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.2f), thickness = 0.5.dp)

                    // Preview Content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (doc.isPdf) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPicture,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "PDF ডকুমেন্ট ফাইল",
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { openFileWithExternalApp(context, doc.file) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
                                ) {
                                    Text("PDF ভিউয়ারে খুলুন")
                                }
                            }
                        } else {
                            AsyncImage(
                                model = doc.file,
                                contentDescription = doc.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    deleteConfirmDoc?.let { doc ->
        AlertDialog(
            onDismissRequest = { deleteConfirmDoc = null },
            title = {
                Text(
                    text = "ডকুমেন্ট মুছে ফেলার নিশ্চিতকরণ",
                    fontFamily = HeadingFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("আপনি কি নিশ্চিত যে '${doc.name}' ডকুমেন্টটি চিরতরে মুছে ফেলতে চান?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = doc.file.delete()
                        if (deleted) {
                            onShowSnackbar("ডকুমেন্ট মুছে ফেলা হয়েছে")
                            loadScannedFiles()
                        } else {
                            onShowSnackbar("ডকুমেন্ট মোছা সম্ভব হয়নি")
                        }
                        deleteConfirmDoc = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("হ্যাঁ, মুছুন", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteConfirmDoc = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

data class CardRectState(
    var x: Float,
    var y: Float,
    var baseWidth: Float,
    var baseHeight: Float,
    var scale: Float
) {
    val currentWidth get() = baseWidth * scale
    val currentHeight get() = baseHeight * scale
}

@Composable
fun CardLayoutEditorDialog(
    frontUri: Uri,
    backUri: Uri,
    onDismiss: () -> Unit,
    onSave: (asPdf: Boolean, frontRect: CardRectState, backRect: CardRectState, canvasSize: androidx.compose.ui.geometry.Size) -> Unit
) {
    val density = LocalDensity.current.density

    // Canvas size in DP (approx A4 page aspect ratio)
    val canvasWidthDp = 320f
    val canvasHeightDp = 460f

    // Default positions & scales
    val baseCardWidth = 260f
    val baseCardHeight = 165f

    var frontX by remember { mutableFloatStateOf(30f) }
    var frontY by remember { mutableFloatStateOf(30f) }
    var frontScale by remember { mutableFloatStateOf(1.0f) }

    var backX by remember { mutableFloatStateOf(30f) }
    var backY by remember { mutableFloatStateOf(230f) }
    var backScale by remember { mutableFloatStateOf(1.0f) }

    fun resetLayout() {
        frontX = 30f
        frontY = 30f
        frontScale = 1.0f

        backX = 30f
        backY = 230f
        backScale = 1.0f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF1E1E1E)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkForestGreen)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "কার্ড লেআউট এডিটর",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = HeadingFontFamily
                        )
                    }

                    TextButton(onClick = { resetLayout() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("রিসেট", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Instruction Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D2D2D))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ছবি চেপে ধরে ড্র্যাগ করে পছন্দমতো পজিশনে বসান এবং সাইজ এডজাস্ট করুন",
                        fontSize = 11.5.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }

                // Page Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // White Document Page Container
                    Box(
                        modifier = Modifier
                            .width(canvasWidthDp.dp)
                            .height(canvasHeightDp.dp)
                            .background(Color.White, shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    ) {
                        // Front Card Box
                        val currentFrontW = baseCardWidth * frontScale
                        val currentFrontH = baseCardHeight * frontScale

                        Box(
                            modifier = Modifier
                                .offset { IntOffset((frontX * density).roundToInt(), (frontY * density).roundToInt()) }
                                .width(currentFrontW.dp)
                                .height(currentFrontH.dp)
                                .border(1.5.dp, DarkForestGreen, RoundedCornerShape(6.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val newX = frontX + dragAmount.x / density
                                        val newY = frontY + dragAmount.y / density
                                        frontX = newX.coerceIn(-20f, canvasWidthDp - 40f)
                                        frontY = newY.coerceIn(-20f, canvasHeightDp - 40f)
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = frontUri,
                                contentDescription = "Front Card",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .background(DarkForestGreen)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("সামনে", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Back Card Box
                        val currentBackW = baseCardWidth * backScale
                        val currentBackH = baseCardHeight * backScale

                        Box(
                            modifier = Modifier
                                .offset { IntOffset((backX * density).roundToInt(), (backY * density).roundToInt()) }
                                .width(currentBackW.dp)
                                .height(currentBackH.dp)
                                .border(1.5.dp, Color(0xFF28A745), RoundedCornerShape(6.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val newX = backX + dragAmount.x / density
                                        val newY = backY + dragAmount.y / density
                                        backX = newX.coerceIn(-20f, canvasWidthDp - 40f)
                                        backY = newY.coerceIn(-20f, canvasHeightDp - 40f)
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = backUri,
                                contentDescription = "Back Card",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .background(Color(0xFF28A745))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("পেছনে", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Controls & Size Sliders
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Sliders Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Front Slider
                            Column(modifier = Modifier.weight(1f)) {
                                Text("সামনের কার্ড সাইজ", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                Slider(
                                    value = frontScale,
                                    onValueChange = { frontScale = it },
                                    valueRange = 0.6f..1.4f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = DarkForestGreen,
                                        activeTrackColor = DarkForestGreen
                                    )
                                )
                            }

                            // Back Slider
                            Column(modifier = Modifier.weight(1f)) {
                                Text("পেছনের কার্ড সাইজ", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                Slider(
                                    value = backScale,
                                    onValueChange = { backScale = it },
                                    valueRange = 0.6f..1.4f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF28A745),
                                        activeTrackColor = Color(0xFF28A745)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Save Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val frontState = CardRectState(frontX, frontY, baseCardWidth, baseCardHeight, frontScale)
                                    val backState = CardRectState(backX, backY, baseCardWidth, baseCardHeight, backScale)
                                    onSave(false, frontState, backState, androidx.compose.ui.geometry.Size(canvasWidthDp, canvasHeightDp))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
                            ) {
                                Text("ছবি সেভ করুন (JPG)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val frontState = CardRectState(frontX, frontY, baseCardWidth, baseCardHeight, frontScale)
                                    val backState = CardRectState(backX, backY, baseCardWidth, baseCardHeight, backScale)
                                    onSave(true, frontState, backState, androidx.compose.ui.geometry.Size(canvasWidthDp, canvasHeightDp))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
                            ) {
                                Text("PDF সেভ করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScannedDocCard(
    doc: ScannedDocItem,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPreview() }
    ) {
        Column {
            // Thumbnail Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .background(Color(0xFFEFECE6)),
                contentAlignment = Alignment.Center
            ) {
                if (doc.isPdf) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = DarkForestGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PDF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkForestGreen
                        )
                    }
                } else {
                    AsyncImage(
                        model = doc.file,
                        contentDescription = doc.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // File Type Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (doc.isPdf) "PDF" else "JPG",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = doc.name,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = doc.formattedDate,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = doc.formattedSize,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkForestGreen
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = WarmBorderColor, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(4.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreview,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "দেখুন",
                            tint = DarkForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "শেয়ার করুন",
                            tint = LightForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "মুছে ফেলুন",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper function to save scanning result to internal storage
private fun saveScanningResult(
    context: Context,
    result: GmsDocumentScanningResult
): Int {
    var savedCount = 0
    val targetDir = File(context.filesDir, "scanned_documents")
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    // Save Pages as JPG
    val pages = result.pages
    if (pages != null && pages.isNotEmpty()) {
        pages.forEachIndexed { index, page ->
            val suffix = if (pages.size > 1) "_${index + 1}" else ""
            val fileName = "scan_${timestamp}$suffix.jpg"
            val destFile = File(targetDir, fileName)

            try {
                context.contentResolver.openInputStream(page.imageUri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                savedCount++
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Save PDF if present
    val pdf = result.pdf
    if (pdf != null) {
        val fileName = "scan_${timestamp}.pdf"
        val destFile = File(targetDir, fileName)
        try {
            context.contentResolver.openInputStream(pdf.uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (pages == null || pages.isEmpty()) {
                savedCount++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return savedCount
}

private suspend fun renderAndSaveCardLayout(
    context: Context,
    frontUri: Uri,
    backUri: Uri,
    frontRect: CardRectState,
    backRect: CardRectState,
    canvasSizeDp: androidx.compose.ui.geometry.Size,
    asPdf: Boolean
): File? = withContext(Dispatchers.IO) {
    try {
        val outWidth = 1200
        val outHeight = 1697 // standard ~A4 aspect ratio

        val bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val frontBitmap = loadBitmapFromUri(context, frontUri)
        val backBitmap = loadBitmapFromUri(context, backUri)

        val scaleX = outWidth / canvasSizeDp.width
        val scaleY = outHeight / canvasSizeDp.height

        if (frontBitmap != null) {
            val destRect = RectF(
                frontRect.x * scaleX,
                frontRect.y * scaleY,
                (frontRect.x + frontRect.currentWidth) * scaleX,
                (frontRect.y + frontRect.currentHeight) * scaleY
            )
            canvas.drawBitmap(frontBitmap, null, destRect, paint)
        }

        if (backBitmap != null) {
            val destRect = RectF(
                backRect.x * scaleX,
                backRect.y * scaleY,
                (backRect.x + backRect.currentWidth) * scaleX,
                (backRect.y + backRect.currentHeight) * scaleY
            )
            canvas.drawBitmap(backBitmap, null, destRect, paint)
        }

        val targetDir = File(context.filesDir, "scanned_documents")
        if (!targetDir.exists()) targetDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        if (asPdf) {
            val pdfFile = File(targetDir, "scan_card_$timestamp.pdf")
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(outWidth, outHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, paint)
            pdfDocument.finishPage(page)

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            pdfFile
        } else {
            val jpgFile = File(targetDir, "scan_card_$timestamp.jpg")
            FileOutputStream(jpgFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            jpgFile
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun shareScannedDoc(context: Context, file: File) {
    try {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)
        val mimeType = if (file.extension.equals("pdf", ignoreCase = true)) "application/pdf" else "image/jpeg"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "ডকুমেন্ট শেয়ার করুন"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun openFileWithExternalApp(context: Context, file: File) {
    try {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)
        val mimeType = if (file.extension.equals("pdf", ignoreCase = true)) "application/pdf" else "image/jpeg"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
