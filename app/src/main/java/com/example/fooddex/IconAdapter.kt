import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddex.Icon
import com.example.fooddex.R

class IconAdapter(private val iconList: List<Icon>) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    var selectedIndex = 0

    inner class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImage: ImageView = itemView.findViewById(R.id.ivIcon)
        private val container: ConstraintLayout = itemView.findViewById(R.id.container)

        fun bind(icon: Icon, position: Int) {
            iconImage.setImageResource(icon.iconId)

            //Set the bg
            val isSelected = selectedIndex == position
            changeBgSelected(isSelected)

            itemView.setOnClickListener {
                selectedIndex = adapterPosition
                notifyDataSetChanged()
            }
        }

        private fun changeBgSelected(selected: Boolean) {
            val backgroundResId = if (selected) R.drawable.bg_icon_selected else 0
            container.setBackgroundResource(backgroundResId)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.icon_item, parent, false)
        return IconViewHolder(view)
    }

    override fun getItemCount(): Int {
        return iconList.size
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val icon = iconList[position]
        holder.bind(icon, position)
    }

    fun getSelectedIcon(): Icon {
        return iconList[selectedIndex]
    }
}
