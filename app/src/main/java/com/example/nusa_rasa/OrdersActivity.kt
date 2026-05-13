package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.adapter.OrderAdapter
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.model.UpdateStatusRequest
import com.example.nusa_rasa.utils.SessionManager
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class OrdersActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var toolbar: Toolbar
    private lateinit var tvOrderCount: TextView
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var layoutLoading: LinearLayout
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var rvOrders: RecyclerView

    private lateinit var adapter: OrderAdapter
    private var currentFilter: String? = null   // null = semua

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        session = SessionManager(this)

        bindViews()
        setupToolbar()
        setupFilterChips()
        setupRecyclerView()
        loadOrders()
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun bindViews() {
        toolbar          = findViewById(R.id.toolbar)
        tvOrderCount     = findViewById(R.id.tvOrderCount)
        chipGroupFilter  = findViewById(R.id.chipGroupFilter)
        layoutLoading    = findViewById(R.id.layoutLoading)
        layoutEmpty      = findViewById(R.id.layoutEmpty)
        rvOrders         = findViewById(R.id.rvOrders)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            currentFilter = when (checkedIds.firstOrNull()) {
                R.id.chipPending  -> "pending"
                R.id.chipApproved -> "approved"
                R.id.chipPaid     -> "paid"
                R.id.chipDone     -> "done"
                else              -> null
            }
            loadOrders()
        }
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(
            orders     = mutableListOf(),
            onApprove  = { order -> updateStatus(order.id, "approved") },
            onReject   = { order -> showRejectDialog(order.id) },
            onMarkDone = { order -> updateStatus(order.id, "done") },
            onDetail   = { order ->
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("order_id", order.id)
                startActivity(intent)
            }
        )
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = adapter
    }

    private fun loadOrders() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getOrders(
                    token  = session.getToken(),
                    status = currentFilter
                )
                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()
                    adapter.updateData(orders)
                    tvOrderCount.text = "${orders.size} pesanan hari ini"
                    setEmpty(orders.isEmpty())
                } else {
                    showSnack("Gagal memuat pesanan")
                }
            } catch (e: Exception) {
                showSnack("Tidak dapat terhubung ke server")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun updateStatus(orderId: Int, newStatus: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.updateOrderStatus(
                    token   = session.getToken(),
                    orderId = orderId,
                    request = UpdateStatusRequest(newStatus)
                )
                if (response.isSuccessful) {
                    showSnack("Status pesanan diperbarui → $newStatus")
                    loadOrders()
                } else {
                    showSnack("Gagal mengubah status")
                }
            } catch (e: Exception) {
                showSnack("Error: ${e.message}")
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

    private fun setLoading(loading: Boolean) {
        layoutLoading.visibility = if (loading) View.VISIBLE else View.GONE
        rvOrders.visibility      = if (loading) View.GONE    else View.VISIBLE
    }

    private fun setEmpty(empty: Boolean) {
        layoutEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        rvOrders.visibility    = if (empty) View.GONE    else View.VISIBLE
    }

    private fun showSnack(msg: String) {
        Snackbar.make(rvOrders, msg, Snackbar.LENGTH_SHORT).show()
    }
}
