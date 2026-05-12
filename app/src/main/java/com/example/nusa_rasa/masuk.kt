package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class masuk : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masuk)

        val etNamaPembeli = findViewById<EditText>(R.id.etNamaPembeli)
        val etNomorMeja = findViewById<EditText>(R.id.etNomorMeja)
        val btnMulaiPesan = findViewById<CardView>(R.id.btnMulaiPesan)
        val tvAdminLink = findViewById<TextView>(R.id.tvAdminLink)

        btnMulaiPesan.setOnClickListener {
            val nama = etNamaPembeli.text.toString().trim()
            val mejaStr = etNomorMeja.text.toString().trim()

            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama pembeli wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mejaStr.isEmpty()) {
                Toast.makeText(this, "Nomor meja wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val meja = mejaStr.toIntOrNull()
            if (meja == null || meja < 1 || meja > 20) {
                Toast.makeText(this, "Nomor meja harus antara 1 sampai 20", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            OrderManager.namaPembeli = nama
            OrderManager.nomorMeja = meja.toString()

            val intent = Intent(this, home::class.java)
            intent.putExtra("namaPembeli", nama)
            intent.putExtra("nomorMeja", meja.toString())
            startActivity(intent)
        }

        tvAdminLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
