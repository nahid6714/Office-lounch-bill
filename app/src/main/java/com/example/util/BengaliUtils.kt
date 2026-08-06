package com.example.util

import java.text.DecimalFormat

object BengaliUtils {

    private val englishToBengaliDigitsMap = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )

    private val bengaliToEnglishDigitsMap = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(englishToBengaliDigitsMap[ch] ?: ch)
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(bengaliToEnglishDigitsMap[ch] ?: ch)
        }
        return sb.toString()
    }

    fun formatBengaliCurrency(amount: Double): String {
        val df = DecimalFormat("#,##0.##")
        val formattedNumber = df.format(amount)
        return toBengaliDigits(formattedNumber)
    }

    fun parseBengaliNumber(input: String): Double {
        val engString = toEnglishDigits(input).replace(",", "").trim()
        val direct = engString.toDoubleOrNull()
        if (direct != null) return direct

        val regex = Regex("""\d+(\.\d+)?""")
        val match = regex.find(engString)
        return match?.value?.toDoubleOrNull() ?: 0.0
    }

    fun parseBengaliInt(input: String): Int {
        return parseBengaliNumber(input).toInt()
    }

    val defaultQuickPresets = emptyList<QuickPreset>()
}

data class QuickPreset(
    val name: String,
    val defaultQty: String = "",
    val defaultRate: String = "",
    val defaultAmount: String = ""
)
