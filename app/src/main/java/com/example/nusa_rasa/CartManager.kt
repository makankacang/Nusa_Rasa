package com.example.nusa_rasa

object CartManager {

    val items = mutableListOf<CartItem>()

    fun tambahItem(menu: MenuItem) {

        val hargaAngka = menu.harga
            .replace("Rp", "")
            .replace(".", "")
            .trim()
            .toInt()

        val itemSudahAda = items.find { it.nama == menu.nama }

        if (itemSudahAda != null) {
            itemSudahAda.qty++
        } else {
            items.add(
                CartItem(
                    emoji = menu.emoji,
                    nama = menu.nama,
                    harga = hargaAngka,
                    qty = 1
                )
            )
        }
    }

    fun subtotal(): Int {
        return items.sumOf { it.harga * it.qty }
    }

    fun pajak(): Int {
        return subtotal() * 5 / 100
    }

    fun total(): Int {
        return subtotal() + pajak()
    }

    fun totalItem(): Int {
        return items.sumOf { it.qty }
    }
}