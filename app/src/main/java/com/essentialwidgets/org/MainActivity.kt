package com.essentialwidgets.org

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.essentialwidgets.org.ui.AppNavigation
import com.essentialwidgets.org.ui.BottomBar
import com.essentialwidgets.org.ui.SearchBar
import com.essentialwidgets.org.ui.theme.EssentialWidgetTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EssentialWidgetTheme(){

                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AppNavigation(navController)
        }
    }

}