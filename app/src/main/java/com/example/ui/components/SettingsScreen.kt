package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@Composable
fun SettingsScreen(
    state: CurrentBillState,
    onCenterNameChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onPurchaserLabelChange: (String) -> Unit = {},
    onResetTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusCenterName = remember { FocusRequester() }
    val focusSubtitle = remember { FocusRequester() }
    val focusPurchaserLabel = remember { FocusRequester() }

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
                    text = "মেমোর হেডার তথ্য পরিবর্তন করুন:",
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
                    keyboardActions = KeyboardActions(onNext = { focusPurchaserLabel.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusSubtitle)
                        .testTag("subtitle_setting_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.purchaserLabel,
                    onValueChange = onPurchaserLabelChange,
                    label = { Text("স্বাক্ষরের টাইটেল (যেমন: ক্রেতার স্বাক্ষর / ক্রয়কারীর স্বাক্ষর)") },
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
                    text = "টেমপ্লেট রিসেট:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "নতুন দিনের বাজার বিলের জন্য ডিফল্ট টেমপ্লেটটি আবার নতুন করে শুরু করুন।",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onResetTemplate,
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonHeaderColor, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_template_button")
                ) {
                    Text("নতুন মেমো ডিফল্ট টেমপ্লেটে রিসেট করুন", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
