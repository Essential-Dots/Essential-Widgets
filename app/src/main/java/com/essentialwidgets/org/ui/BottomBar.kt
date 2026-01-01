package com.essentialwidgets.org.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.contentColorFor
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.essentialwidgets.org.R
import com.essentialwidgets.org.ui.theme.EssentialWidgetTheme
import com.essentialwidgets.org.ui.theme.n_gray
import com.essentialwidgets.org.ui.theme.n_red


data class BottomNavItem(
    val route: String,
    val icon: Int,
    val contentDescription: String ?= null
)

val items = listOf(
    BottomNavItem("widgets",R.drawable.ic_widgets),
    BottomNavItem("favorites", R.drawable.ic_favorites)
)



@Composable
fun BottomBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    if(currentRoute != null)
    PillNavigationBar(
        items = items,
        currentRoute = currentRoute,
        onItemClick = { item ->
            navController.navigate(item.route) {
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}

@Composable
fun PillNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemClick: (BottomNavItem) -> Unit
) {
    Row(
        // pill modifiers
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(50.dp)
            )
            .border(
                width = 0.1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceAround,

    ) {
        // bottom navigation items ui
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            IconButton(
                onClick = { onItemClick(item) },
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                Icon(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.contentDescription,
                    tint = if (isSelected) n_red else n_gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}