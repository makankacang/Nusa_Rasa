package com.example.nusa_rasa

object CartManager {

    val items = mutableListOf<CartItem>()

    fun tambahItem(menu: MenuItem) {
        val hargaAngka = menu.harga
            .replace("Rp", "")
            .replace(".", "")
            .trim()
            .toInt()

        val itemSudahAda = items.find { it.menuId == menu.menuId }

        if (itemSudahAda != null) {
            itemSudahAda.qty++
        } else {
            items.add(
                CartItem(
                    menuId = menu.menuId,
                    emoji = menu.emoji,
                    nama = menu.nama,
                    harga = hargaAngka,
                    qty = 1
                )
            )
        }
    }

    fun subtotal(): Int = items.sumOf { it.harga * it.qty }
    fun pajak(): Int = subtotal() * 5 / 100
    fun total(): Int = subtotal() + pajak()
    fun totalItem(): Int = items.sumOf { it.qty }
}
