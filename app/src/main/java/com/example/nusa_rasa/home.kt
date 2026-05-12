package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvHalo =
            findViewById<TextView>(R.id.tvHalo)

        val namaPembeli =
            intent.getStringExtra("namaPembeli") ?: ""

        if (namaPembeli.isNotEmpty()) {

            tvHalo.text =
                "Halo, $namaPembeli 👋"

        } else {

            tvHalo.text =
                "Halo 👋"
        }

        val rvMenu = findViewById<RecyclerView>(R.id.rvMenu)

        val cartBar = findViewById<View>(R.id.cartBar)
        val tvJumlahItem = findViewById<TextView>(R.id.tvJumlahItem)
        val tvInfoPesanan = findViewById<TextView>(R.id.tvInfoPesanan)
        val tvTotalHarga = findViewById<TextView>(R.id.tvTotalHarga)

        val dataMenu = listOf(

            MenuItem("🍗", "Ayam Taliwang", "Rp 25.000"),
            MenuItem("🍚", "Nasi Putih", "Rp 5.000"),
            MenuItem("🍗", "Ayam Bakar Madu", "Rp 28.000"),
            MenuItem("🥤", "Es Teh Manis", "Rp 6.000"),
            MenuItem("🍛", "Nasi Ayam Komplit", "Rp 32.000"),
            MenuItem("🍟", "Kentang Goreng", "Rp 12.000"),
            MenuItem("🍜", "Mie Goreng Spesial", "Rp 18.000"),
            MenuItem("🥗", "Plecing Kangkung", "Rp 10.000"),
            MenuItem("🍢", "Sate Ayam", "Rp 22.000"),
            MenuItem("🧋", "Es Kopi Susu", "Rp 15.000")

        )

        rvMenu.layoutManager = GridLayoutManager(this, 2)

        rvMenu.adapter = MenuAdapter(dataMenu) { menu ->

            CartManager.tambahItem(menu)

            cartBar.visibility = View.VISIBLE

            tvJumlahItem.text =
                "${CartManager.totalItem()} item"

            tvInfoPesanan.text =
                "Subtotal pesanan"

            tvTotalHarga.text =
                "Rp ${String.format("%,d", CartManager.subtotal()).replace(",", ".")}"
        }

        cartBar.setOnClickListener {

            val intent = Intent(this, keranjang::class.java)
            startActivity(intent)

        }
    }
}