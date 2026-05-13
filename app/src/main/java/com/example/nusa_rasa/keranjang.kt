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
            if (CartManager.items.isEmpty()) {
                Toast.makeText(this, "Keranjang masih kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnPesan.isEnabled = false
            kirimPesanan(btnPesan)
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun kirimPesanan(btnPesan: TextView) {
        val orderItems = CartManager.items.map { item ->
            CreateOrderItemRequest(
                menuId = item.menuId,
                quantity = item.qty,
                subtotal = item.harga * item.qty
            )
        }

        val request = CreateOrderRequest(
            customerName = OrderManager.namaPembeli,
            tableNumber = OrderManager.nomorMeja,
            notes = null,
            items = orderItems
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.createOrder(request)
                if (response.isSuccessful) {
                    val order = response.body()
                    if (order != null) {
                        OrderManager.orderId = order.id
                        OrderManager.buatPesanan(
                            nama = OrderManager.namaPembeli,
                            meja = OrderManager.nomorMeja,
                            items = CartManager.items,
                            total = CartManager.total()
                        )
                        startActivity(Intent(this@keranjang, qris::class.java))
                    } else {
                        Toast.makeText(this@keranjang, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
                        btnPesan.isEnabled = true
                    }
                } else {
                    Toast.makeText(this@keranjang, "Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                    btnPesan.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@keranjang, "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show()
                btnPesan.isEnabled = true
            }
        }
    }
}
