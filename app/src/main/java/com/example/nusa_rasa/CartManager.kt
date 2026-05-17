package com.example.nusa_rasa

object CartManager {

    val items = mutableListOf<CartItem>()

    fun tambahItem(menu: MenuItem) {
        val hargaAngka = menu.harga
            .replace("Rp", "")
            .replace(".", "")
            .trim()
            .toInt()

        val itemSudahAda = items.find { it.menuId == menu.id }

        if (itemSudahAda != null) {
            itemSudahAda.qty++
        } else {
            items.add(
                CartItem(
                    menuId   = menu.id,
                    emoji    = menu.emoji,
                    nama     = menu.nama,
                    harga    = hargaAngka,
                    qty      = 1,
                    imageUrl = menu.imageUrl   // ← tambahan
                )
            )
        }
    }

    fun subtotal(): Int = items.sumOf { it.harga * it.qty }
    fun pajak(): Int    = subtotal() * 5 / 100
    fun total(): Int    = subtotal() + pajak()
    fun totalItem(): Int = items.sumOf { it.qty }
}
