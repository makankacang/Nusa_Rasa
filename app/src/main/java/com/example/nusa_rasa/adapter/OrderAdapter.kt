package com.example.nusa_rasa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.R
import com.example.nusa_rasa.model.Order
import java.text.NumberFormat
import java.util.Locale

class OrderAdapter(
    private var orders: MutableList<Order>,
    private val onApprove:  ((Order) -> Unit)? = null,
    private val onReject:   ((Order) -> Unit)? = null,
    private val onMarkDone: ((Order) -> Unit)? = null,
    private val onDetail:   ((Order) -> Unit)? = null,
    private val compact: Boolean = false
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private val rupiahFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardOrder:           View    = itemView.findViewById(R.id.cardOrder)
        val tvOrderId:           TextView = itemView.findViewById(R.id.tvOrderId)
        val tvTableNumber:       TextView = itemView.findViewById(R.id.tvTableNumber)
        val tvOrderTime:         TextView = itemView.findViewById(R.id.tvOrderTime)
        val tvBuyerName:         TextView = itemView.findViewById(R.id.tvBuyerName)
        val tvStatusBadge:       TextView = itemView.findViewById(R.id.tvStatusBadge)
        val tvOrderItems:        TextView = itemView.findViewById(R.id.tvOrderItems)
        val tvOrderNote:         TextView = itemView.findViewById(R.id.tvOrderNote)
        val tvOrderTotal:        TextView = itemView.findViewById(R.id.tvOrderTotal)
        val tvTapHint:           TextView = itemView.findViewById(R.id.tvTapHint)
        val layoutPendingActions: View   = itemView.findViewById(R.id.layoutPendingActions)
        val btnApprove:          View    = itemView.findViewById(R.id.btnApprove)
        val btnReject:           View    = itemView.findViewById(R.id.btnReject)
        val tvWaitingQris:       View    = itemView.findViewById(R.id.tvWaitingQris)
        val btnMarkDone:         View    = itemView.findViewById(R.id.btnMarkDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val ctx   = holder.itemView.context

        // Data utama
        holder.tvOrderId.text    = order.orderCode.ifEmpty { "#${order.id}" }
        holder.tvTableNumber.text = "Meja ${order.tableNumber}"
        holder.tvBuyerName.text  = order.customerName
        holder.tvOrderTotal.text = rupiahFormat.format(order.total)
        holder.tvOrderTime.text  = order.createdAt.let {
            if (it.length >= 16) it.substring(11, 16) else it
        }
        holder.tvOrderItems.text = order.items.joinToString(" • ") {
            "${it.quantity}× ${it.menuName}"
        }

        // Catatan
        val hasNote = !order.note.isNullOrBlank()
        holder.tvOrderNote.visibility = if (hasNote) View.VISIBLE else View.GONE
        if (hasNote) holder.tvOrderNote.text = "📝 ${order.note}"

        // Status badge
        applyStatusBadge(holder.tvStatusBadge, order.status, ctx)

        // Klik seluruh card → detail
        holder.cardOrder.setOnClickListener { onDetail?.invoke(order) }

        // Sembunyikan semua action dulu
        holder.layoutPendingActions.visibility = View.GONE
        holder.tvWaitingQris.visibility        = View.GONE
        holder.btnMarkDone.visibility          = View.GONE
        holder.tvTapHint.visibility            = View.VISIBLE

        if (compact) {
            // Mode dashboard: tidak ada action buttons, cukup hint ketuk
            return
        }

        when (order.status.lowercase()) {
            "pending" -> {
                holder.layoutPendingActions.visibility = View.VISIBLE
                holder.tvTapHint.visibility            = View.GONE
                holder.btnApprove.setOnClickListener {
                    it.isEnabled = false
                    onApprove?.invoke(order)
                }
                holder.btnReject.setOnClickListener {
                    it.isEnabled = false
                    onReject?.invoke(order)
                }
            }
            "approved" -> {
                holder.tvWaitingQris.visibility = View.VISIBLE
                holder.tvTapHint.visibility     = View.GONE
            }
            "paid" -> {
                holder.btnMarkDone.visibility = View.VISIBLE
                holder.tvTapHint.visibility   = View.GONE
                holder.btnMarkDone.setOnClickListener {
                    it.isEnabled = false
                    onMarkDone?.invoke(order)
                }
            }
        }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    private fun applyStatusBadge(tv: TextView, status: String, ctx: android.content.Context) {
        val (bgRes, textColor, label) = when (status.lowercase()) {
            "pending"  -> Triple(R.drawable.bg_badge_pending,  R.color.status_pending_text,  "Pending")
            "approved" -> Triple(R.drawable.bg_badge_approved, R.color.status_approved_text, "Approved")
            "paid"     -> Triple(R.drawable.bg_badge_paid,     R.color.status_paid_text,     "Paid")
            "done"     -> Triple(R.drawable.bg_badge_done,     R.color.status_done_text,     "Selesai")
            "rejected" -> Triple(R.drawable.bg_badge_rejected, R.color.status_rejected_text, "Ditolak")
            else       -> Triple(R.drawable.bg_badge_pending,  R.color.status_pending_text,  status)
        }
        tv.setBackgroundResource(bgRes)
        tv.setTextColor(ContextCompat.getColor(ctx, textColor))
        tv.text = label
    }
}