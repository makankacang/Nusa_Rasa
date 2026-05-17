package com.example.nusa_rasa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.R
import com.example.nusa_rasa.model.OrderItem
import java.text.NumberFormat

class OrderItemDetailAdapter(
    private val items: List<OrderItem>,
    private val fmt: NumberFormat
) : RecyclerView.Adapter<OrderItemDetailAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvItemName: TextView = v.findViewById(R.id.tvItemName)
        val tvItemSubtotal: TextView = v.findViewById(R.id.tvItemSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_order_item_detail, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.tvItemName.text = "${item.quantity}× ${item.menu?.name ?: "Item"}"
        h.tvItemSubtotal.text = fmt.format(item.subtotal)
    }
}
