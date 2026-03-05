package com.pierregasly.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.pierregasly.app.R
import com.pierregasly.app.data.model.supabase.ProductRow
import com.pierregasly.app.data.repository.AppDataRepository
import com.pierregasly.app.data.repository.Result
import com.pierregasly.app.ui.auth.LoginActivity
import com.pierregasly.app.ui.common.MenuHelper
import com.pierregasly.app.ui.common.ThemePrefs
import com.pierregasly.app.utils.SessionManager
import kotlinx.coroutines.launch
import androidx.core.widget.doAfterTextChanged

class ProductsActivity : AppCompatActivity() {
    private val session by lazy { SessionManager(this) }
    private val repo by lazy { AppDataRepository() }

    private var allProducts: List<ProductRow> = emptyList()
    private var selectedBrand: String = "All brands"

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
        findViewById<View?>(R.id.btnMenu)?.setOnClickListener { MenuHelper.show(it, this) }
        findViewById<TextView?>(R.id.tvPageTitle)?.text = "Products"

        bindFilters()
        loadProducts()
    }

    private fun bindFilters() {
        findViewById<TextInputEditText>(R.id.etSearch).doAfterTextChanged { applyFilters() }

        findViewById<ChipGroup>(R.id.chipCategory).setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }

        val brandView = findViewById<AutoCompleteTextView>(R.id.actBrand)
        brandView.setOnItemClickListener { parent, _, position, _ ->
            selectedBrand = parent.getItemAtPosition(position)?.toString().orEmpty().ifBlank { "All brands" }
            applyFilters()
        }
    }

    private fun loadProducts() {
        val token = session.getAccessToken().orEmpty()
        val emptyState = findViewById<TextView>(R.id.tvProductsEmpty)

        lifecycleScope.launch {
            when (val res = repo.getAllProducts(token)) {
                is Result.Success -> {
                    allProducts = res.data
                    setupBrandDropdown(allProducts)
                    applyFilters()
                }
                is Result.Error -> {
                    emptyState.visibility = View.VISIBLE
                    emptyState.text = res.message
                }
                else -> Unit
            }
        }
    }

    private fun setupBrandDropdown(products: List<ProductRow>) {
        val brands = mutableListOf("All brands")
        brands.addAll(products.mapNotNull { it.productName.substringBefore(' ').trim().takeIf { b -> b.isNotBlank() } }.distinct().sorted())

        val actBrand = findViewById<AutoCompleteTextView>(R.id.actBrand)
        actBrand.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, brands))
        actBrand.setText(selectedBrand, false)
    }

    private fun applyFilters() {
        val container = findViewById<LinearLayout>(R.id.productsContainer)
        val emptyState = findViewById<TextView>(R.id.tvProductsEmpty)

        val q = findViewById<TextInputEditText>(R.id.etSearch).text?.toString().orEmpty().trim().lowercase()
        val checkedId = findViewById<ChipGroup>(R.id.chipCategory).checkedChipId

        val categoryFilter: (ProductRow) -> Boolean = when (checkedId) {
            R.id.chipRefill -> { p -> p.productName.contains("refill", true) }
            R.id.chipLpg -> { p -> p.productName.contains("lpg", true) || p.productName.contains("tank", true) || p.productName.contains("cylinder", true) }
            else -> { _ -> true }
        }

        val filtered = allProducts.filter { p ->
            val brand = p.productName.substringBefore(' ').trim()
            val brandMatch = selectedBrand == "All brands" || brand.equals(selectedBrand, true)
            val searchMatch = q.isBlank() || p.productName.lowercase().contains(q)
            brandMatch && searchMatch && categoryFilter(p)
        }

        container.removeAllViews()
        if (filtered.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            emptyState.text = "No matching products found"
            container.addView(emptyState)
            return
        }

        emptyState.visibility = View.GONE
        filtered.forEach { product ->
            container.addView(createProductCard(product))
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
