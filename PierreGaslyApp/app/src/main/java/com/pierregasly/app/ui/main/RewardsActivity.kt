package com.pierregasly.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pierregasly.app.R
import com.pierregasly.app.data.repository.AppDataRepository
import com.pierregasly.app.data.repository.Result
import com.pierregasly.app.ui.auth.LoginActivity
import com.pierregasly.app.ui.common.MenuHelper
import com.pierregasly.app.ui.common.ThemePrefs
import com.pierregasly.app.utils.SessionManager
import kotlinx.coroutines.launch

class RewardsActivity : AppCompatActivity() {
    private val session by lazy { SessionManager(this) }
    private val repo by lazy { AppDataRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)

        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_rewards)
        MainNavHelper.setup(this, activeTab = 3)
        findViewById<View>(R.id.btnMenu).setOnClickListener { MenuHelper.show(it, this) }
        findViewById<TextView>(R.id.tvPageTitle).text = "Rewards"

        loadRewards()
    }

    private fun loadRewards() {
        val token = session.getAccessToken().orEmpty()
        val email = session.getUserEmail().orEmpty()
        lifecycleScope.launch {
            when (val res = repo.getRewardsSummary(token, email)) {
                is Result.Success -> {
                    findViewById<TextView>(R.id.tvTier).text = res.data.tier
                    findViewById<TextView>(R.id.tvPoints).text = "${res.data.pointsBalance} pts"
                }
                is Result.Error -> {
                    findViewById<TextView>(R.id.tvPoints).text = res.message
                }
                else -> Unit
            }
        }
    }
}
