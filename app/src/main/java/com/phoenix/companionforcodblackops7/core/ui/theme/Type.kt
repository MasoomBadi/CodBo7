package com.phoenix.companionforcodblackops7.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.phoenix.companionforcodblackops7.R


// Google Fonts Provider
val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val RajdhaniLight = Font(
    googleFont = GoogleFont("Rajdhani"),
    fontProvider = fontProvider,
    weight = androidx.compose.ui.text.font.FontWeight.Light
)

private val RajdhaniRegular = Font(
    googleFont = GoogleFont("Rajdhani"),
    fontProvider = fontProvider,
    weight = androidx.compose.ui.text.font.FontWeight.Normal
)

private val RajdhaniMedium = Font(
    googleFont = GoogleFont("Rajdhani"),
    fontProvider = fontProvider,
    weight = androidx.compose.ui.text.font.FontWeight.Medium
)

private val RajdhaniSemiBold = Font(
    googleFont = GoogleFont("Rajdhani"),
    fontProvider = fontProvider,
    weight = androidx.compose.ui.text.font.FontWeight.SemiBold
)

private val RajdhaniBold = Font(
    googleFont = GoogleFont("Rajdhani"),
    fontProvider = fontProvider,
    weight = androidx.compose.ui.text.font.FontWeight.Bold
)

val RajdhaniFontFamily = FontFamily(
    RajdhaniLight,
    RajdhaniRegular,
    RajdhaniMedium,
    RajdhaniSemiBold,
    RajdhaniBold
)

// --------------------------------------
// Material 3 Base Typography Reference
// --------------------------------------
private val baseline = Typography()

// --------------------------------------
// Final BO7 Typography
// --------------------------------------

val BO7Typography = Typography(

    displayLarge = baseline.displayLarge.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    displayMedium = baseline.displayMedium.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    displaySmall = baseline.displaySmall.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),

    headlineLarge = baseline.headlineLarge.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),

    titleLarge = baseline.titleLarge.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    titleSmall = baseline.titleSmall.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),

    bodyLarge = baseline.bodyLarge.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),
    bodyMedium = baseline.bodyMedium.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),
    bodySmall = baseline.bodySmall.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),

    labelLarge = baseline.labelLarge.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    labelSmall = baseline.labelSmall.copy(
        fontFamily = RajdhaniFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
)
