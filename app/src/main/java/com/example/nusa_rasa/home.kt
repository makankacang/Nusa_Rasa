package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class home : AppCompatActivity() {

    private val dataMenu = listOf(
        MenuItem("🍗", "Ayam Taliwang", "Rp 25.000", "Ayam"),
        MenuItem("🍚", "Nasi Putih", "Rp 5.000", "Nasi"),
        MenuItem("🍗", "Ayam Bakar Madu", "Rp 28.000", "Ayam"),
        MenuItem("🥤", "Es Teh Manis", "Rp 6.000", "Minuman"),
        MenuItem("🍛", "Nasi Ayam Komplit", "Rp 32.000", "Nasi"),
        MenuItem("🍟", "Kentang Goreng", "Rp 12.000", "Snack"),
        MenuItem("🍜", "Mie Goreng Spesial", "Rp 18.000", "Snack"),
        MenuItem("🥗", "Plecing Kangkung", "Rp 10.000", "Snack"),
        MenuItem("🍢", "Sate Ayam", "Rp 22.000", "Ayam"),
        MenuItem("🧋", "Es Kopi Susu", "Rp 15.000", "Minuman")
    )

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

        adapter = MenuAdapter(dataMenu) { menu ->
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
        var hasil = dataMenu
        if (kategoriAktif != "Semua") hasil = hasil.filter { it.kategori == kategoriAktif }
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
