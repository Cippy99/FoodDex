package com.example.fooddex

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class IngredientAmountDialog(private val context: Context, private val product: Product, private val listener: IngredientAmountDialogListener,
                             ) {

    private val dialogView = LayoutInflater.from(context).inflate(R.layout.ingredient_amount_dialog, null)
    private val tietAmount: TextInputEditText = dialogView.findViewById(R.id.tietAmount)

    private val tilAmount: TextInputLayout = dialogView.findViewById(R.id.tilAmount)

    fun show() {
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Quanti ${product.unitOfMeasure}?")
            .setView(dialogView)
            .setPositiveButton("Aggiungi", null)
            .setNegativeButton("Cancella") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val amountText = tietAmount.text.toString()
                val amount = if (amountText.isNotEmpty()) amountText.toDouble() else 0.0

                if (validate()) {
                    listener.onAmountSelected(product, amount)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun validate(): Boolean{
        var ok = true

        val amount = tietAmount.text.toString()

        if (amount.isNullOrEmpty()){
            ok = false
            tilAmount.error = "Inserire una quantità"
        }
        else if (amount.toDouble() <= 0){
            tilAmount.error = "La quantità deve essere maggiore di 0"
        }


        return ok
    }

}