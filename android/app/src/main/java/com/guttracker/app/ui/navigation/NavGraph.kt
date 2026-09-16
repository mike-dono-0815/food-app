package com.guttracker.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.guttracker.app.AppContainer
import com.guttracker.app.R
import com.guttracker.app.ui.overview.OverviewScreen
import com.guttracker.app.ui.rating.RatingScreen
import com.guttracker.app.ui.theme.AppColors
import com.guttracker.app.ui.today.TodayScreen

private const val ROUTE_TODAY = "today"
private const val ROUTE_OVERVIEW = "overview"
private const val ROUTE_RATING = "rating"

@Composable
fun AppNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showTabBar = currentRoute == ROUTE_TODAY || currentRoute == ROUTE_OVERVIEW

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            if (showTabBar) {
                BottomTabBar(
                    currentRoute = currentRoute,
                    onToday = { navController.navigate(ROUTE_TODAY) { popUpTo(ROUTE_TODAY) { inclusive = true } } },
                    onOverview = { navController.navigate(ROUTE_OVERVIEW) { popUpTo(ROUTE_TODAY) } },
                )
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = ROUTE_TODAY, modifier = Modifier.padding(padding)) {
            composable(ROUTE_TODAY) {
                TodayScreen(container = container, onOpenRating = { navController.navigate(ROUTE_RATING) })
            }
            composable(ROUTE_OVERVIEW) {
                OverviewScreen(container = container)
            }
            composable(ROUTE_RATING) {
                RatingScreen(container = container, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomTabBar(currentRoute: String?, onToday: () -> Unit, onOverview: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .drawBehind { drawLine(AppColors.Border, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 2f) }
            .background(AppColors.Surface),
    ) {
        TabItem(Modifier.weight(1f), R.drawable.ic_home_tab, "Today", currentRoute == ROUTE_TODAY, onToday)
        TabItem(Modifier.weight(1f), R.drawable.ic_chart_tab, "Overview", currentRoute == ROUTE_OVERVIEW, onOverview)
    }
}

@Composable
private fun TabItem(modifier: Modifier, iconRes: Int, label: String, active: Boolean, onClick: () -> Unit) {
    val color = if (active) AppColors.Accent else AppColors.TextMuted
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(iconRes), contentDescription = label, tint = color, modifier = Modifier.size(21.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = color, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
    }
}
