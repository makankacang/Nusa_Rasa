package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class qris : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qris)

        val tvTotalAmount = findViewById<TextView>(R.id.tvTotalAmount)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        val totalPembayaran = intent.getIntExtra("totalPembayaran", 0)

        tvTotalAmount.text =
            "Rp ${String.format("%,d", totalPembayaran).replace(",", ".")}"

        tvStatus.setOnClickListener {
            Toast.makeText(
                this,
                "Pembayaran anda berhasil",
                Toast.LENGTH_SHORT
            ).show()

            CartManager.items.clear()

            val intent = Intent(this, masuk::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}