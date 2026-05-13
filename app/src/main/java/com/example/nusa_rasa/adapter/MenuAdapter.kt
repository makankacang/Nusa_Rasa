package com.example.nusa_rasa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nusa_rasa.R
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.model.MenuItem
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.NumberFormat
import java.util.Locale

class MenuAdapter(
    private var items: MutableList<MenuItem>,
    private val onEdit: (MenuItem) -> Unit,
    private val onDelete: (MenuItem) -> Unit,
    private val onToggleAvailable: (MenuItem, Boolean) -> Unit
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    private val rupiahFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMenu: ImageView          = itemView.findViewById(R.id.imgMenu)
        val tvHabisOverlay: TextView    = itemView.findViewById(R.id.tvHabisOverlay)
        val tvMenuName: TextView        = itemView.findViewById(R.id.tvMenuName)
        val tvMenuPrice: TextView       = itemView.findViewById(R.id.tvMenuPrice)
        val tvMenuKategori: TextView    = itemView.findViewById(R.id.tvMenuKategori)
        val switchAvailable: SwitchMaterial = itemView.findViewById(R.id.switchAvailable)
        val tvAvailableLabel: TextView  = itemView.findViewById(R.id.tvAvailableLabel)
        val btnEdit: View               = itemView.findViewById(R.id.btnEdit)
        val btnDelete: View             = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvMenuName.text    = item.name
        holder.tvMenuPrice.text   = rupiahFormat.format(item.price)
        holder.tvMenuKategori.text = item.category?.replaceFirstChar { it.uppercase() } ?: ""

        val imageUrl = item.image?.let {
            if (it.startsWith("http")) it
            else "${RetrofitClient.BASE_URL.trimEnd('/')}$it"
        }
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_food_placeholder)
            .error(R.drawable.ic_food_placeholder)
            .centerCrop()
            .into(holder.imgMenu)

        holder.tvHabisOverlay.visibility =
            if (item.isAvailable) View.GONE else View.VISIBLE

        holder.switchAvailable.setOnCheckedChangeListener(null)
        holder.switchAvailable.isChecked = item.isAvailable
        holder.tvAvailableLabel.text =
            if (item.isAvailable) "Tersedia" else "Habis"
        holder.switchAvailable.setOnCheckedChangeListener { _, isChecked ->
            onToggleAvailable(item, isChecked)
        }

        holder.btnEdit.setOnClickListener   { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<MenuItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateItem(updated: MenuItem) {
        val idx = items.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            items[idx] = updated
            notifyItemChanged(idx)
        }
    }

    fun removeItem(menuId: Int) {
        val idx = items.indexOfFirst { it.id == menuId }
        if (idx >= 0) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}
