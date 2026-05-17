package com.example.nusa_rasa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class KeranjangAdapter(
    private val items: MutableList<CartItem>,
    private val onQtyChanged: () -> Unit
) : RecyclerView.Adapter<KeranjangAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgMenuCart: ImageView = view.findViewById(R.id.imgMenuCart)
        val tvEmojiCart: TextView  = view.findViewById(R.id.tvEmojiCart)
        val tvNamaCart: TextView   = view.findViewById(R.id.tvNamaCart)
        val tvHargaCart: TextView  = view.findViewById(R.id.tvHargaCart)
        val tvQtyCart: TextView    = view.findViewById(R.id.tvQtyCart)
        val btnMinusCart: TextView = view.findViewById(R.id.btnMinusCart)
        val btnPlusCart: TextView  = view.findViewById(R.id.btnPlusCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keranjang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvNamaCart.text  = item.nama
        holder.tvHargaCart.text = "Rp ${String.format("%,d", item.harga).replace(",", ".")}"
        holder.tvQtyCart.text   = item.qty.toString()

        // Load gambar atau fallback emoji
        if (!item.imageUrl.isNullOrBlank()) {
            holder.tvEmojiCart.visibility  = View.GONE
            holder.imgMenuCart.visibility  = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imgMenuCart)
        } else {
            holder.imgMenuCart.visibility  = View.GONE
            holder.tvEmojiCart.visibility  = View.VISIBLE
            holder.tvEmojiCart.text        = item.emoji
        }

        holder.btnPlusCart.setOnClickListener {
            item.qty++
            notifyItemChanged(position)
            onQtyChanged()
        }

        holder.btnMinusCart.setOnClickListener {
            if (item.qty > 1) {
                item.qty--
                notifyItemChanged(position)
            } else {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
            onQtyChanged()
        }
    }

    override fun getItemCount(): Int = items.size
}
