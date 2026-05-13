package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.model.MenuItem as ApiMenuItem
import kotlinx.coroutines.launch

class home : AppCompatActivity() {

    private var allMenus: List<MenuItem> = emptyList()
    private var kategoriAktif = "Semua"
    private var querySearch = ""
    private lateinit var adapter: MenuAdapter
    private lateinit var chips: Map<String, TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvHalo = findViewById<TextView>(R.id.tvHalo)
        val namaPembeli = intent.getStringExtra("namaPembeli") ?: ""
        tvHalo.text = if (namaPembeli.isNotEmpty()) "Halo, $namaPembeli 👋" else "Halo 👋"

        val rvMenu = findViewById<RecyclerView>(R.id.rvMenu)
        val cartBar = findViewById<View>(R.id.cartBar)
        val tvJumlahItem = findViewById<TextView>(R.id.tvJumlahItem)
        val tvInfoPesanan = findViewById<TextView>(R.id.tvInfoPesanan)
        val tvTotalHarga = findViewById<TextView>(R.id.tvTotalHarga)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        chips = mapOf(
            "Semua" to findViewById(R.id.chipSemua),
            "Nasi" to findViewById(R.id.chipNasi),
            "Ayam" to findViewById(R.id.chipAyam),
            "Minuman" to findViewById(R.id.chipMinuman),
            "Snack" to findViewById(R.id.chipSnack)
        )

        adapter = MenuAdapter(emptyList()) { menu ->
            CartManager.tambahItem(menu)
            cartBar.visibility = View.VISIBLE
            tvJumlahItem.text = "${CartManager.totalItem()} item"
            tvInfoPesanan.text = "Subtotal pesanan"
            tvTotalHarga.text = "Rp ${String.format("%,d", CartManager.subtotal()).replace(",", ".")}"
        }

        rvMenu.layoutManager = GridLayoutManager(this, 2)
        rvMenu.adapter = adapter

        chips.forEach { (label, chip) ->
            chip.setOnClickListener { setKategori(label) }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                querySearch = s?.toString() ?: ""
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cartBar.setOnClickListener {
            startActivity(Intent(this, keranjang::class.java))
        }

        updateCartBar(cartBar, tvJumlahItem, tvInfoPesanan, tvTotalHarga)
        loadMenus()
    }

    private fun loadMenus() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMenus()
                if (response.isSuccessful) {
                    val apiMenus: List<ApiMenuItem> = response.body() ?: emptyList()
                    allMenus = apiMenus
                        .filter { it.isAvailable }
                        .map { apiItem ->
                            MenuItem(
                                menuId = apiItem.id,
                                emoji = emojiUntukMenu(apiItem.name, apiItem.category),
                                nama = apiItem.name,
                                harga = "Rp ${String.format("%,d", apiItem.price).replace(",", ".")}",
                                kategori = apiItem.category ?: "Lainnya"
                            )
                        }
                    applyFilter()
                } else {
                    Toast.makeText(this@home, "Gagal memuat menu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@home, "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun emojiUntukMenu(name: String, category: String?): String {
        val n = name.lowercase()
        return when {
            "sate" in n     -> "🍢"
            "mie" in n      -> "🍜"
            "kangkung" in n -> "🥗"
            "kentang" in n  -> "🍟"
            "kopi" in n     -> "☕"
            "teh" in n      -> "🧋"
            "nasi" in n && "ayam" in n -> "🍛"
            "nasi" in n     -> "🍚"
            "ayam" in n     -> "🍗"
            "es" in n       -> "🥤"
            else            -> "🍽️"
        }
    }

    private fun setKategori(label: String) {
        kategoriAktif = label
        chips.forEach { (chipLabel, chip) ->
            if (chipLabel == label) {
                chip.setBackgroundResource(R.drawable.bg_category_active)
                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                chip.setBackgroundResource(R.drawable.bg_category_inactive)
                chip.setTextColor(ContextCompat.getColor(this, R.color.primary_red))
            }
        }
        applyFilter()
    }

    private fun applyFilter() {
        var hasil = allMenus
        if (kategoriAktif != "Semua") hasil = hasil.filter {
            it.kategori.equals(kategoriAktif, ignoreCase = true)
        }
        if (querySearch.isNotEmpty()) hasil = hasil.filter {
            it.nama.contains(querySearch, ignoreCase = true)
        }
        adapter.updateList(hasil)
    }

    private fun updateCartBar(
        cartBar: View,
        tvJumlahItem: TextView,
        tvInfoPesanan: TextView,
        tvTotalHarga: TextView
    ) {
        if (CartManager.totalItem() > 0) {
            cartBar.visibility = View.VISIBLE
            tvJumlahItem.text = "${CartManager.totalItem()} item"
            tvInfoPesanan.text = "Subtotal pesanan"
            tvTotalHarga.text = "Rp ${String.format("%,d", CartManager.subtotal()).replace(",", ".")}"
        }
    }
}
