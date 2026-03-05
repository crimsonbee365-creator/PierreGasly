package com.pierregasly.app.data.model.supabase

import com.google.gson.annotations.SerializedName

data class SupabaseSignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, Any>? = null
)

data class SupabasePasswordGrantRequest(
    val email: String,
    val password: String
)

data class SupabaseRecoverRequest(
    val email: String
)

data class SupabaseAuthResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    val user: SupabaseUser? = null
)

data class SupabaseUser(
    val id: String? = null,
    val email: String? = null,
    @SerializedName("email_confirmed_at") val emailConfirmedAt: String? = null
)

/**
 * Payload for your `public.users` table.
 * Includes commonly used profile fields for the web/admin users table.
 */
data class UserRowUpsert(
    @SerializedName("auth_user_id") val authUserId: String? = null,
    @SerializedName("full_name") val fullName: String,
    val email: String,
    val phone: String,
    val role: String = "customer",
    val status: String = "active",
    @SerializedName("email_verified") val emailVerified: Boolean = true
)

data class UserRow(
    @SerializedName("user_id") val userId: Int,
    val email: String,
    @SerializedName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val status: String? = null
)

data class ProductRow(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("size_kg") val sizeKg: Int? = null,
    val price: Double? = null,
    @SerializedName("product_image") val productImage: String? = null,
    @SerializedName("stock_quantity") val stockQuantity: Int? = null,
    @SerializedName("is_active") val isActive: Boolean? = null
)

data class UserRewardRow(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("total_points") val totalPoints: Int? = null,
    @SerializedName("redeemed_points") val redeemedPoints: Int? = null,
    val tier: String? = null
)

data class SupabaseVerifyOtpRequest(
    val type: String,
    val email: String,
    val token: String
)

data class SupabaseResendRequest(
    val type: String,
    val email: String
)

data class SupabaseUpdateUserRequest(
    val password: String
)

data class SupabaseUserResponse(
    val user: SupabaseUser? = null
)
