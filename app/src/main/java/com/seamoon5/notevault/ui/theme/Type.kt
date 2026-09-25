package com.seamoon5.notevault.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

private val Sans = FontFamily.SansSerif

private fun style(
    size: Int,
    weight: FontWeight,
    lineHeight: Int = (size * 1.35).toInt(),
    letter: Double = 0.0
) = TextStyle(
    fontFamily = Sans,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letter.sp,
    textAlign = TextAlign.Start
)

val NoteVaultTypography = Typography(
    displaySmall = style(30, FontWeight.Bold, 36, (-0.5)),
    headlineMedium = style(26, FontWeight.Bold, 32, (-0.4)),
    headlineSmall = style(22, FontWeight.Bold, 28, (-0.3)),
    titleLarge = style(20, FontWeight.SemiBold, 26, (-0.2)),
    titleMedium = style(16, FontWeight.SemiBold, 22, 0.1),
    titleSmall = style(14, FontWeight.SemiBold, 20, 0.1),
    bodyLarge = style(16, FontWeight.Normal, 24, 0.15),
    bodyMedium = style(14, FontWeight.Normal, 21, 0.2),
    bodySmall = style(12, FontWeight.Normal, 17, 0.3),
    labelLarge = style(14, FontWeight.Medium, 20, 0.1),
    labelMedium = style(12, FontWeight.Medium, 16, 0.5),
    labelSmall = style(11, FontWeight.Medium, 15, 0.5)
)
