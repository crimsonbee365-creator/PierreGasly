package com.pierregasly.app.data.api

import com.pierregasly.app.data.model.supabase.ProductRow
import com.pierregasly.app.data.model.supabase.UserRewardRow
import com.pierregasly.app.data.model.supabase.UserRow
import com.pierregasly.app.data.model.supabase.UserRowUpsert
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseRestService {

    @POST("rest/v1/users?on_conflict=email")
    suspend fun upsertUser(
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body body: List<UserRowUpsert>
    ): Response<List<UserRowUpsert>>

    @GET("rest/v1/users")
    suspend fun getUserByAuthId(
        @Header("Authorization") bearer: String,
        @Query("auth_user_id") authFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<UserRowUpsert>>

    @GET("rest/v1/users")
    suspend fun getUserByEmail(
        @Header("Authorization") bearer: String,
        @Query("email") emailFilter: String,
        @Query("select") select: String = "user_id,email,full_name,phone,role,status"
    ): Response<List<UserRow>>

    @PATCH("rest/v1/users")
    suspend fun linkAuthIdByEmail(
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Query("email") emailFilter: String,
        @Body body: Map<String, Any?>
    ): Response<List<UserRowUpsert>>

    @GET("rest/v1/products")
    suspend fun getProducts(
        @Header("Authorization") bearer: String,
        @Query("select") select: String = "product_id,product_name,size_kg,price,product_image,stock_quantity,is_active",
        @Query("is_active") activeFilter: String = "eq.true",
        @Query("order") order: String = "stock_quantity.desc"
    ): Response<List<ProductRow>>

    @GET("rest/v1/user_rewards")
    suspend fun getUserRewards(
        @Header("Authorization") bearer: String,
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "user_id,total_points,redeemed_points,tier"
    ): Response<List<UserRewardRow>>
}
