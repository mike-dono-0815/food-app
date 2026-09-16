package com.guttracker.app.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val Background = Color(0xFFFAF6F1)
    val Surface = Color(0xFFFFFFFF)
    val Surface2 = Color(0xFFF2EBE1)
    val TextPrimary = Color(0xFF2B2621)
    val TextSecondary = Color(0xFF6B6259)
    val TextMuted = Color(0xFF9A9186)
    val Border = Color(0x17282621)
    val Accent = Color(0xFFC1622F)
    val AccentSoft = Color(0xFFF4E2D5)
    val Sage = Color(0xFF4F8F56)
    val SageSoft = Color(0xFFE6EFE3)
    val Vacation = Color(0xFF6B84B0)
    val VacationBand = Color(0x1F6B84B0)
    val Danger = Color(0xFFB0483F)
    val Gridline = Color(0xFFE6E0D6)

    // diverging wellbeing scale, index 0 = "Super Bad" .. 9 = "Perfect Day"
    val WellbeingScale = listOf(
        Color(0xFFC8503F), Color(0xFFBE6050), Color(0xFFB47061), Color(0xFFA97F72), Color(0xFF9F8F84),
        Color(0xFF92967E), Color(0xFF81947A), Color(0xFF70936E), Color(0xFF609162), Color(0xFF4F8F56),
    )

    // sequential sage ramp, index 0 = digestion 1 .. 4 = digestion 5
    val DigestionScale = listOf(Color(0xFFDCEBDC), Color(0xFFB9D6BA), Color(0xFF8FBD92), Color(0xFF5FA066), Color(0xFF3D7A44))
}
