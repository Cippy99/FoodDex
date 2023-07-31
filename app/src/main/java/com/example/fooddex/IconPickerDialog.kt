package com.example.fooddex

import IconAdapter
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class IconPickerDialog(private val context: Context, private val iconList: List<Icon>) {

    fun show(onIconSelected: (Icon) -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.icon_selector, null)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val iconAdapter = IconAdapter(iconList)
        recyclerView.adapter = iconAdapter

        MaterialAlertDialogBuilder(context)
            .setTitle("Seleziona un'icona")
            .setView(view)
            .setPositiveButton("Seleziona"){ dialog, _ ->
                val selectedIcon = iconAdapter.getSelectedIcon()
                onIconSelected(selectedIcon)
                dialog.dismiss()
            }
            .setNegativeButton("Cancella", null)
            .show()
    }
}

