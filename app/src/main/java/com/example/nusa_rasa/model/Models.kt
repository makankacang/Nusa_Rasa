package com.example.nusa_rasa.model

import com.google.gson.annotations.SerializedName

// ─── Auth ────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val admin: Admin
)

data class Admin(
    val id: Int,
    val name: String,
    val email: String
)

// ─── Orders ──────────────────────────────────────────────────────────────────

data class Order(
    val id: Int,
    @SerializedName("order_code") val orderCode: String,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("table_number") val tableNumber: String,
    val items: List<OrderItem>,
    val total: Long,
    val status: String,           // pending | approved | paid | done | rejected
    val note: String?,
    @SerializedName("created_at") val createdAt: String
)

data class OrderItem(
    @SerializedName("menu_id") val menuId: Int,
    @SerializedName("menu_name") val menuName: String,
    val quantity: Int,
    val price: Long,
    val subtotal: Long
)

data class UpdateStatusRequest(
    val status: String
)

// ─── Menu ─────────────────────────────────────────────────────────────────────

data class MenuItem(
    val id: Int,
    val name: String,
    val price: Long,
    val kategori: String,         // makanan | minuman | sayuran
    val description: String?,
    @SerializedName("is_available") val isAvailable: Boolean,
    @SerializedName("image_url") val imageUrl: String?
)

// ─── Payment ──────────────────────────────────────────────────────────────────

data class Payment(
    val id: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("order_code") val orderCode: String,
    @SerializedName("buyer_name") val buyerName: String,
    val amount: Long,
    val status: String,           // paid | unpaid | failed
    @SerializedName("created_at") val createdAt: String
)

// ─── Dashboard ────────────────────────────────────────────────────────────────

data class DashboardStats(
    @SerializedName("total_orders") val totalOrders: Int,
    val pending: Int,
    val approved: Int,
    val paid: Int,
    val done: Int,
    val revenue: Long,
    @SerializedName("revenue_transactions") val revenueTransactions: Int,
    @SerializedName("recent_orders") val recentOrders: List<Order>
)

// ─── Generic response wrapper ─────────────────────────────────────────────────

data class MessageResponse(
    val message: String
)
