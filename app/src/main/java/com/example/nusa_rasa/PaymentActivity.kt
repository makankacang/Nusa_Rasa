package com.example.nusa_rasa

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.adapter.PaymentAdapter
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.model.Payment
import com.example.nusa_rasa.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var toolbar: Toolbar
    private lateinit var tvPaymentCount: TextView
    private lateinit var tvTotalPaid: TextView
    private lateinit var tvTotalUnpaid: TextView
    private lateinit var tvTotalFailed: TextView
    private lateinit var layoutLoading: LinearLayout
    private lateinit var rvPayments: RecyclerView
    private lateinit var adapter: PaymentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        session = SessionManager(this)

        bindViews()
        setupToolbar()
        setupRecyclerView()
        loadPayments()
    }

    override fun onResume() {
        super.onResume()
        loadPayments()
    }

    private fun bindViews() {
        toolbar          = findViewById(R.id.toolbar)
        tvPaymentCount   = findViewById(R.id.tvPaymentCount)
        tvTotalPaid      = findViewById(R.id.tvTotalPaid)
        tvTotalUnpaid    = findViewById(R.id.tvTotalUnpaid)
        tvTotalFailed    = findViewById(R.id.tvTotalFailed)
        layoutLoading    = findViewById(R.id.layoutLoading)
        rvPayments       = findViewById(R.id.rvPayments)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = PaymentAdapter(mutableListOf())
        rvPayments.layoutManager = LinearLayoutManager(this)
        rvPayments.adapter = adapter
    }

    private fun loadPayments() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getPayments(session.getToken())
                if (response.isSuccessful) {
                    val payments = response.body() ?: emptyList()
                    adapter.updateData(payments)
                    updateSummaryCards(payments)
                    tvPaymentCount.text = "${payments.size} transaksi hari ini"
                } else {
                    showSnack("Gagal memuat data pembayaran")
                }
            } catch (e: Exception) {
                showSnack("Tidak dapat terhubung ke server")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun updateSummaryCards(payments: List<Payment>) {
        val paid   = payments.count { it.status.lowercase() == "paid" }
        val unpaid = payments.count { it.status.lowercase() == "unpaid" }
        val failed = payments.count { it.status.lowercase() == "failed" }

        tvTotalPaid.text   = paid.toString()
        tvTotalUnpaid.text = unpaid.toString()
        tvTotalFailed.text = failed.toString()
    }

    private fun setLoading(loading: Boolean) {
        layoutLoading.visibility = if (loading) View.VISIBLE else View.GONE
        rvPayments.visibility    = if (loading) View.GONE    else View.VISIBLE
    }

    private fun showSnack(msg: String) {
        Snackbar.make(rvPayments, msg, Snackbar.LENGTH_SHORT).show()
    }
}
