package com.example.nusa_rasa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.R
import com.example.nusa_rasa.model.Payment

class PaymentAdapter(
    private var payments: MutableList<Payment>
) : RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPaymentId: TextView      = itemView.findViewById(R.id.tvPaymentId)
        val tvPaymentStatus: TextView  = itemView.findViewById(R.id.tvPaymentStatus)
        val tvPaymentBuyer: TextView   = itemView.findViewById(R.id.tvPaymentBuyer)
        val tvPaymentOrderRef: TextView = itemView.findViewById(R.id.tvPaymentOrderRef)
        val tvPaymentAmount: TextView  = itemView.findViewById(R.id.tvPaymentAmount)
        val tvPaymentTime: TextView    = itemView.findViewById(R.id.tvPaymentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p   = payments[position]
        val ctx = holder.itemView.context

        holder.tvPaymentId.text       = "#${p.id}"
        holder.tvPaymentBuyer.text    = "Order #${p.orderId}"
        holder.tvPaymentOrderRef.text = p.qrisCode ?: "-"
        holder.tvPaymentAmount.text   = p.paymentMethod ?: "-"
        holder.tvPaymentTime.text     = p.paidAt?.let {
            if (it.length >= 16) it.substring(0, 16).replace("T", " ") else it
        } ?: "-"

        when (p.paymentStatus.lowercase()) {
            "paid" -> {
                holder.tvPaymentStatus.text = "Paid"
                holder.tvPaymentStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_paid_text))
                holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_badge_paid)
            }
            "failed" -> {
                holder.tvPaymentStatus.text = "Gagal"
                holder.tvPaymentStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_rejected_text))
                holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_badge_rejected)
            }
            else -> {
                holder.tvPaymentStatus.text = "Belum Bayar"
                holder.tvPaymentStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_pending_text))
                holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_badge_pending)
            }
        }
    }

    override fun getItemCount() = payments.size

    fun updateData(newPayments: List<Payment>) {
        payments.clear()
        payments.addAll(newPayments)
        notifyDataSetChanged()
    }
}
