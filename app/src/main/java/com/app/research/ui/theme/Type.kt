package com.app.research.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.app.research.R


val outFit = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
    Font(R.font.outfit_black, FontWeight.Black),
    Font(R.font.outfit_thin, FontWeight.Thin),
    Font(R.font.outfit_extralight, FontWeight.ExtraLight),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_light, FontWeight.Light),
    Font(R.font.outfit_medium, FontWeight.Medium)
)

val skaiFitnessTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),
    displayMedium = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),
    displaySmall = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),
    headlineLarge = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),
    headlineMedium = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),
    headlineSmall = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),

    titleLarge = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),

    titleMedium = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Medium,
    ),
    titleSmall = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Medium,
    ),

    bodyLarge = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),
    bodySmall = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Medium,
    ),
    labelMedium = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Medium,
    ),
    labelSmall = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.Medium,
    )
)