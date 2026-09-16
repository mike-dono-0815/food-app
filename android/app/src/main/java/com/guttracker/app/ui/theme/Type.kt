package com.guttracker.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// The mockups use Manrope; this build uses the system sans-serif instead of bundling font
// files, so weights/spacing are approximated rather than pixel-matched.
val AppTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 19.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.5.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.5.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp),
)
