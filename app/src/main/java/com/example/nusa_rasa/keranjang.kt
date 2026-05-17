package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.model.CreateOrderItemRequest
import com.example.nusa_rasa.model.CreateOrderRequest
import kotlinx.coroutines.launch

class keranjang : AppCompatActivity() {

    private var namaPembeli: String = ""
    private var nomorMeja: String = ""
    private lateinit var btnPesan: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keranjang)

        namaPembeli = intent.getStringExtra("namaPembeli") ?: "Pembeli"
        nomorMeja = intent.getStringExtra("nomorMeja") ?: "-"

        val rvKeranjang = findViewById<RecyclerView>(R.id.rvKeranjang)
        val tvSubtotal = findViewById<TextView>(R.id.tvSubtotal)
        val tvPajak = findViewById<TextView>(R.id.tvPajak)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        btnPesan = findViewById(R.id.btnPesan)

        fun formatRupiah(angka: Int): String {
            return "Rp ${String.format("%,d", angka).replace(",", ".")}"
        }

        fun updateTotal() {
            tvSubtotal.text = formatRupiah(CartManager.subtotal())
            tvPajak.text = formatRupiah(CartManager.pajak())
            tvTotal.text = formatRupiah(CartManager.total())
        }

        rvKeranjang.layoutManager = LinearLayoutManager(this)
        rvKeranjang.adapter = KeranjangAdapter(CartManager.items) {
            updateTotal()
        }

        updateTotal()

        btnPesan.setOnClickListener {
            createOrder()
        }

        val btnBack = findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun createOrder() {
        if (CartManager.items.isEmpty()) {
            Toast.makeText(this, "Keranjang masih kosong", Toast.LENGTH_SHORT).show()
            return
        }

        btnPesan.isEnabled = false
        btnPesan.text = "Mengirim pesanan..."

        val request = CreateOrderRequest(
            customerName = namaPembeli,
            tableNumber = nomorMeja,
            notes = "Pajak 5% sudah termasuk di total aplikasi pembeli",
            items = CartManager.items.map { item ->
                CreateOrderItemRequest(
                    menuId = item.menuId,
                    quantity = item.qty,
                    subtotal = (item.harga * item.qty).toLong()
                )
            }
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.createPublicOrder(request)
                if (response.isSuccessful && response.body() != null) {
                    val order = response.body()!!
                    val intent = Intent(this@keranjang, qris::class.java)
                    intent.putExtra("orderId", order.id)
                    intent.putExtra("totalPembayaran", order.total.toInt())
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@keranjang, "Pesanan gagal dikirim", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@keranjang, "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show()
            } finally {
                btnPesan.isEnabled = true
                btnPesan.text = "Pesan Sekarang"
            }
        }
    }
}
