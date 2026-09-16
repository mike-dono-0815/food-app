package com.guttracker.app.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guttracker.app.AppContainer
import com.guttracker.app.ui.theme.AppColors

@Composable
fun OverviewScreen(container: AppContainer) {
    val viewModel: OverviewViewModel = viewModel(factory = viewModelFactory { initializer { OverviewViewModel(container) } })
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(AppColors.Background).verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.padding(20.dp, 10.dp, 20.dp, 0.dp)) {
            Text("Overview", style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
            Text("How you've been feeling", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
        }

        Row(
            modifier = Modifier
                .padding(20.dp, 16.dp, 20.dp, 0.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.Surface2)
                .padding(4.dp),
        ) {
            listOf(7 to "7D", 30 to "30D", 90 to "90D").forEach { (days, label) ->
                val selected = state.windowDays == days
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selected) AppColors.Accent else Color.Transparent)
                        .clickable { viewModel.setWindow(days) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, color = if (selected) Color.White else AppColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
            }
        }

        ChartCard(title = "Wellbeing", avg = state.wellbeingAvg) {
            TrendChart(points = state.points, valueOf = { it.wellbeing }, minVal = 1, maxVal = 10, colorScale = AppColors.WellbeingScale, gridValues = listOf(10, 5, 1))
        }
        Row(modifier = Modifier.padding(20.dp, 6.dp, 20.dp, 0.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(9.dp).clip(RoundedCornerShape(3.dp)).background(AppColors.Vacation))
            Text("Vacation", color = AppColors.TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        }

        ChartCard(title = "Digestion", avg = state.digestionAvg) {
            TrendChart(points = state.points, valueOf = { it.digestion }, minVal = 1, maxVal = 5, colorScale = AppColors.DigestionScale, gridValues = listOf(5, 3, 1))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ChartCard(title: String, avg: Double?, chart: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(20.dp, 14.dp, 20.dp, 0.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AppColors.Surface)
            .border(BorderStroke(1.dp, AppColors.Border), RoundedCornerShape(22.dp))
            .padding(18.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
            Text(avg?.let { "avg ${"%.1f".format(it)}" } ?: "no data yet", color = AppColors.TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(6.dp))
        chart()
    }
}
