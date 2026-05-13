package com.example.nusa_rasa.api

import com.example.nusa_rasa.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ────────────────────────────────────────────────────────────────

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ─── Menu (publik) ────────────────────────────────────────────────────────

    @GET("api/v1/menus")
    suspend fun getMenus(
        @Query("category") category: String? = null
    ): Response<List<MenuItem>>

    // ─── Orders (publik — pembeli) ────────────────────────────────────────────

    @POST("api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<Order>

    @GET("api/v1/orders/{id}")
    suspend fun getOrder(@Path("id") orderId: Int): Response<Order>

    // ─── Orders (admin) ──────────────────────────────────────────────────────

    @GET("api/v1/admin/orders")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): Response<List<Order>>

    @PUT("api/v1/admin/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int,
        @Body request: UpdateStatusRequest
    ): Response<Order>

    // ─── Menu (admin) ─────────────────────────────────────────────────────────

    @DELETE("api/v1/menus/{id}")
    suspend fun deleteMenu(
        @Header("Authorization") token: String,
        @Path("id") menuId: Int
    ): Response<MessageResponse>

    // ─── Payments ─────────────────────────────────────────────────────────────

    @GET("api/v1/payments/order/{orderId}")
    suspend fun getPaymentByOrder(
        @Path("orderId") orderId: Int
    ): Response<Payment>
}
