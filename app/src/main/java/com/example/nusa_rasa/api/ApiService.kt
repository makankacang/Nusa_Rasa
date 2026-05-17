package com.example.nusa_rasa.api

import com.example.nusa_rasa.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ────────────────────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ─── Dashboard ───────────────────────────────────────────────────────────

    @GET("dashboard/stats")
    suspend fun getDashboardStats(
        @Header("Authorization") token: String
    ): Response<DashboardStats>

    // ─── Orders ──────────────────────────────────────────────────────────────

    @GET("orders/")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): Response<List<Order>>

    @GET("orders/{id}")
    suspend fun getOrderDetail(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): Response<Order>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int,
        @Body request: UpdateStatusRequest
    ): Response<Order>

    // ─── Menu ─────────────────────────────────────────────────────────────────

    @GET("menu/")
    suspend fun getMenu(
        @Header("Authorization") token: String,
        @Query("kategori") kategori: String? = null
    ): Response<List<MenuItem>>

    @GET("api/v1/menus")
    suspend fun getPublicMenu(
        @Query("kategori") kategori: String? = null
    ): Response<List<MenuItem>>

    @Multipart
    @POST("menu/")
    suspend fun createMenu(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("kategori") kategori: RequestBody,
        @Part("description") description: RequestBody,
        @Part("is_available") isAvailable: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<MenuItem>

    @Multipart
    @PUT("menu/{id}")
    suspend fun updateMenu(
        @Header("Authorization") token: String,
        @Path("id") menuId: Int,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("kategori") kategori: RequestBody,
        @Part("description") description: RequestBody,
        @Part("is_available") isAvailable: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<MenuItem>

    @PATCH("menu/{id}/availability")
    suspend fun toggleMenuAvailability(
        @Header("Authorization") token: String,
        @Path("id") menuId: Int,
        @Body body: Map<String, Boolean>
    ): Response<MenuItem>

    @DELETE("menu/{id}")
    suspend fun deleteMenu(
        @Header("Authorization") token: String,
        @Path("id") menuId: Int
    ): Response<MessageResponse>

    // ─── Payments ─────────────────────────────────────────────────────────────

    @GET("payments/")
    suspend fun getPayments(
        @Header("Authorization") token: String
    ): Response<List<Payment>>

    @POST("api/v1/orders")
    suspend fun createPublicOrder(
        @Body request: CreateOrderRequest
    ): Response<Order>

    @GET("api/v1/orders/{id}")
    suspend fun getPublicOrderDetail(
        @Path("id") orderId: Int
    ): Response<Order>

    @POST("api/v1/orders/{id}/confirm-payment")
    suspend fun confirmPublicPayment(
        @Path("id") orderId: Int
    ): Response<Order>
}
