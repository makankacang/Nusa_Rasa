package com.example.nusa_rasa

data class MenuItem(
    val emoji: String,
    val nama: String,
    val harga: String,
    val kategori: String = "Semua"
)
