package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.adapter.OrderAdapter
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.utils.SessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    // Stat card view refs (via include IDs → child views)
    private lateinit var tvRevenue: TextView
    private lateinit var tvRevenueInfo: TextView
    private lateinit var tvSeeAllOrders: TextView
    private lateinit var rvRecentOrders: RecyclerView

    private val rupiahFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)
        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        bindViews()
        setupToolbar()
        setupNavDrawer()
        setupNavHeader()
        loadDashboard()
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    private fun bindViews() {
        drawerLayout     = findViewById(R.id.drawerLayout)
        navigationView   = findViewById(R.id.navigationView)
        toolbar          = findViewById(R.id.toolbar)
        tvRevenue        = findViewById(R.id.tvRevenue)
        tvRevenueInfo    = findViewById(R.id.tvRevenueInfo)
        tvSeeAllOrders   = findViewById(R.id.tvSeeAllOrders)
        rvRecentOrders   = findViewById(R.id.rvRecentOrders)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_dashboard, R.string.nav_dashboard
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupNavDrawer() {
        navigationView.setCheckedItem(R.id.nav_dashboard)
        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.nav_dashboard -> { /* sudah di sini */ }
                R.id.nav_orders    -> startActivity(Intent(this, OrdersActivity::class.java))
                R.id.nav_menu      -> startActivity(Intent(this, MenuActivity::class.java))
                R.id.nav_payments  -> startActivity(Intent(this, PaymentActivity::class.java))
                R.id.nav_logout    -> logout()
            }
            true
        }
    }

    private fun setupNavHeader() {
        val header = navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvNavAdminName)?.text  = session.getAdminName()
        header.findViewById<TextView>(R.id.tvNavAdminEmail)?.text = session.getAdminEmail()
        // Inisial avatar
        val initial = session.getAdminName().firstOrNull()?.uppercase() ?: "A"
        header.findViewById<TextView>(R.id.tvNavAvatar)?.text = initial
    }

    private fun loadDashboard() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getDashboardStats(session.getToken())
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!

                    // Revenue card
                    tvRevenue.text     = rupiahFormat.format(stats.revenue)
                    tvRevenueInfo.text = "dari ${stats.revenueTransactions} transaksi berhasil"

                    // Stat cards (via include IDs)
                    bindStatCard(R.id.statTotalOrders, "Total Pesanan", stats.totalOrders.toString(), "hari ini")
                    bindStatCard(R.id.statPending,     "Pending",       stats.pending.toString(),     "menunggu approval")
                    bindStatCard(R.id.statPaid,        "Paid",          stats.paid.toString(),        "sudah bayar")
                    bindStatCard(R.id.statDone,        "Selesai",       stats.done.toString(),        "selesai hari ini")

                    // Recent orders
                    val adapter = OrderAdapter(
                        orders  = stats.recentOrders.toMutableList(),
                        onDetail = { order ->
                            val intent = Intent(this@DashboardActivity, OrderDetailActivity::class.java)
                            intent.putExtra("order_id", order.id)
                            startActivity(intent)
                        },
                        compact = true
                    )
                    rvRecentOrders.layoutManager = LinearLayoutManager(this@DashboardActivity)
                    rvRecentOrders.adapter = adapter
                }
            } catch (_: Exception) {
                // Tidak tampilkan error agar UX tetap bersih; cukup biarkan nilai default
            }
        }
    }

    private fun bindStatCard(includeId: Int, label: String, value: String, sub: String) {
        val card = findViewById<android.view.View>(includeId) ?: return
        card.findViewById<TextView>(R.id.tvStatLabel)?.text = label
        card.findViewById<TextView>(R.id.tvStatValue)?.text = value
        card.findViewById<TextView>(R.id.tvStatSub)?.text   = sub
    }

    private fun logout() {
        session.clearSession()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
