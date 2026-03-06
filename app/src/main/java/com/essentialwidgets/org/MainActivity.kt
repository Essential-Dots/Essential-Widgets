package com.essentialwidgets.org

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    // Dichiarazione delle view che useremo
    private lateinit var navIconWidgets: ImageView
    private lateinit var navIconFavorites: ImageView
    private lateinit var customNavBar: View
    private var selectedIcon: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForegroundService(Intent(this, ThemeWatcherService::class.java))
        setContentView(R.layout.activity_main)


        customNavBar = findViewById(R.id.custom_nav_bar)
        navIconWidgets = customNavBar.findViewById(R.id.nav_icon_widgets)
        navIconFavorites = customNavBar.findViewById(R.id.nav_icon_favorites)

        setupNavigation()

        // Shows the fist Fragment (Widgets Page)
        if (savedInstanceState == null) {
            // Selects the first icon (Widgets)
            selectIcon(navIconWidgets)
            replaceFragment(WidgetsFragment())
        }
    }

    private fun setupNavigation() {
        navIconWidgets.setOnClickListener {
            selectIcon(it as ImageView)
            replaceFragment(WidgetsFragment())
        }

        navIconFavorites.setOnClickListener {
            selectIcon(it as ImageView)
            replaceFragment(FavoritesFragment())
        }
        // Add other navigation items as needed
    }

    private fun selectIcon(icon: ImageView) {
        // Removes the selection from the previously selected icon
        selectedIcon?.isSelected = false

        // Selects the new icon
        icon.isSelected = true
        selectedIcon = icon
    }

    // This function replaces the current fragment with the new one
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

}
