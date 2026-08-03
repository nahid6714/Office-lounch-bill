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
        return "${toBengaliDigits(formattedNumber)} টাকা"
    }

    fun parseBengaliNumber(input: String): Double {
        val engString = toEnglishDigits(input).replace(",", "").trim()
        return engString.toDoubleOrNull() ?: 0.0
    }

    val defaultQuickPresets = listOf(
        QuickPreset("চাল", "২ কেজি"),
        QuickPreset("ডাল", "২ কেজি"),
        QuickPreset("লবণ", "১ প্যাকেট"),
        QuickPreset("মুরগি", "২ কেজি"),
        QuickPreset("আলু", "১ কেজি"),
        QuickPreset("সয়াবিন তেল", "২ লিটার"),
        QuickPreset("ডিম", "১ ডজন"),
        QuickPreset("মাছ", "১.৫ কেজি"),
        QuickPreset("পেঁয়াজ", "১ কেজি"),
        QuickPreset("রসুন ও আদা", "২৫০ গ্রাম"),
        QuickPreset("কাঁচা মরিচ", "২৫০ গ্রাম"),
        QuickPreset("লেবু", "৪ টি"),
        QuickPreset("শাক-সবজি", "১ আঁটি"),
        QuickPreset("গ্যাস সিলিন্ডার", "১ টি")
    )
}

data class QuickPreset(
    val name: String,
    val defaultQty: String
)
