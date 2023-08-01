package com.example.fooddex

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ExpirationDialog(private val context: Context, private val onPositiveButtonClick: (ExpirationDate) -> Unit) {

    private val dialogView = LayoutInflater.from(context).inflate(R.layout.add_expiration_dialog, null)
    private val tietExpirationDate: TextInputEditText = dialogView.findViewById(R.id.tietExpirationDate)
    private val tietAmount: TextInputEditText = dialogView.findViewById(R.id.tietAmount)

    private val tilExpirationDate: TextInputLayout = dialogView.findViewById(R.id.tilExpirationDate)
    private val tilAmount: TextInputLayout = dialogView.findViewById(R.id.tilAmount)

    private lateinit var selectedDate: LocalDate

    init {

        //create Date Picker
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleziona Data")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDateInMillis = selection ?: return@addOnPositiveButtonClickListener
            val c = Calendar.getInstance()
            c.timeInMillis = selectedDateInMillis

            selectedDate = LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(
                Calendar.DAY_OF_MONTH))
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy")
            tietExpirationDate.setText(selectedDate.format(formatter))
        }

        //Open Date picker when Edit Text is pressed
        tietExpirationDate.setOnClickListener {
            datePicker.show((context as AppCompatActivity).supportFragmentManager, "tag")
        }


    }

    fun show() {
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Aggiungi Scadenza")
            .setView(dialogView)
            .setPositiveButton("Conferma", null)
            .setNegativeButton("Cancella") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val amountText = tietAmount.text.toString()
                val amount = if (amountText.isNotEmpty()) amountText.toInt() else 0

                if (validate()) {
                    onPositiveButtonClick(ExpirationDate(selectedDate, amount))
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun validate(): Boolean{
        var ok = true
        val data = tietExpirationDate.text.toString()
        val amount = tietAmount.text.toString()

        if (data.isNullOrEmpty()){
            ok = false
            tilExpirationDate.error = "Inserire una Data"
        }

        if (amount.isNullOrEmpty()){
            ok = false
            tilAmount.error = "Inserire una quantità"
        }
        else if (amount.toInt() <= 0){
            tilAmount.error = "La quantità deve essere maggiore di 0"
        }


        return ok
    }

}