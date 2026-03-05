package com.pierregasly.app.data.repository

import com.pierregasly.app.data.api.SupabaseClient
import com.pierregasly.app.data.model.supabase.ProductRow
import com.pierregasly.app.data.model.supabase.UserRewardRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RewardsSummary(
    val tier: String,
    val pointsBalance: Int
)

class AppDataRepository {

    suspend fun getTopProducts(accessToken: String, limit: Int = 3): Result<List<ProductRow>> = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured) return@withContext Result.Error("Supabase configuration is missing.")
        try {
            val resp = SupabaseClient.rest.getProducts(bearer = "Bearer $accessToken")
            if (!resp.isSuccessful) {
                return@withContext Result.Error("Failed to load products.")
            }
            val top = resp.body().orEmpty().take(limit)
            Result.Success(top)
        } catch (e: Exception) {
            Result.Error("Failed to load products: ${e.message}")
        }
    }

    suspend fun getAllProducts(accessToken: String): Result<List<ProductRow>> = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured) return@withContext Result.Error("Supabase configuration is missing.")
        try {
            val resp = SupabaseClient.rest.getProducts(bearer = "Bearer $accessToken")
            if (!resp.isSuccessful) {
                return@withContext Result.Error("Failed to load products.")
            }
            Result.Success(resp.body().orEmpty())
        } catch (e: Exception) {
            Result.Error("Failed to load products: ${e.message}")
        }
    }

    suspend fun getRewardsSummary(accessToken: String, email: String): Result<RewardsSummary> = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured) return@withContext Result.Error("Supabase configuration is missing.")
        try {
            val userResp = SupabaseClient.rest.getUserByEmail(
                bearer = "Bearer $accessToken",
                emailFilter = "eq.$email"
            )
            if (!userResp.isSuccessful) {
                return@withContext Result.Error("Failed to load profile.")
            }
            val user = userResp.body().orEmpty().firstOrNull()
                ?: return@withContext Result.Success(RewardsSummary("Bronze", 0))

            val rewardsResp = SupabaseClient.rest.getUserRewards(
                bearer = "Bearer $accessToken",
                userIdFilter = "eq.${user.userId}"
            )

            if (!rewardsResp.isSuccessful) {
                return@withContext Result.Success(RewardsSummary("Bronze", 0))
            }

            val rewards: UserRewardRow? = rewardsResp.body().orEmpty().firstOrNull()
            val total = rewards?.totalPoints ?: 0
            val redeemed = rewards?.redeemedPoints ?: 0
            val available = (total - redeemed).coerceAtLeast(0)
            Result.Success(RewardsSummary(rewards?.tier ?: "Bronze", available))
        } catch (e: Exception) {
            Result.Error("Failed to load rewards: ${e.message}")
        }
    }
}
