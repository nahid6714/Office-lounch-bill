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

    fun formatDigits(input: String, isEnglish: Boolean): String {
        return if (isEnglish) toEnglishDigits(input) else toBengaliDigits(input)
    }

    fun formatCurrency(amount: Double, isEnglish: Boolean): String {
        val df = DecimalFormat("#,##0.##")
        val formattedNumber = df.format(amount)
        return if (isEnglish) formattedNumber else toBengaliDigits(formattedNumber)
    }

    fun convertNumberToBengaliWords(amount: Long): String {
        if (amount <= 0) return "শূন্য টাকা মাত্র"
        val units = arrayOf("", "এক", "দুই", "তিন", "চার", "পাঁচ", "ছয়", "সাত", "আট", "নয়")
        val teens = arrayOf("দশ", "এগারো", "বারো", "তেরো", "চৌদ্দ", "পনেরো", "ষোলো", "সতেরো", "আঠারো", "উনিশ")
        val tens = arrayOf("", "", "বিশ", "ত্রিশ", "চল্লিশ", "পঞ্চাশ", "ষাট", "সত্তর", "আশি", "নব্বই")

        fun convertUnder100(n: Int): String {
            if (n <= 0) return ""
            if (n < 10) return units[n]
            if (n < 20) return teens[n - 10]
            val t = n / 10
            val u = n % 10
            return if (u == 0) tens[t] else "${tens[t]} ${units[u]}"
        }

        fun convertUnder1000(n: Int): String {
            val hundred = n / 100
            val remainder = n % 100
            var res = ""
            if (hundred > 0) {
                res += when (hundred) {
                    1 -> "একশত"
                    2 -> "দুইশত"
                    3 -> "তিনশত"
                    4 -> "চারশত"
                    5 -> "পাঁচশত"
                    6 -> "ছয়শত"
                    7 -> "সাতশত"
                    8 -> "আটশত"
                    9 -> "নয়শত"
                    else -> "${units[hundred]} শত"
                }
            }
            if (remainder > 0) {
                if (res.isNotBlank()) res += " "
                res += convertUnder100(remainder)
            }
            return res
        }

        var num = amount
        var result = ""

        if (num >= 10000000) {
            val crore = (num / 10000000).toInt()
            result += "${convertUnder100(crore)} কোটি "
            num %= 10000000
        }
        if (num >= 100000) {
            val lakh = (num / 100000).toInt()
            result += "${convertUnder100(lakh)} লাখ "
            num %= 100000
        }
        if (num >= 1000) {
            val thousand = (num / 1000).toInt()
            result += "${convertUnder100(thousand)} হাজার "
            num %= 1000
        }
        if (num > 0) {
            result += convertUnder1000(num.toInt())
        }

        return result.trim() + " টাকা মাত্র"
    }

    fun convertNumberToEnglishWords(amount: Long): String {
        if (amount <= 0) return "Zero Taka Only"
        val units = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

        fun convertUnder1000(n: Int): String {
            var str = ""
            val h = n / 100
            val rem = n % 100
            if (h > 0) {
                str += units[h] + " Hundred "
            }
            if (rem < 20) {
                str += units[rem]
            } else {
                str += tens[rem / 10] + if (rem % 10 > 0) "-" + units[rem % 10] else ""
            }
            return str.trim()
        }

        var num = amount
        var result = ""

        if (num >= 10000000) {
            val crore = (num / 10000000).toInt()
            result += convertUnder1000(crore) + " Crore "
            num %= 10000000
        }
        if (num >= 100000) {
            val lakh = (num / 100000).toInt()
            result += convertUnder1000(lakh) + " Lakh "
            num %= 100000
        }
        if (num >= 1000) {
            val thousand = (num / 1000).toInt()
            result += convertUnder1000(thousand) + " Thousand "
            num %= 1000
        }
        if (num > 0) {
            result += convertUnder1000(num.toInt()) + " "
        }

        return result.trim() + " Taka Only"
    }

    fun convertAmountToWords(amount: Double, isEnglish: Boolean): String {
        val wholePart = amount.toLong()
        return if (isEnglish) {
            convertNumberToEnglishWords(wholePart)
        } else {
            convertNumberToBengaliWords(wholePart)
        }
    }

    fun formatPresetDisplayText(preset: QuickPreset, isEnglish: Boolean = false): String {
        val name = preset.name.trim()
        val qty = preset.defaultQty.trim()
        val priceRaw = preset.defaultRate.ifBlank { preset.defaultAmount }.trim()

        val qtyFormatted = if (qty.isNotBlank()) formatDigits(qty, isEnglish) else ""
        val priceFormatted = if (priceRaw.isNotBlank()) {
            if (isEnglish) "Price: ${formatDigits(priceRaw, true)}৳" else "টাকা: ${toBengaliDigits(priceRaw)}৳"
        } else ""

        val infoPart = when {
            qtyFormatted.isNotBlank() && priceFormatted.isNotBlank() -> "$qtyFormatted | $priceFormatted"
            qtyFormatted.isNotBlank() -> qtyFormatted
            priceFormatted.isNotBlank() -> priceFormatted
            else -> ""
        }

        return if (infoPart.isNotBlank()) {
            "$name ($infoPart)"
        } else {
            name
        }
    }

    val defaultQuickPresets = emptyList<QuickPreset>()
}

data class QuickPreset(
    val name: String,
    val defaultQty: String = "",
    val defaultRate: String = "",
    val defaultAmount: String = ""
)
