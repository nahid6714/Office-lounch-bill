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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FoodBillUiModel
import com.example.ui.theme.CreamPaperBg
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.LedgerRed
import com.example.ui.theme.StampBlue
import com.example.ui.theme.WarmBorderColor
import com.example.util.BengaliUtils

@Composable
fun BillHistoryList(
    bills: List<FoodBillUiModel>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditBill: (FoodBillUiModel) -> Unit,
    onPrintBill: (FoodBillUiModel) -> Unit,
    onSharePdfBill: (FoodBillUiModel) -> Unit,
    onDeleteBill: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var billToDelete by remember { mutableStateOf<FoodBillUiModel?>(null) }

    val filteredBills = remember(bills, searchQuery) {
        if (searchQuery.isBlank()) {
            bills
        } else {
            val q = searchQuery.lowercase().trim()
            val englishQ = BengaliUtils.toEnglishDigits(q)
            val bengaliQ = BengaliUtils.toBengaliDigits(q)
            bills.filter { bill ->
                bill.dateString.lowercase().contains(q) ||
                bill.dateString.lowercase().contains(englishQ) ||
                bill.dateString.lowercase().contains(bengaliQ) ||
                bill.purchaserName.lowercase().contains(q) ||
                bill.items.any { item -> 
                    item.name.lowercase().contains(q) || 
                    item.quantity.lowercase().contains(q) ||
                    item.quantity.lowercase().contains(englishQ) ||
                    item.quantity.lowercase().contains(bengaliQ)
                }
            }
        }
    }

    val totalSpent = remember(filteredBills) {
        filteredBills.sumOf { it.totalAmount }
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("তারিখ বা আইটেম দিয়ে খুঁজুন...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = DarkForestGreen,
                unfocusedBorderColor = WarmBorderColor
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_history_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Monthly / Total spending summary card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("summary_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkForestGreen)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "মোট খরচের হিসাব (${BengaliUtils.toBengaliDigits(filteredBills.size.toString())}টি বিল)",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = BengaliUtils.formatBengaliCurrency(totalSpent),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(36.dp).width(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredBills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "এখনো কোনো বিল সংরক্ষণ করা হয়নি।" else "কোনো সম্পর্কিত বিল পাওয়া যায়নি।",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().testTag("history_list_column")
            ) {
                items(filteredBills, key = { it.id }) { bill ->
                    HistoryBillItemCard(
                        bill = bill,
                        onEdit = { onEditBill(bill) },
                        onPrint = { onPrintBill(bill) },
                        onSharePdf = { onSharePdfBill(bill) },
                        onDelete = { billToDelete = bill }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    billToDelete?.let { bill ->
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = { Text("বিল মুছে ফেলুন") },
            text = { Text("${bill.dateString} তারিখের বিলটি মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBill(bill.id)
                        billToDelete = null
                    }
                ) {
                    Text("মুছুন", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { billToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun HistoryBillItemCard(
    bill: FoodBillUiModel,
    onEdit: () -> Unit,
    onPrint: () -> Unit,
    onSharePdf: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_card_${bill.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CreamPaperBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "তারিখ: ${BengaliUtils.toBengaliDigits(bill.dateString)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenText
                    )
                    if (bill.purchaserName.isNotBlank()) {
                        Text(
                            text = "ক্রয়কারী: ${bill.purchaserName}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Text(
                    text = BengaliUtils.formatBengaliCurrency(bill.totalAmount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkForestGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = WarmBorderColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Items Preview (up to 3 items)
            val previewItems = bill.items.take(4)
            Text(
                text = previewItems.joinToString(", ") { "${it.name} (${BengaliUtils.toBengaliDigits(it.quantity)})" } +
                        if (bill.items.size > 4) " এবং আরও ${BengaliUtils.toBengaliDigits((bill.items.size - 4).toString())}টি" else "",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_history_bill_${bill.id}")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = DarkForestGreen)
                }

                IconButton(
                    onClick = onPrint,
                    modifier = Modifier.testTag("print_history_bill_${bill.id}")
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = "প্রিন্ট করুন", tint = DarkForestGreen)
                }

                IconButton(
                    onClick = onSharePdf,
                    modifier = Modifier.testTag("share_history_bill_${bill.id}")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "পিডিএফ শেয়ার করুন", tint = DarkForestGreen)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_history_bill_${bill.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "মুছুন", tint = LedgerRed)
                }
            }
        }
    }
}
