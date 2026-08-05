package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val NotoSerifBengaliFont = GoogleFont("Noto Serif Bengali")
val HindSiliguriFont = GoogleFont("Hind Siliguri")

val NotoSerifBengaliFamily = FontFamily(
    Font(googleFont = NotoSerifBengaliFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = NotoSerifBengaliFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = NotoSerifBengaliFont, fontProvider = fontProvider, weight = FontWeight.SemiBold)
)

val HindSiliguriFamily = FontFamily(
    Font(googleFont = HindSiliguriFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = HindSiliguriFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = HindSiliguriFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = HindSiliguriFont, fontProvider = fontProvider, weight = FontWeight.SemiBold)
)

val HeadingFontFamily = NotoSerifBengaliFamily
val BodyFontFamily = HindSiliguriFamily

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleSmall = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)
