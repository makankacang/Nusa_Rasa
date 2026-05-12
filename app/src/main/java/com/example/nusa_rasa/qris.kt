package com.example.nusa_rasa

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class qris : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvStatus: TextView

    private val pollRunnable = object : Runnable {
        override fun run() {
            when (OrderManager.status) {
                OrderManager.Status.DITERIMA -> {
                    Toast.makeText(
                        this@qris,
                        "Pesanan diterima! Silakan lakukan pembayaran.",
                        Toast.LENGTH_LONG
                    ).show()
                    CartManager.items.clear()
                    OrderManager.reset()
                    val intent = Intent(this@qris, masuk::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                OrderManager.Status.DITOLAK -> {
                    Toast.makeText(
                        this@qris,
                        "Pesanan ditolak oleh admin.",
                        Toast.LENGTH_LONG
                    ).show()
                    CartManager.items.clear()
                    OrderManager.reset()
                    val intent = Intent(this@qris, masuk::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                OrderManager.Status.MENUNGGU -> {
                    handler.postDelayed(this, 2000)
                }
            }
        }
    }

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
        handler.removeCallbacksAndMessages(null)
        handler.post(pollRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
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
