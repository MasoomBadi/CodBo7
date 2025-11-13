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

// --------------------------------------
// Inter Font Family (All UI Typography)
// --------------------------------------

private val InterRegular = Font(
    googleFont = GoogleFont("Inter"),
    fontProvider = fontProvider,
    weight = androidx.compose.ui.text.font.FontWeight.Normal
)

private val InterMedium = Font(
    googleFont = GoogleFont("Inter"),
    fontProvider = fontProvider,
    weight = androidx.compose.ui.text.font.FontWeight.Medium
)

private val InterSemiBold = Font(
    googleFont = GoogleFont("Inter"),
    fontProvider = fontProvider,
    weight = androidx.compose.ui.text.font.FontWeight.SemiBold
)

private val InterBold = Font(
    googleFont = GoogleFont("Inter"),
    fontProvider = fontProvider,
    weight = androidx.compose.ui.text.font.FontWeight.Bold
)

// Complete Inter Font Family
val InterFontFamily = FontFamily(
    InterRegular,
    InterMedium,
    InterSemiBold,
    InterBold
)

// --------------------------------------
// Material 3 Base Typography Reference
// --------------------------------------
private val baseline = Typography()

// --------------------------------------
// Final BO7 Typography
// --------------------------------------

val BO7Typography = Typography(

    // Display (Large Titles / Main Hero Text)
    displayLarge = baseline.displayLarge.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    displayMedium = baseline.displayMedium.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    displaySmall = baseline.displaySmall.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),

    // Headlines (Section Titles)
    headlineLarge = baseline.headlineLarge.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),

    // Titles (Toolbars, Settings Titles, Cards)
    titleLarge = baseline.titleLarge.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    titleSmall = baseline.titleSmall.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),

    // Body (Content paragraphs)
    bodyLarge = baseline.bodyLarge.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),
    bodyMedium = baseline.bodyMedium.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),
    bodySmall = baseline.bodySmall.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),

    // Labels (Buttons, Chips, Small Controls)
    labelLarge = baseline.labelLarge.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    labelSmall = baseline.labelSmall.copy(
        fontFamily = InterFontFamily,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
)
