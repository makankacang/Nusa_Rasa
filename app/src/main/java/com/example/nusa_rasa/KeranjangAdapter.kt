package com.example.nusa_rasa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class KeranjangAdapter(
    private val items: MutableList<CartItem>,
    private val onQtyChanged: () -> Unit
) : RecyclerView.Adapter<KeranjangAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvEmoji = view.findViewById<TextView>(R.id.tvEmojiCart)
        val tvNama = view.findViewById<TextView>(R.id.tvNamaCart)
        val tvHarga = view.findViewById<TextView>(R.id.tvHargaCart)
        val tvQty = view.findViewById<TextView>(R.id.tvQtyCart)
        val btnMinus = view.findViewById<TextView>(R.id.btnMinusCart)
        val btnPlus = view.findViewById<TextView>(R.id.btnPlusCart)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keranjang, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items[position]

        holder.tvEmoji.text = item.emoji
        holder.tvNama.text = item.nama
        holder.tvHarga.text =
            "Rp ${String.format("%,d", item.harga).replace(",", ".")}"

        holder.tvQty.text = item.qty.toString()

        holder.btnPlus.setOnClickListener {

            item.qty++
            notifyItemChanged(position)
            onQtyChanged()

        }

        holder.btnMinus.setOnClickListener {

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