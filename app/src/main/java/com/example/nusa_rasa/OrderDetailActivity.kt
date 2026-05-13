package com.example.nusa_rasa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.model.Order
import com.example.nusa_rasa.model.UpdateStatusRequest
import com.example.nusa_rasa.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    private lateinit var toolbar: Toolbar
    private lateinit var layoutProcessing: View
    private lateinit var tvOrderId: TextView
    private lateinit var tvStatusBadge: TextView
    private lateinit var tvBuyerName: TextView
    private lateinit var tvTableNumber: TextView
    private lateinit var tvOrderTime: TextView
    private lateinit var rvOrderItems: LinearLayout   // bukan RecyclerView, tapi LinearLayout
    private lateinit var layoutNote: View
    private lateinit var tvNote: TextView
    private lateinit var tvTotal: TextView
    private lateinit var layoutActions: View
    private lateinit var btnReject: MaterialButton
    private lateinit var btnApprove: MaterialButton
    private lateinit var btnMarkDone: MaterialButton

    private var orderId: Int = -1
    private val rupiahFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        session = SessionManager(this)
        orderId = intent.getIntExtra("order_id", -1)

        bindViews()
        setupToolbar()

        if (orderId != -1) loadOrderDetail()
    }

    private fun bindViews() {
        toolbar           = findViewById(R.id.toolbar)
        layoutProcessing  = findViewById(R.id.layoutProcessing)
        tvOrderId         = findViewById(R.id.tvOrderId)
        tvStatusBadge     = findViewById(R.id.tvStatusBadge)
        tvBuyerName       = findViewById(R.id.tvBuyerName)
        tvTableNumber     = findViewById(R.id.tvTableNumber)
        tvOrderTime       = findViewById(R.id.tvOrderTime)
        rvOrderItems      = findViewById(R.id.rvOrderItems)
        layoutNote        = findViewById(R.id.layoutNote)
        tvNote            = findViewById(R.id.tvNote)
        tvTotal           = findViewById(R.id.tvTotal)
        layoutActions     = findViewById(R.id.layoutActions)
        btnReject         = findViewById(R.id.btnReject)
        btnApprove        = findViewById(R.id.btnApprove)
        btnMarkDone       = findViewById(R.id.btnMarkDone)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadOrderDetail() {
        setProcessing(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getOrderDetail(
                    token   = session.getToken(),
                    orderId = orderId
                )
                if (response.isSuccessful && response.body() != null) {
                    populateUI(response.body()!!)
                } else {
                    showSnack("Gagal memuat detail pesanan")
                }
            } catch (e: Exception) {
                showSnack("Tidak dapat terhubung ke server")
            } finally {
                setProcessing(false)
            }
        }
    }

    private fun populateUI(order: Order) {
        tvOrderId.text    = order.orderCode.ifEmpty { "ORD-${order.id}" }
        tvBuyerName.text  = order.customerName
        tvTableNumber.text = "Meja ${order.tableNumber}"
        tvOrderTime.text  = order.createdAt.replace("T", " ").let {
            if (it.length >= 16) it.substring(0, 16) else it
        }
        tvTotal.text = rupiahFormat.format(order.total)

        // Note
        if (!order.note.isNullOrBlank()) {
            layoutNote.visibility = View.VISIBLE
            tvNote.text = order.note
        } else {
            layoutNote.visibility = View.GONE
        }

        // Status badge
        applyStatusBadge(order.status)

        // Item list (inflate dinamis ke LinearLayout)
        rvOrderItems.removeAllViews()
        order.items.forEach { item ->
            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_order_item_detail, rvOrderItems, false)
            row.findViewById<TextView>(R.id.tvItemName)?.text     = "${item.quantity}× ${item.menuName}"
            row.findViewById<TextView>(R.id.tvItemSubtotal)?.text = rupiahFormat.format(item.subtotal)
            rvOrderItems.addView(row)
        }

        // Tombol aksi berdasarkan status
        setupActionButtons(order)
    }

    private fun setupActionButtons(order: Order) {
        layoutActions.visibility = View.VISIBLE
        btnApprove.visibility = View.GONE
        btnReject.visibility  = View.GONE
        btnMarkDone.visibility = View.GONE

        when (order.status.lowercase()) {
            "pending" -> {
                btnApprove.visibility = View.VISIBLE
                btnReject.visibility  = View.VISIBLE
                btnApprove.setOnClickListener { updateStatus(order.id, "approved") }
                btnReject.setOnClickListener  { showRejectDialog(order.id) }
            }
            "paid" -> {
                btnMarkDone.visibility = View.VISIBLE
                btnMarkDone.setOnClickListener { updateStatus(order.id, "done") }
            }
            else -> layoutActions.visibility = View.GONE
        }
    }

    private fun updateStatus(orderId: Int, newStatus: String) {
        setProcessing(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.updateOrderStatus(
                    token   = session.getToken(),
                    orderId = orderId,
                    request = UpdateStatusRequest(newStatus)
                )
                if (response.isSuccessful && response.body() != null) {
                    populateUI(response.body()!!)
                    showSnack("Status diperbarui → $newStatus")
                } else {
                    showSnack("Gagal mengubah status")
                }
            } catch (e: Exception) {
                showSnack("Error: ${e.message}")
            } finally {
                setProcessing(false)
            }
        }
    }

    private fun showRejectDialog(orderId: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_reject_title))
            .setMessage(getString(R.string.dialog_reject_message))
            .setPositiveButton(getString(R.string.dialog_reject_confirm)) { _, _ ->
                updateStatus(orderId, "rejected")
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun applyStatusBadge(status: String) {
        val (bgRes, colorRes, label) = when (status.lowercase()) {
            "pending"  -> Triple(R.drawable.bg_badge_pending,  R.color.status_pending_text,  "Pending")
            "approved" -> Triple(R.drawable.bg_badge_approved, R.color.status_approved_text, "Approved")
            "paid"     -> Triple(R.drawable.bg_badge_paid,     R.color.status_paid_text,     "Paid")
            "done"     -> Triple(R.drawable.bg_badge_done,     R.color.status_done_text,     "Selesai")
            "rejected" -> Triple(R.drawable.bg_badge_rejected, R.color.status_rejected_text, "Ditolak")
            else       -> Triple(R.drawable.bg_badge_pending,  R.color.status_pending_text,  status)
        }
        tvStatusBadge.setBackgroundResource(bgRes)
        tvStatusBadge.setTextColor(ContextCompat.getColor(this, colorRes))
        tvStatusBadge.text = label
    }

    private fun setProcessing(show: Boolean) {
        layoutProcessing.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showSnack(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show()
    }
}
