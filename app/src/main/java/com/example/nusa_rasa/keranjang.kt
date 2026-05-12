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
        val btnBack = findViewById<TextView>(R.id.btnBack)

        fun formatRupiah(angka: Int) =
            "Rp ${String.format("%,d", angka).replace(",", ".")}"

        fun updateTotal() {
            tvSubtotal.text = formatRupiah(CartManager.subtotal())
            tvPajak.text = formatRupiah(CartManager.pajak())
            tvTotal.text = formatRupiah(CartManager.total())
        }

        rvKeranjang.layoutManager = LinearLayoutManager(this)
        rvKeranjang.adapter = KeranjangAdapter(CartManager.items) { updateTotal() }

        updateTotal()

        btnPesan.setOnClickListener {
            OrderManager.buatPesanan(
                nama = OrderManager.namaPembeli,
                meja = OrderManager.nomorMeja,
                items = CartManager.items,
                total = CartManager.total()
            )
            startActivity(Intent(this, qris::class.java))
        }

        btnBack.setOnClickListener { finish() }
    }
}
