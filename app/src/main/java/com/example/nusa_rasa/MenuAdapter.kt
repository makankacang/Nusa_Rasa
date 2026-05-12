package com.example.nusa_rasa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class MenuAdapter(
    private var listMenu: List<MenuItem>,
    private val onTambahClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        val btnTambah: CardView = itemView.findViewById(R.id.btnTambah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = listMenu[position]
        holder.tvEmoji.text = menu.emoji
        holder.tvNama.text = menu.nama
        holder.tvHarga.text = menu.harga
        holder.btnTambah.setOnClickListener { onTambahClick(menu) }
    }

    override fun getItemCount(): Int = listMenu.size

    fun updateList(newList: List<MenuItem>) {
        listMenu = newList
        notifyDataSetChanged()
    }
}
