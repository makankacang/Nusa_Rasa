package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nusa_rasa.api.RetrofitClient
import kotlinx.coroutines.launch

class qris : AppCompatActivity() {

    private lateinit var tvTotalAmount: TextView
    private lateinit var tvStatus: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var orderId: Int = -1
    private var lastStatus: String = "pending"

    private val pollRunnable = object : Runnable {
        override fun run() {
            loadOrderStatus()
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qris)

        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvStatus = findViewById(R.id.tvStatus)

        orderId = intent.getIntExtra("orderId", -1)
        val totalPembayaran = intent.getIntExtra("totalPembayaran", 0)

        tvTotalAmount.text = "Rp ${String.format("%,d", totalPembayaran).replace(",", ".")}"
        tvStatus.text = "Menunggu admin approve pesanan..."

        tvStatus.setOnClickListener {
            if (lastStatus.lowercase() == "approved") {
                confirmPayment()
            } else if (lastStatus.lowercase() == "pending") {
                Toast.makeText(this, "Tunggu admin approve dulu", Toast.LENGTH_SHORT).show()
            }
        }

        loadOrderStatus()
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(pollRunnable, 3000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(pollRunnable)
    }

    private fun loadOrderStatus() {
        if (orderId == -1) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getPublicOrderDetail(orderId)
                if (response.isSuccessful && response.body() != null) {
                    val order = response.body()!!
                    lastStatus = order.status
                    tvTotalAmount.text = "Rp ${String.format("%,d", order.total).replace(",", ".")}"
                    updateStatusText(order.status)
                }
            } catch (_: Exception) {
                tvStatus.text = "Tidak dapat terhubung ke server"
            }
        }
    }

    private fun updateStatusText(status: String) {
        tvStatus.text = when (status.lowercase()) {
            "pending" -> "Menunggu admin approve pesanan..."
            "approved" -> "Pesanan disetujui. Klik di sini setelah bayar QRIS"
            "paid" -> "Pembayaran berhasil. Pesanan diproses"
            "done" -> "Pesanan selesai"
            "rejected" -> "Pesanan ditolak admin"
            else -> "Status: $status"
        }
    }

    private fun confirmPayment() {
        tvStatus.isEnabled = false
        tvStatus.text = "Mengonfirmasi pembayaran..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.confirmPublicPayment(orderId)
                if (response.isSuccessful && response.body() != null) {
                    CartManager.items.clear()
                    Toast.makeText(this@qris, "Pembayaran berhasil", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@qris, masuk::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@qris, "Pembayaran gagal dikonfirmasi", Toast.LENGTH_SHORT).show()
                    loadOrderStatus()
                }
            } catch (e: Exception) {
                Toast.makeText(this@qris, "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show()
                loadOrderStatus()
            } finally {
                tvStatus.isEnabled = true
            }
        }
    }
}
