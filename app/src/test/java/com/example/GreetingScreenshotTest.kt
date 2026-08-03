package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.BillItem
import com.example.ui.CurrentBillState
import com.example.ui.components.MemoVoucherCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        MemoVoucherCard(
          state = CurrentBillState(
            editingBillId = 1L,
            dateString = "03/08/2026",
            centerName = "আল বারাকা মেডিকেল সেন্টার",
            subtitle = "দৈনিক খাবার বিল",
            purchaserName = "কুলসুম",
            items = listOf(
              BillItem(name = "চাল", quantity = "২ কেজি", rate = "60", amount = 120.0),
              BillItem(name = "ডাল", quantity = "২ কেজি", rate = "110", amount = 220.0),
              BillItem(name = "লবণ", quantity = "১ প্যাকেট", rate = "40", amount = 40.0),
              BillItem(name = "মুরগি", quantity = "২ কেজি", rate = "180", amount = 360.0)
            )
          ),
          onUpdateDateClick = {},
          onUpdateItemName = { _, _ -> },
          onUpdateItemQty = { _, _ -> },
          onUpdateItemRate = { _, _ -> },
          onUpdateItemAmount = { _, _ -> },
          onRemoveItem = {},
          onAddItemRow = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

