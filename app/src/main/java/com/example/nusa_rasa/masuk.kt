package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class masuk : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masuk)

        val etNamaPembeli =
            findViewById<EditText>(R.id.etNamaPembeli)

        val etNomorMeja =
            findViewById<EditText>(R.id.etNomorMeja)

        val btnMulaiPesan =
            findViewById<CardView>(R.id.btnMulaiPesan)

        btnMulaiPesan.setOnClickListener {

            val nama =
                etNamaPembeli.text.toString().trim()

            val meja =
                etNomorMeja.text.toString().trim()

            if (nama.isEmpty()) {

                Toast.makeText(
                    this,
                    "Nama pembeli wajib diisi",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (meja.isEmpty()) {

                Toast.makeText(
                    this,
                    "Nomor meja wajib diisi",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val intent =
                Intent(this, home::class.java)

            intent.putExtra("namaPembeli", nama)
            intent.putExtra("nomorMeja", meja)

            startActivity(intent)
        }
    }
}