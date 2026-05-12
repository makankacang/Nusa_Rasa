package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class keranjang : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keranjang)

        val rvKeranjang = findViewById<RecyclerView>(R.id.rvKeranjang)

        val tvSubtotal = findViewById<TextView>(R.id.tvSubtotal)
        val tvPajak = findViewById<TextView>(R.id.tvPajak)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)

        val btnPesan = findViewById<TextView>(R.id.btnPesan)

        fun formatRupiah(angka: Int): String {
            return "Rp ${String.format("%,d", angka).replace(",", ".")}"
        }

        fun updateTotal() {

            tvSubtotal.text =
                formatRupiah(CartManager.subtotal())

            tvPajak.text =
                formatRupiah(CartManager.pajak())

            tvTotal.text =
                formatRupiah(CartManager.total())
        }

        rvKeranjang.layoutManager =
            LinearLayoutManager(this)

        rvKeranjang.adapter =
            KeranjangAdapter(CartManager.items) {

                updateTotal()

            }

        updateTotal()

        btnPesan.setOnClickListener {
            val intent = Intent(this, qris::class.java)
            intent.putExtra("totalPembayaran", CartManager.total())
            startActivity(intent)
        }

        val btnBack = findViewById<TextView>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }
}