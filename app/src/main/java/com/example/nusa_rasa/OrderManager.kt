package com.example.nusa_rasa

object OrderManager {

    enum class Status { MENUNGGU, DITERIMA, DITOLAK }

    var orderId: Int = 0
    var status = Status.MENUNGGU
    var namaPembeli = ""
    var nomorMeja = ""
    var daftarPesanan: List<CartItem> = emptyList()
    var totalPembayaran = 0

    fun buatPesanan(nama: String, meja: String, items: List<CartItem>, total: Int) {
        namaPembeli = nama
        nomorMeja = meja
        daftarPesanan = items.toList()
        totalPembayaran = total
        status = Status.MENUNGGU
    }

    fun reset() {
        orderId = 0
        status = Status.MENUNGGU
        namaPembeli = ""
        nomorMeja = ""
        daftarPesanan = emptyList()
        totalPembayaran = 0
    }
}
