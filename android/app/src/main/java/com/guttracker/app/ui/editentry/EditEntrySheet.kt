package com.guttracker.app.ui.editentry

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
import androidx.compose.ui.window.Dialog
import com.guttracker.app.AppContainer
import com.guttracker.app.R
import com.guttracker.app.data.local.EntryEntity
import com.guttracker.app.ui.icons.ItemIcons
import com.guttracker.app.ui.theme.AppColors
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntrySheet(container: AppContainer, localId: Long, onDismiss: () -> Unit) {
    var entry by remember { mutableStateOf<EntryEntity?>(null) }
    var itemName by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf("food") }
    var dateTime by remember { mutableStateOf<java.time.LocalDateTime?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val zone = ZoneId.systemDefault()

    LaunchedEffect(localId) {
        val e = container.entryDao.getByLocalId(localId) ?: return@LaunchedEffect
        entry = e
        dateTime = Instant.ofEpochMilli(e.timestamp).atZone(zone).toLocalDateTime()
        if (e.label != null) {
            itemName = e.label
            category = e.type
        } else if (e.itemId != null) {
            val item = container.itemDao.getById(e.itemId)
            itemName = item?.name ?: "Item"
            category = item?.category ?: e.type
        }
    }

    if (entry == null || dateTime == null) return

    val dt = dateTime!!
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppColors.Surface) {
        Column(modifier = Modifier.padding(22.dp, 4.dp, 22.dp, 26.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                val bg = if (category == "drink") AppColors.AccentSoft else AppColors.SageSoft
                val ink = if (category == "drink") AppColors.Accent else AppColors.Sage
                val iconRes = itemName?.let { ItemIcons.iconFor(it) }
                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(bg), contentAlignment = Alignment.Center) {
                    if (iconRes != null) {
                        androidx.compose.foundation.Image(painter = painterResource(iconRes), contentDescription = null)
                    } else {
                        Text(ItemIcons.monogramFor(itemName ?: "?"), color = ink, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Text(itemName ?: "", style = MaterialTheme.typography.titleLarge, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(50)).background(AppColors.Surface2).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = "Close", tint = AppColors.TextPrimary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(22.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EditField("Date", dt.toLocalDate().format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)), Modifier.weight(1f)) { showDatePicker = true }
                EditField("Time", dt.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)), Modifier.weight(1f)) { showTimePicker = true }
            }
            Spacer(Modifier.height(22.dp))

            Button(
                onClick = {
                    scope.launch {
                        val millis = dt.atZone(zone).toInstant().toEpochMilli()
                        container.entryRepository.editEntry(localId, millis)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = Color.White),
            ) {
                Text("Save changes", fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .clickable {
                        scope.launch {
                            container.entryRepository.deleteEntry(localId)
                            onDismiss()
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_trash), contentDescription = null, tint = AppColors.Danger, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(7.dp))
                Text("Delete this entry", color = AppColors.Danger, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dt.toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val newDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        dateTime = dt.toLocalTime().let { newDate.atTime(it) }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = dt.hour, initialMinute = dt.minute)
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = AppColors.Surface) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = state)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            dateTime = dt.toLocalDate().atTime(LocalTime.of(state.hour, state.minute))
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditField(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier) {
        Text(label.uppercase(), color = AppColors.TextMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.Background)
                .border(BorderStroke(1.5.dp, AppColors.Border), RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(value, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
        }
    }
}
