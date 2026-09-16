package com.guttracker.app.ui.rating

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guttracker.app.AppContainer
import com.guttracker.app.R
import com.guttracker.app.ui.theme.AppColors
import java.time.format.DateTimeFormatter

@Composable
fun RatingScreen(container: AppContainer, onBack: () -> Unit) {
    val viewModel: RatingViewModel = viewModel(factory = viewModelFactory { initializer { RatingViewModel(container) } })
    val state by viewModel.state.collectAsState()
    var showAddTag by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onBack() }
    if (!state.loaded) return

    Column(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
        Row(modifier = Modifier.padding(20.dp, 8.dp, 20.dp, 0.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(AppColors.Surface).border(BorderStroke(1.dp, AppColors.Border), CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = "Back", tint = AppColors.TextPrimary, modifier = Modifier.size(16.dp))
            }
            Column {
                Text("How are you today?", style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
                Text(java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
            }
        }
        Text("You can update this anytime today.", color = AppColors.TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(20.dp, 14.dp, 20.dp, 0.dp))

        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                RatingCard {
                    CardLabel("Wellbeing", center = true)
                    BigNumber(state.wellbeingRating)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                        for (i in 1..10) {
                            val selected = i == state.wellbeingRating
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.WellbeingScale[i - 1])
                                    .then(if (selected) Modifier.border(BorderStroke(2.5.dp, AppColors.TextPrimary), RoundedCornerShape(8.dp)) else Modifier)
                                    .clickable { viewModel.setWellbeing(i) },
                            )
                        }
                    }
                    EndLabels("Super Bad", "Perfect Day")
                }
            }
            item {
                RatingCard {
                    CardLabel("Digestion", center = true)
                    BigNumber(state.digestionRating)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.weight(1f))
                        for (i in 1..5) {
                            val filled = i <= state.digestionRating
                            Icon(
                                painterResource(if (filled) R.drawable.ic_drop_filled else R.drawable.ic_drop_outline),
                                contentDescription = null,
                                tint = if (filled) AppColors.Sage else AppColors.TextMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(30.dp).clickable { viewModel.setDigestion(i) },
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    EndLabels("No digestion", "Perfect digestion")
                }
            }
            item {
                RatingCard {
                    CardLabel("Today was", center = false)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.tags.forEach { tag ->
                            val selected = tag.id == state.contextTagId
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) AppColors.Accent else AppColors.Surface2)
                                    .clickable { viewModel.setTag(tag.id) }
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
                                .clickable { showAddTag = true }
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
            item {
                RatingCard {
                    CardLabel("Notes", center = false)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = viewModel::setNotes,
                        placeholder = { Text("Optional — anything worth remembering about today", color = AppColors.TextMuted) },
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
        }

        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = Color.White),
            ) {
                Text("Save today's rating", fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
            }
        }
    }

    if (showAddTag) {
        var newTagName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTag = false },
            title = { Text("New tag") },
            text = {
                OutlinedTextField(value = newTagName, onValueChange = { newTagName = it }, placeholder = { Text("e.g. Sick") }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTagName.isNotBlank()) viewModel.addTag(newTagName.trim())
                    showAddTag = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddTag = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun RatingCard(content: @Composable ColumnScope.() -> Unit) {
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
private fun CardLabel(text: String, center: Boolean) {
    Text(
        text.uppercase(),
        color = AppColors.TextMuted,
        style = MaterialTheme.typography.bodySmall,
        textAlign = if (center) TextAlign.Center else TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun BigNumber(value: Int) {
    Text(
        "$value",
        color = AppColors.TextPrimary,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 56.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    )
}

@Composable
private fun EndLabels(start: String, end: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(start, color = AppColors.TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        Text(end, color = AppColors.TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}
