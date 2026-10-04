package com.guttracker.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guttracker.app.R
import com.guttracker.app.ui.icons.ItemIcons
import com.guttracker.app.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconTile(name: String, category: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (category == "drink") AppColors.AccentSoft else AppColors.SageSoft
    val ink = if (category == "drink") AppColors.Accent else AppColors.Sage
    val icon = ItemIcons.iconFor(name)

    // TooltipBox wires up long-press-to-show on its content automatically (see
    // BasicTooltipBox's handleGestures), so the inner clickable only needs the tap handler.
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(name) } },
        state = rememberTooltipState(),
    ) {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Image(painter = painterResource(icon), contentDescription = name)
            } else {
                Text(ItemIcons.monogramFor(name), color = ink, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
            }
        }
    }
}

@Composable
fun AddIconTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, AppColors.Border), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(R.drawable.ic_plus), contentDescription = "Add new", tint = AppColors.TextSecondary)
    }
}
