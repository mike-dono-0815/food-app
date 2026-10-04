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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guttracker.app.AppContainer
import com.guttracker.app.R
import com.guttracker.app.ui.components.AddTagDialog
import com.guttracker.app.ui.components.DigestionCard
import com.guttracker.app.ui.components.NotesCard
import com.guttracker.app.ui.components.TagRow
import com.guttracker.app.ui.components.WellbeingCard
import com.guttracker.app.ui.theme.AppColors
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                Text(java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)), style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
            }
        }
        Text("You can update this anytime today.", color = AppColors.TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(20.dp, 14.dp, 20.dp, 0.dp))

        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { WellbeingCard(state.wellbeingRating, viewModel::setWellbeing) }
            item { DigestionCard(state.digestionRating, viewModel::setDigestion) }
            item { TagRow("Today was", state.tags, state.contextTagId, viewModel::setTag, onAddClick = { showAddTag = true }) }
            item { NotesCard(state.notes, viewModel::setNotes) }
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
        AddTagDialog(onDismiss = { showAddTag = false }, onAdd = viewModel::addTag)
    }
}
