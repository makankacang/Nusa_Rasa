package com.example.nusa_rasa.model

import com.google.gson.annotations.SerializedName

// ─── Auth ────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("admin_id") val adminId: Int,
    val name: String
)

// ─── Menu ─────────────────────────────────────────────────────────────────────

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Int,
    val image: String?,
    val category: String?,
    @SerializedName("is_available") val isAvailable: Boolean
)

// ─── Orders ──────────────────────────────────────────────────────────────────

data class CreateOrderItemRequest(
    @SerializedName("menu_id") val menuId: Int,
    val quantity: Int,
    val subtotal: Int
)

data class CreateOrderRequest(
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("table_number") val tableNumber: String?,
    val notes: String?,
    val items: List<CreateOrderItemRequest>
)

data class Order(
    val id: Int,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("table_number") val tableNumber: String?,
    @SerializedName("total_price") val totalPrice: Int,
    val status: String,
    val notes: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("order_items") val orderItems: List<OrderItem> = emptyList(),
    val payment: Payment?,
    val logs: List<OrderLog> = emptyList()
)

data class OrderItem(
    val id: Int,
    @SerializedName("menu_id") val menuId: Int,
    val quantity: Int,
    val subtotal: Int,
    val menu: MenuItem?
)

data class OrderLog(
    val id: Int,
    val status: String?,
    @SerializedName("created_at") val createdAt: String
)

data class UpdateStatusRequest(
    val status: String
)

// ─── Payment ──────────────────────────────────────────────────────────────────

data class Payment(
    val id: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("qris_code") val qrisCode: String?,
    @SerializedName("paid_at") val paidAt: String?
)

// ─── Generic response wrapper ─────────────────────────────────────────────────

data class MessageResponse(
    val message: String
)
