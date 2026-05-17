package com.example.nusa_rasa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class MenuAdapter(
    private val listMenu: List<MenuItem>,
    private val onTambahClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMenuBuyer: ImageView = itemView.findViewById(R.id.imgMenuBuyer)
        val tvEmoji: TextView       = itemView.findViewById(R.id.tvEmoji)
        val tvNama: TextView        = itemView.findViewById(R.id.tvNama)
        val tvHarga: TextView       = itemView.findViewById(R.id.tvHarga)
        val btnTambah: CardView     = itemView.findViewById(R.id.btnTambah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = listMenu[position]

        holder.tvNama.text  = menu.nama
        holder.tvHarga.text = menu.harga

        // Load gambar dari server kalau ada, fallback ke emoji
        val imageUrl = menu.imageUrl
        if (!imageUrl.isNullOrBlank()) {
            holder.tvEmoji.visibility      = View.GONE
            holder.imgMenuBuyer.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imgMenuBuyer)
        } else {
            // Tidak ada gambar → tampilkan emoji sesuai kategori
            holder.imgMenuBuyer.visibility = View.GONE
            holder.tvEmoji.visibility      = View.VISIBLE
            holder.tvEmoji.text            = menu.emoji
        }

        holder.btnTambah.setOnClickListener {
            onTambahClick(menu)
        }
    }

    override fun getItemCount(): Int = listMenu.size
}
