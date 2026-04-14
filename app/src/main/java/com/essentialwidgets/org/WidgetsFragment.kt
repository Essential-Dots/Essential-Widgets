package com.essentialwidgets.org

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class WidgetItem(
    val name: String,
    val size: String,
    val previewRes: Int
)

class WidgetsFragment : Fragment() {

    private val allWidgets = listOf(
        WidgetItem("Alarm", "2×1", R.drawable.alarm_widget_preview),
        WidgetItem("Digital Clock", "2×1", R.drawable.digital_clock2_widget_preview),
        WidgetItem("Analog Digital Clock", "2×2", R.drawable.analog_digital_clock_widget_preview),
        WidgetItem("Calendar", "2×2", R.drawable.calendar_widget_preview),
    )

    private lateinit var adapter: WidgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_widgets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rv_widgets)

        adapter = WidgetAdapter(allWidgets.toMutableList()) { widget ->
            addWidget(widget)
        }

        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = adapter

        view.findViewById<EditText>(R.id.et_search)
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s.toString().trim().lowercase()
                    val filtered = if (query.isEmpty()) allWidgets
                    else allWidgets.filter { it.name.lowercase().contains(query) }
                    adapter.updateList(filtered)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
    }

    private fun addWidget(widget: WidgetItem) {
        val appWidgetManager = AppWidgetManager.getInstance(requireContext())

        val provider = when (widget.name) {
            "Alarm" -> ComponentName(requireContext(), AlarmWidgetProvider::class.java)
            "Digital Clock" -> ComponentName(requireContext(), DigitalTime2Provider::class.java)
            "Analog Digital Clock" -> ComponentName(requireContext(), AnalogDigitalClockWidgetProvider::class.java)
            "Calendar" -> ComponentName(requireContext(), CalendarWidgetProvider::class.java)
            else -> return
        }

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(provider, null, null)
        }
    }
}

class WidgetAdapter(
    private val items: MutableList<WidgetItem>,
    private val onClick: (WidgetItem) -> Unit
) : RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder>() {

    inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvSize: TextView = itemView.findViewById(R.id.tv_size)
        val ivPreview: ImageView = itemView.findViewById(R.id.iv_preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_widget_card, parent, false)
        return WidgetViewHolder(v)
    }

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.name
        holder.tvSize.text = item.size
        holder.ivPreview.setImageResource(item.previewRes)

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<WidgetItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}