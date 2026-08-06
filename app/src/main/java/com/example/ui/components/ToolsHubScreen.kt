package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.LightForestGreen
import com.example.ui.theme.WarmBorderColor

data class AppToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isAvailable: Boolean = true,
    val badgeText: String = if (isAvailable) "সক্রিয়" else "শীঘ্রই আসছে",
    val accentColor: Color = DarkForestGreen
)

@Composable
fun ToolsHubScreen(
    onSelectFoodBillTool: () -> Unit,
    onSelectUpcomingTool: (title: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val toolsList = listOf(
        AppToolItem(
            id = "food_bill",
            title = "খাবার বিল মেমো",
            subtitle = "দৈনিক খাবার বাজার বিল তৈরি, প্রিন্ট ও হিসাব সংরক্ষণ করুন",
            icon = Icons.Default.Restaurant,
            isAvailable = true,
            badgeText = "চালু আছে",
            accentColor = DarkForestGreen
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4EE))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero Header Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            DarkForestGreen,
                            Color(0xFF133B2B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0x33FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ডিজিটাল টুলস হাব",
                            fontFamily = HeadingFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "আপনার প্রয়োজনীয় সকল টুলস এক জায়গায়",
                            fontSize = 13.sp,
                            color = Color(0xFFE2D6C5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x33FFFFFF))
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "প্রয়োজনীয় যেকোনো টুল নির্বাচন করে কাজ শুরু করুন:",
                    fontSize = 13.sp,
                    color = Color(0xFFF5EBE0),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "উপলব্ধ টুলস সূচী",
            fontFamily = HeadingFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreenText,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        toolsList.forEach { tool ->
            ToolCardItem(
                tool = tool,
                onClick = {
                    if (tool.id == "food_bill") {
                        onSelectFoodBillTool()
                    } else {
                        onSelectUpcomingTool(tool.title)
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Info Note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEAE2D5))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💡 আরও নতুন দৈনন্দিন টুলস খুব শীঘ্রই যোগ করা হবে!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A3E31)
            )
        }
    }
}

@Composable
fun ToolCardItem(
    tool: AppToolItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("tool_card_${tool.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (tool.isAvailable) Color.White else Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (tool.isAvailable) 3.dp else 1.dp
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (tool.isAvailable) tool.accentColor.copy(alpha = 0.12f)
                        else Color(0xFFE0E0E0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = if (tool.isAvailable) tool.accentColor else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tool.title,
                        fontFamily = HeadingFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tool.isAvailable) Color.Black else Color.DarkGray
                    )

                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (tool.isAvailable) DarkForestGreen
                                else Color(0xFF757575)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tool.badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tool.subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Arrow
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "খুলুন",
                tint = if (tool.isAvailable) DarkForestGreen else Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
