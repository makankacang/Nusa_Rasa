package com.example.nusa_rasa

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class admin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val tvOrderDetail = findViewById<TextView>(R.id.tvOrderDetail)
        val btnSetujui = findViewById<Button>(R.id.btnSetujui)
        val btnTolak = findViewById<Button>(R.id.btnTolak)
        val layoutTombol = findViewById<View>(R.id.layoutTombol)
    }

    override fun onResume() {
        super.onResume()
        tampilkanPesanan()
    }

    private fun tampilkanPesanan() {
        val tvOrderDetail = findViewById<TextView>(R.id.tvOrderDetail)
        val btnSetujui = findViewById<Button>(R.id.btnSetujui)
        val btnTolak = findViewById<Button>(R.id.btnTolak)
        val layoutTombol = findViewById<View>(R.id.layoutTombol)

        if (OrderManager.daftarPesanan.isEmpty()) {
            tvOrderDetail.text = "Tidak ada pesanan masuk"
            layoutTombol.visibility = View.GONE
            return
        }

        val sb = StringBuilder()
        sb.appendLine("Meja ${OrderManager.nomorMeja}  —  ${OrderManager.namaPembeli}")
        sb.appendLine()
        OrderManager.daftarPesanan.forEach { item ->
            val subtotal = String.format("%,d", item.harga * item.qty).replace(",", ".")
            sb.appendLine("${item.nama} x${item.qty}   Rp $subtotal")
        }
        sb.appendLine()
        val total = String.format("%,d", OrderManager.totalPembayaran).replace(",", ".")
        sb.append("Total   Rp $total")

        tvOrderDetail.text = sb.toString()

        val sudahDiproses = OrderManager.status != OrderManager.Status.MENUNGGU
        layoutTombol.visibility = if (sudahDiproses) View.GONE else View.VISIBLE

        if (!sudahDiproses) {
            btnSetujui.setOnClickListener {
                OrderManager.status = OrderManager.Status.DITERIMA
                Toast.makeText(this, "Pesanan disetujui ✓", Toast.LENGTH_SHORT).show()
                layoutTombol.visibility = View.GONE
                tvOrderDetail.append("\n\n✓ Sudah disetujui")
            }

            btnTolak.setOnClickListener {
                OrderManager.status = OrderManager.Status.DITOLAK
                Toast.makeText(this, "Pesanan ditolak", Toast.LENGTH_SHORT).show()
                layoutTombol.visibility = View.GONE
                tvOrderDetail.append("\n\n✗ Ditolak")
            }
        }
    }
}
