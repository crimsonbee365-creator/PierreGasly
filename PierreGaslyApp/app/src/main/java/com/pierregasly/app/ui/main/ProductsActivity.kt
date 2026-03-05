package com.pierregasly.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.pierregasly.app.R
import com.pierregasly.app.data.model.supabase.ProductRow
import com.pierregasly.app.data.repository.AppDataRepository
import com.pierregasly.app.data.repository.Result
import com.pierregasly.app.ui.auth.LoginActivity
import com.pierregasly.app.ui.common.MenuHelper
import com.pierregasly.app.ui.common.ThemePrefs
import com.pierregasly.app.utils.SessionManager
import kotlinx.coroutines.launch

class ProductsActivity : AppCompatActivity() {
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

        setContentView(R.layout.activity_products)
        MainNavHelper.setup(this, activeTab = 1)
        findViewById<View>(R.id.btnMenu).setOnClickListener { MenuHelper.show(it, this) }

        loadProducts()
    }

    private fun loadProducts() {
        val token = session.getAccessToken().orEmpty()
        val container = findViewById<LinearLayout>(R.id.productsContainer)
        val emptyState = findViewById<TextView>(R.id.tvProductsEmpty)

        lifecycleScope.launch {
            when (val res = repo.getAllProducts(token)) {
                is Result.Success -> {
                    container.removeAllViews()
                    val items = res.data
                    if (items.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        return@launch
                    }
                    emptyState.visibility = View.GONE
                    items.forEach { product ->
                        container.addView(createProductCard(product))
                    }
                }
                is Result.Error -> {
                    emptyState.visibility = View.VISIBLE
                    emptyState.text = res.message
                }
                else -> Unit
            }
        }
    }

    private fun createProductCard(product: ProductRow): View {
        val card = layoutInflater.inflate(R.layout.item_product_card, null) as MaterialCardView
        card.findViewById<TextView>(R.id.tvProductName).text = product.productName
        card.findViewById<TextView>(R.id.tvProductVariant).text = product.sizeKg?.let { "${it}kg cylinder" } ?: "Standard"
        card.findViewById<TextView>(R.id.tvProductPrice).text = product.price?.let { "₱${"%.2f".format(it)}" } ?: "-"
        return card
    }
}
