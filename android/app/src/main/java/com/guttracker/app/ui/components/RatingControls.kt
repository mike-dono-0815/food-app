package com.guttracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guttracker.app.R
import com.guttracker.app.data.local.TagEntity
import com.guttracker.app.ui.theme.AppColors
import java.util.Locale
import kotlin.math.round

@Composable
fun RatingCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AppColors.Surface)
            .border(BorderStroke(1.dp, AppColors.Border), RoundedCornerShape(22.dp))
            .padding(20.dp),
        content = content,
    )
}

@Composable
fun CardLabel(text: String, center: Boolean) {
    Text(
        text.uppercase(),
        color = AppColors.TextMuted,
        style = MaterialTheme.typography.bodySmall,
        textAlign = if (center) TextAlign.Center else TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun BigNumber(value: Double) {
    Text(
        formatRating(value),
        color = AppColors.TextPrimary,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 56.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    )
}

@Composable
fun EndLabels(start: String, end: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(start, color = AppColors.TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        Text(end, color = AppColors.TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}

fun formatRating(value: Double): String =
    if (value == round(value)) value.toInt().toString() else String.format(Locale.ENGLISH, "%.1f", value)

fun Double.roundToHalfStep(): Double = round(this * 2) / 2.0

/** Interpolates a color from a fixed-step scale (index i = value min+i) for a fractional value. */
fun scaleColor(colors: List<Color>, value: Double, min: Double, max: Double): Color {
    val t = (((value - min) / (max - min)).coerceIn(0.0, 1.0) * (colors.size - 1)).toFloat()
    val index = t.toInt().coerceIn(0, colors.size - 2)
    return lerp(colors[index], colors[index + 1], t - index)
}

@Composable
fun WellbeingCard(value: Double, onValueChange: (Double) -> Unit) {
    RatingCard {
        CardLabel("Wellbeing", center = true)
        BigNumber(value)
        val trackColor = scaleColor(AppColors.WellbeingScale, value, 1.0, 10.0)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble().roundToHalfStep()) },
            valueRange = 1f..10f,
            steps = 17, // 0.5 increments across 1..10
            colors = SliderDefaults.colors(thumbColor = trackColor, activeTrackColor = trackColor, inactiveTrackColor = AppColors.Border),
        )
        EndLabels("Super Bad", "Perfect Day")
    }
}

@Composable
fun DigestionCard(value: Double, onValueChange: (Double) -> Unit) {
    RatingCard {
        CardLabel("Digestion", center = true)
        BigNumber(value)
        val trackColor = scaleColor(AppColors.DigestionScale, value, 0.0, 5.0)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble().roundToHalfStep()) },
            valueRange = 0f..5f,
            steps = 9, // 0.5 increments across 0..5
            colors = SliderDefaults.colors(thumbColor = trackColor, activeTrackColor = trackColor, inactiveTrackColor = AppColors.Border),
        )
        EndLabels("No digestion", "Perfect digestion")
    }
}

@Composable
fun NotesCard(value: String, onValueChange: (String) -> Unit) {
    RatingCard {
        CardLabel("Notes", center = false)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Optional — anything worth remembering about this day", color = AppColors.TextMuted) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.Background,
                unfocusedContainerColor = AppColors.Background,
                focusedBorderColor = AppColors.Accent,
                unfocusedBorderColor = AppColors.Border,
            ),
            shape = RoundedCornerShape(14.dp),
        )
    }
}

@Composable
fun TagRow(label: String, tags: List<TagEntity>, selectedTagId: Int?, onSelectTag: (Int) -> Unit, onAddClick: () -> Unit) {
    RatingCard {
        CardLabel(label, center = false)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                val selected = tag.id == selectedTagId
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) AppColors.Accent else AppColors.Surface2)
                        .clickable { onSelectTag(tag.id) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(tag.name, color = if (selected) Color.White else AppColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(50))
                    .border(BorderStroke(1.5.dp, AppColors.Border), RoundedCornerShape(50))
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(painterResource(R.drawable.ic_plus), contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(12.dp))
                    Text("Add", color = AppColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun AddTagDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var newTagName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New tag") },
        text = {
            OutlinedTextField(value = newTagName, onValueChange = { newTagName = it }, placeholder = { Text("e.g. Sick") }, singleLine = true)
        },
        confirmButton = {
            TextButton(onClick = {
                if (newTagName.isNotBlank()) onAdd(newTagName.trim())
                onDismiss()
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
