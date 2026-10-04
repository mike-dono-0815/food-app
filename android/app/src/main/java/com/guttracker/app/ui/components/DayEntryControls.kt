package com.guttracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.guttracker.app.R
import com.guttracker.app.data.local.ItemEntity
import com.guttracker.app.ui.theme.AppColors
import com.guttracker.app.util.millisToTimeOfDay

@Composable
fun SectionHeader(title: String, hint: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp, 16.dp, 20.dp, 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
        if (hint != null) Text(hint, style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
    }
}

@Composable
fun ItemGrid(items: List<ItemEntity>, onTap: (ItemEntity) -> Unit, showAdd: Boolean, onAdd: () -> Unit) {
    val columns = 5
    val rows = kotlin.math.ceil((items.size + if (showAdd) 1 else 0) / columns.toFloat()).toInt().coerceAtLeast(1)
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            index < items.size -> {
                                val item = items[index]
                                IconTile(name = item.name, category = item.category, onClick = { onTap(item) })
                            }
                            showAdd && index == items.size -> AddIconTile(onClick = onAdd)
                            else -> Spacer(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

/** A localId + display name + timestamp for a logged entry, shared by any screen that lists entries for a day. */
data class EntryDisplay(val localId: Long, val timestampMillis: Long, val name: String)

@Composable
fun LoggedEntriesCard(entries: List<EntryDisplay>, emptyText: String, onEdit: (Long) -> Unit) {
    Box(Modifier.padding(20.dp, 10.dp, 20.dp, 0.dp)) {
        if (entries.isEmpty()) {
            Text(emptyText, color = AppColors.TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(4.dp))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(AppColors.Surface)
                    .border(BorderStroke(1.dp, AppColors.Border), RoundedCornerShape(22.dp)),
            ) {
                entries.forEachIndexed { index, entry ->
                    if (index > 0) HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEdit(entry.localId) }
                            .padding(16.dp, 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                millisToTimeOfDay(entry.timestampMillis),
                                color = AppColors.TextMuted,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(64.dp),
                            )
                            Text(entry.name, color = AppColors.TextPrimary, style = MaterialTheme.typography.bodyLarge)
                        }
                        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Edit", tint = AppColors.TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
