package com.EssentialWidget.org

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

// Make sure to extend androidx.fragment.app.Fragment
class WidgetFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Make sure you have a 'fragment_home.xml' file in your 'res/layout' directory
        return inflater.inflate(R.layout.widget_page, container, false)
    }
}
