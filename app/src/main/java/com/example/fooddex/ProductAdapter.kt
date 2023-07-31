package com.example.fooddex

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.abs


class ProductAdapter(val productList: MutableList<Product>, val context: Context): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvExpiration: TextView = itemView.findViewById(R.id.tvExpiration)
        private val tvPortions: TextView = itemView.findViewById(R.id.tvPortions)
        private val tvPortionsInUdM: TextView = itemView.findViewById(R.id.tvPortionsInUdM)
        private val btnPlus: Button = itemView.findViewById(R.id.btnPlus)
        private val btnMinus: Button = itemView.findViewById(R.id.btnMinus)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivProductIcon)

        private lateinit var selectedDate: LocalDate
        private lateinit var product: Product

        val auth = Firebase.auth
        val dbReference = Firebase.database.reference


        fun bind(product: Product){

            this.product = product

            tvName.text = product.name
            tvPortions.text = product.getTotalAmount().toString()
            tvPortionsInUdM.text = "${product.getTotalSize()} ${getUoMShort(product)}"
            ivIcon.setImageResource(product.iconId)

            val daysUntilExpiration = product.getDaysUntilExpiration()
            
            val textExpiration: String

            if (product.getNearestExpirationsInDays() <= 0){
                textExpiration = ""
            }
            else if (daysUntilExpiration == 0){
                textExpiration = "Oggi"
            }
            else if(daysUntilExpiration == 1){
                textExpiration = "Domani"
            }
            else if(daysUntilExpiration == -1){
                textExpiration = "Ieri"
            }
            else if(abs(daysUntilExpiration) <=31){
                textExpiration = "$daysUntilExpiration giorni"
            }
            else{
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
                val date = LocalDate.ofEpochDay(product.getNearestExpirationsInDays())
                textExpiration = date.format(formatter)
            }

            tvExpiration.text = textExpiration
            if(daysUntilExpiration <= 0){
                tvExpiration.setTextColor(MaterialColors.getColor(itemView, androidx.appcompat.R.attr.colorError))
            }
            else{
                tvExpiration.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnPrimarySurface))
            }

            updateMinusButton()

        }

        init {

            val dialogView = LayoutInflater.from(context).inflate(R.layout.add_expiration_dialog, null)
            val tietExpirationDate: TextInputEditText = dialogView.findViewById(R.id.tietExpirationDate)
            val tietAmount: TextInputEditText = dialogView.findViewById(R.id.tietAmount)
            val cardContainer: MaterialCardView = itemView.findViewById(R.id.cardContainer)

            cardContainer.setOnClickListener {
                editProduct(adapterPosition)
            }

            //Create DatePicker
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

            //Open Datepicker when click on EditText
            tietExpirationDate.setOnClickListener {
                datePicker.show((context as AppCompatActivity).supportFragmentManager, "tag")
            }

            //Create Dialog
            val dialog = MaterialAlertDialogBuilder(tvName.context)
                .setTitle("Aggiungi Scadenza")
                .setView(dialogView)
                .setPositiveButton("Conferma") { dialog, _ ->

                    val amountText = tietAmount.text.toString()
                    val amount = if (amountText.isNotEmpty()) amountText.toInt() else 0
                    addExpiration(ExpirationDate(selectedDate, amount))
                    dialog.dismiss()
                }
                .setNegativeButton("Cancella") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            btnPlus.setOnClickListener {
                dialog.show()
            }

            btnMinus.setOnClickListener {
                removeOneItem()
            }
        }

        private fun addExpiration(expiration: ExpirationDate){
            product.expirations.add(expiration)
            updateProduct()
        }

        private fun removeOneItem(){

            product.removeNearestExpirationItem()
            updateProduct()
        }

        private fun updateMinusButton(){
            btnMinus.isEnabled = product.getTotalAmount() > 0
        }

        private fun updateProduct(){
            val userRef = dbReference.child("users").child(auth.currentUser?.uid!!)

            userRef.child("familyId").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val familyId = snapshot.getValue(String::class.java)

                    if (familyId != null) {

                        val productRef = dbReference.child("products").child(familyId)

                        productRef.child(product.id).setValue(product)

                        updateMinusButton()
                        notifyItemChanged(adapterPosition)

                    } else {
                        // Handle the case when "familyId" doesn't exist in the database for the user
                        // You can show an error message or take appropriate action
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        private fun getUoMShort(product: Product): String {
            val udMArray = itemView.resources.getStringArray(R.array.UdM)
            val udMShortArray = itemView.resources.getStringArray(R.array.UdMShort)
            val index = udMArray.indexOf(product.unitOfMeasure)
            return if (index != -1 && index < udMShortArray.size) {
                udMShortArray[index]
            } else {
                ""
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_card_item, parent, false)

        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    fun editProduct(position: Int){
        val intent = Intent(context, EditProductActivity::class.java)
        intent.putExtra("productId", productList[position].id)
        context.startActivity(intent)
    }


}