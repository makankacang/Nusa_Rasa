package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.api.RetrofitClient
import kotlinx.coroutines.launch
import com.example.nusa_rasa.model.MenuItem as ApiMenuItem

class home : AppCompatActivity() {

    private lateinit var cartBar: View
    private lateinit var tvJumlahItem: TextView
    private lateinit var tvInfoPesanan: TextView
    private lateinit var tvTotalHarga: TextView
    private lateinit var rvMenu: RecyclerView
    private var namaPembeli: String = ""
    private var nomorMeja: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvHalo = findViewById<TextView>(R.id.tvHalo)

        namaPembeli = intent.getStringExtra("namaPembeli") ?: ""
        nomorMeja = intent.getStringExtra("nomorMeja") ?: ""

        tvHalo.text = if (namaPembeli.isNotEmpty()) {
            "Halo, $namaPembeli 👋"
        } else {
            "Halo 👋"
        }

        rvMenu = findViewById(R.id.rvMenu)
        cartBar = findViewById(R.id.cartBar)
        tvJumlahItem = findViewById(R.id.tvJumlahItem)
        tvInfoPesanan = findViewById(R.id.tvInfoPesanan)
        tvTotalHarga = findViewById(R.id.tvTotalHarga)

        rvMenu.layoutManager = GridLayoutManager(this, 2)
        cartBar.setOnClickListener {
            val intent = Intent(this, keranjang::class.java)
            intent.putExtra("namaPembeli", namaPembeli)
            intent.putExtra("nomorMeja", nomorMeja)
            startActivity(intent)
        }

        loadMenuFromServer()
        updateCartBar()
    }

    override fun onResume() {
        super.onResume()
        updateCartBar()
    }

    private fun loadMenuFromServer() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getPublicMenu()
                if (response.isSuccessful) {
                    val dataMenu = response.body().orEmpty().map { item ->
                        MenuItem(
                            id = item.id,
                            emoji = emojiForCategory(item.kategori),
                            nama = item.name,
                            harga = formatRupiah(item.price.toInt()),
                            imageUrl = item.imageUrl
                        )
                    }

                    rvMenu.adapter = MenuAdapter(dataMenu) { menu ->
                        CartManager.tambahItem(menu)
                        updateCartBar()
                    }
                } else {
                    Toast.makeText(this@home, "Gagal memuat menu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@home, "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCartBar() {
        if (CartManager.totalItem() > 0) {
            cartBar.visibility = View.VISIBLE
            tvJumlahItem.text = "${CartManager.totalItem()} item"
            tvInfoPesanan.text = "Subtotal pesanan"
            tvTotalHarga.text = formatRupiah(CartManager.subtotal())
        } else {
            cartBar.visibility = View.GONE
        }
    }

    private fun formatRupiah(angka: Int): String {
        return "Rp ${String.format("%,d", angka).replace(",", ".")}"
    }

    private fun emojiForCategory(kategori: String?): String {
        return when (kategori?.lowercase()) {
            "minuman" -> "🥤"
            "sayuran" -> "🥗"
            "snack" -> "🍟"
            else -> "🍽️"
        }
    }
}
