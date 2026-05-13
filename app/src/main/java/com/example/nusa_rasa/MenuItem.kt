package com.example.nusa_rasa

data class MenuItem(
    val menuId: Int = 0,
    val emoji: String,
    val nama: String,
    val harga: String,
    val kategori: String = "Semua"
)
