package com.example.nusa_rasa

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nusa_rasa.api.RetrofitClient
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class qris : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private var isPolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qris)

        val imgQrCode = findViewById<ImageView>(R.id.imgQrCode)
        val tvTotalAmount = findViewById<TextView>(R.id.tvTotalAmount)
        tvStatus = findViewById(R.id.tvStatus)
        val tvAdminPanelLink = findViewById<TextView>(R.id.tvAdminPanelLink)

        val total = OrderManager.totalPembayaran
        tvTotalAmount.text = "Rp ${String.format("%,d", total).replace(",", ".")}"

        val qrContent = "NUSARASA|MEJA:${OrderManager.nomorMeja}|NAMA:${OrderManager.namaPembeli}|TOTAL:$total"
        imgQrCode.setImageBitmap(generateQr(qrContent, 400))

        tvAdminPanelLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        isPolling = true
        mulaiBolling()
    }

    override fun onPause() {
        super.onPause()
        isPolling = false
    }

    private fun mulaiBolling() {
        lifecycleScope.launch {
            while (isPolling) {
                val orderId = OrderManager.orderId
                if (orderId > 0) {
                    try {
                        val response = RetrofitClient.instance.getOrder(orderId)
                        if (response.isSuccessful) {
                            when (response.body()?.status?.uppercase()) {
                                "APPROVED", "PAID", "DONE" -> {
                                    isPolling = false
                                    selesaikanPesanan(diterima = true)
                                    return@launch
                                }
                                "REJECTED" -> {
                                    isPolling = false
                                    selesaikanPesanan(diterima = false)
                                    return@launch
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // lanjut polling saat jaringan error
                    }
                } else {
                    when (OrderManager.status) {
                        OrderManager.Status.DITERIMA -> {
                            isPolling = false
                            selesaikanPesanan(diterima = true)
                            return@launch
                        }
                        OrderManager.Status.DITOLAK -> {
                            isPolling = false
                            selesaikanPesanan(diterima = false)
                            return@launch
                        }
                        OrderManager.Status.MENUNGGU -> {}
                    }
                }
                delay(2000)
            }
        }
    }

    private fun selesaikanPesanan(diterima: Boolean) {
        val pesan = if (diterima)
            "Pesanan diterima! Silakan lakukan pembayaran."
        else
            "Pesanan ditolak oleh admin."
        Toast.makeText(this, pesan, Toast.LENGTH_LONG).show()
        CartManager.items.clear()
        OrderManager.reset()
        val intent = Intent(this, masuk::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun generateQr(content: String, size: Int): Bitmap {
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
