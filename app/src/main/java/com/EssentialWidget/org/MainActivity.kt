package com.EssentialWidget.org

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Se viene premuto "home", sostituisci il contenitore con HomeFragment
                    replaceFragment(WidgetFragment())
                    true // Indica che l'evento è stato gestito
                }
                R.id.favorite -> {
                    // Se viene premuto "favorite", sostituisci con FavoriteFragment
                    replaceFragment(FavoriteFragment())
                    true // Indica che l'evento è stato gestito
                }
                else -> false
            }
        }

        // All'avvio dell'app, se non stiamo ripristinando da uno stato precedente,
        // seleziona programmaticamente la voce "home".
        // Questo attiverà il listener qui sopra e caricherà HomeFragment.
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    // Una funzione di supporto per rendere il codice più pulito
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}