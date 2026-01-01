package com.essentialwidgets.org.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.essentialwidgets.org.ui.theme.NDotFont
import com.essentialwidgets.org.ui.theme.Nserif

@Composable
fun WidgetsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp)
        ,
        contentAlignment = Alignment.TopCenter

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Essential Widgets",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = Nserif,
                fontWeight = FontWeight.Bold
            )
            val searchQuery = mutableStateOf("")

            SearchBar(
                query = searchQuery.value,
                onQueryChange = { searchQuery.value = it },
                onSearchClicked = { println("Cerca: $searchQuery") }
            )

        }
    }
}