package com.pierregasly.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pierregasly.app.R
import com.pierregasly.app.data.model.supabase.ProductRow
import com.pierregasly.app.data.repository.AppDataRepository
import com.pierregasly.app.data.repository.Result
import com.pierregasly.app.ui.auth.LoginActivity
import com.pierregasly.app.ui.common.MenuHelper
import com.pierregasly.app.ui.common.ThemePrefs
import com.pierregasly.app.utils.SessionManager
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private val session by lazy { SessionManager(this) }
    private val dataRepo by lazy { AppDataRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)

        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard)
        MainNavHelper.setup(this, activeTab = 0)

        findViewById<View>(R.id.btnMenu).setOnClickListener { MenuHelper.show(it, this) }

        val email = session.getUserEmail().orEmpty()
        val name = session.getUserName().orEmpty().ifBlank { email.substringBefore('@').replaceFirstChar { it.uppercase() } }

        findViewById<TextView>(R.id.tvGreeting).text = "Welcome back, $name!"
        findViewById<TextView>(R.id.tvEmail).text = if (email.isBlank()) "-" else email
        findViewById<TextView>(R.id.tvStatus).text = if (email.isBlank()) "Profile incomplete" else "Email verified"

        loadDashboardData()
    }

    private fun loadDashboardData() {
        val token = session.getAccessToken().orEmpty()
        val email = session.getUserEmail().orEmpty()
        if (token.isBlank()) return

        lifecycleScope.launch {
            when (val rewardsRes = dataRepo.getRewardsSummary(token, email)) {
                is Result.Success -> {
                    findViewById<TextView>(R.id.tvRewardTier).text = rewardsRes.data.tier
                    findViewById<TextView>(R.id.tvRewardPoints).text = "${rewardsRes.data.pointsBalance} pts"
                }
                else -> Unit
            }

            when (val productsRes = dataRepo.getTopProducts(token, 3)) {
                is Result.Success -> renderTopProducts(productsRes.data)
                else -> Unit
            }
        }
    }

    private fun renderTopProducts(products: List<ProductRow>) {
        val names = listOf(
            findViewById<TextView>(R.id.tvTopName1),
            findViewById<TextView>(R.id.tvTopName2),
            findViewById<TextView>(R.id.tvTopName3)
        )
        val variants = listOf(
            findViewById<TextView>(R.id.tvTopVariant1),
            findViewById<TextView>(R.id.tvTopVariant2),
            findViewById<TextView>(R.id.tvTopVariant3)
        )
        val prices = listOf(
            findViewById<TextView>(R.id.tvTopPrice1),
            findViewById<TextView>(R.id.tvTopPrice2),
            findViewById<TextView>(R.id.tvTopPrice3)
        )

        for (i in 0..2) {
            val p = products.getOrNull(i)
            names[i].text = p?.productName ?: "No product yet"
            variants[i].text = p?.sizeKg?.let { "${it}kg variant" } ?: "Awaiting stock"
            prices[i].text = p?.price?.let { "₱${"%.2f".format(it)}" } ?: "-"
        }
    }
}
