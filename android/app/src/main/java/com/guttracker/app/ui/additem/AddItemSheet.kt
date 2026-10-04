package com.guttracker.app.ui.additem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.guttracker.app.AppContainer
import com.guttracker.app.R
import com.guttracker.app.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSheet(container: AppContainer, onDismiss: () -> Unit, timestampMillis: Long = System.currentTimeMillis()) {
    var name by remember { mutableStateOf("") }
    var saveToShortlist by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppColors.Surface) {
        Column(modifier = Modifier.padding(22.dp, 4.dp, 22.dp, 26.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Add new", style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary)
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(50)).background(AppColors.Surface2).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = "Close", tint = AppColors.TextPrimary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(20.dp))

            FieldLabel("Name")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("e.g. Pizza", color = AppColors.TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.Background,
                    unfocusedContainerColor = AppColors.Background,
                    focusedBorderColor = AppColors.Accent,
                    unfocusedBorderColor = AppColors.Border,
                ),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable { saveToShortlist = !saveToShortlist },
            ) {
                Checkbox(checked = saveToShortlist, onCheckedChange = { saveToShortlist = it }, colors = CheckboxDefaults.colors(checkedColor = AppColors.Accent))
                Text("Save to my shortlist for next time", color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(22.dp))

            if (error != null) {
                Text(error!!, color = AppColors.Danger, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 10.dp))
            }

            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) { error = "Enter a name"; return@Button }
                    saving = true
                    error = null
                    scope.launch {
                        if (saveToShortlist) {
                            container.catalogRepository.createFoodItem(trimmed).fold(
                                onSuccess = { item ->
                                    container.entryRepository.createEntry(timestampMillis, "food", item.id, null)
                                    onDismiss()
                                },
                                onFailure = { error = "Couldn't save — check your connection and try again"; saving = false },
                            )
                        } else {
                            container.entryRepository.createEntry(timestampMillis, "food", null, trimmed)
                            onDismiss()
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = Color.White),
            ) {
                Text("Log it", fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text.uppercase(),
        color = AppColors.TextMuted,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
