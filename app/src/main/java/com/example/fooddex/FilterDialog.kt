package com.example.fooddex

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FilterDialog(private val context: Context, private val onPositiveButtonClick: (Map<String, Boolean>) -> Unit,
                   private val onNegativeButtonClick: () -> Unit, private val appliedFilters: MutableMap<String, Boolean>) {

    private val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_filter_recipe, null)

    private var selectedChips =  mutableMapOf<String, Boolean>()

    private val chipPrimi = dialogView.findViewById<Chip>(R.id.chipPrimi)
    private val chipSecondi = dialogView.findViewById<Chip>(R.id.chipSecondi)
    private val chipDolci = dialogView.findViewById<Chip>(R.id.chipDolci)
    private val chipContorni = dialogView.findViewById<Chip>(R.id.chipContorni)

    fun show(){
        Log.d("Dialog", "showing dialog")
        Log.d("Dialog", "$context")

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Filtra Ricette")
            .setView(dialogView)
            .setPositiveButton("Applica", null)
            .setNegativeButton("Resetta FIltri", null)
            .create()

        //Set chips status to the currently applied filters
        initializeChips()

        dialog.setOnShowListener{
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.setOnClickListener {
                getChipsStatus()
                onPositiveButtonClick(selectedChips)
                dialog.dismiss()
            }

            negativeButton.setOnClickListener {
                onNegativeButtonClick()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun getChipsStatus() {
        selectedChips["Primo"] = chipPrimi.isChecked
        selectedChips["Secondo"] = chipSecondi.isChecked
        selectedChips["Contorno"] = chipContorni.isChecked
        selectedChips["Dolce"] = chipDolci.isChecked
    }

    private fun initializeChips(){
        chipPrimi.isChecked = appliedFilters["Primo"]?: true
        chipSecondi.isChecked = appliedFilters["Secondo"]?: true
        chipDolci.isChecked = appliedFilters["Dolce"]?: true
        chipContorni.isChecked = appliedFilters["Contorno"]?: true
    }
}