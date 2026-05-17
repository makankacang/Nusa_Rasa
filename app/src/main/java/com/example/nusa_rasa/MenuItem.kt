package com.example.nusa_rasa

data class MenuItem(
    val id: Int,
    val emoji: String,
    val nama: String,
    val harga: String,
    val imageUrl: String? = null   // ← tambahan untuk load gambar dari server
)
