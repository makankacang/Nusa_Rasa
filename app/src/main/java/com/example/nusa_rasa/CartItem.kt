package com.example.nusa_rasa

data class CartItem(
    val emoji: String,
    val nama: String,
    val harga: Int,
    var qty: Int = 1
)