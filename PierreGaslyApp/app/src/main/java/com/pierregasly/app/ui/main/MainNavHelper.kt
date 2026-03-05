package com.pierregasly.app.ui.main

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.pierregasly.app.R

object MainNavHelper {

    fun setup(activity: Activity, activeTab: Int) {
        val tabs = listOf(
            Triple(R.id.navDashboard, R.id.navDashboardLabel, DashboardActivity::class.java),
            Triple(R.id.navProducts, R.id.navProductsLabel, ProductsActivity::class.java),
            Triple(R.id.navOrders, R.id.navOrdersLabel, OrdersActivity::class.java),
            Triple(R.id.navRewards, R.id.navRewardsLabel, RewardsActivity::class.java),
            Triple(R.id.navProfile, R.id.navProfileLabel, ProfileActivity::class.java)
        )

        tabs.forEachIndexed { idx, (iconId, labelId, cls) ->
            val icon = activity.findViewById<ImageView?>(iconId)
            val label = activity.findViewById<TextView?>(labelId)
            if (icon == null || label == null) return@forEachIndexed

            val selected = idx == activeTab
            val tint = activity.getColor(if (selected) R.color.pg_primary else R.color.text_secondary_light)
            icon.setColorFilter(tint)
            label.setTextColor(tint)

            val click = View.OnClickListener {
                if (!selected) {
                    activity.startActivity(Intent(activity, cls))
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    activity.finish()
                }
            }
            icon.setOnClickListener(click)
            label.setOnClickListener(click)
        }
    }
}
