package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nusa_rasa.adapter.MenuAdapter
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var toolbar: Toolbar
    private lateinit var tvMenuCount: TextView
    private lateinit var btnAddMenu: MaterialButton
    private lateinit var chipGroupKategori: ChipGroup
    private lateinit var layoutLoading: LinearLayout
    private lateinit var rvMenu: RecyclerView
    private lateinit var adapter: MenuAdapter

    private var currentKategori: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        session = SessionManager(this)

        bindViews()
        setupToolbar()
        setupFilterChips()
        setupRecyclerView()
        loadMenu()
    }

    override fun onResume() {
        super.onResume()
        loadMenu()
    }

    private fun bindViews() {
        toolbar           = findViewById(R.id.toolbar)
        tvMenuCount       = findViewById(R.id.tvMenuCount)
        btnAddMenu        = findViewById(R.id.btnAddMenu)
        chipGroupKategori = findViewById(R.id.chipGroupKategori)
        layoutLoading     = findViewById(R.id.layoutLoading)
        rvMenu            = findViewById(R.id.rvMenu)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnAddMenu.setOnClickListener {
            startActivity(Intent(this, AddMenuActivity::class.java))
        }
    }

    private fun setupFilterChips() {
        chipGroupKategori.setOnCheckedStateChangeListener { _, checkedIds ->
            currentKategori = when (checkedIds.firstOrNull()) {
                R.id.chipKatMakanan  -> "makanan"
                R.id.chipKatMinuman  -> "minuman"
                R.id.chipKatSayuran  -> "sayuran"
                else                 -> null
            }
            loadMenu()
        }
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(
            items = mutableListOf(),
            onEdit = { menuItem ->
                val intent = Intent(this, AddMenuActivity::class.java).apply {
                    putExtra("menu_id",          menuItem.id)
                    putExtra("menu_name",        menuItem.name)
                    putExtra("menu_price",       menuItem.price)
                    putExtra("menu_kategori",    menuItem.kategori)
                    putExtra("menu_description", menuItem.description ?: "")
                    putExtra("menu_available",   menuItem.isAvailable)
                    putExtra("menu_image_url",   menuItem.imageUrl ?: "")
                }
                startActivity(intent)
            },
            onDelete = { menuItem ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Menu")
                    .setMessage("Yakin ingin menghapus \"${menuItem.name}\"?")
                    .setPositiveButton("Hapus") { _, _ -> deleteMenu(menuItem.id) }
                    .setNegativeButton("Batal", null)
                    .show()
            },
            onToggleAvailable = { menuItem, isAvailable ->
                toggleAvailability(menuItem.id, isAvailable)
            }
        )
        rvMenu.layoutManager = GridLayoutManager(this, 2)
        rvMenu.adapter = adapter
    }

    private fun loadMenu() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMenu(
                    token    = session.getToken(),
                    kategori = currentKategori
                )
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    adapter.updateData(items)
                    tvMenuCount.text = "${items.size} item tersedia"
                }
            } catch (_: Exception) {
                Snackbar.make(rvMenu, "Gagal memuat menu", Snackbar.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun deleteMenu(menuId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteMenu(
                    token  = session.getToken(),
                    menuId = menuId
                )
                if (response.isSuccessful) {
                    adapter.removeItem(menuId)
                    Snackbar.make(rvMenu, "Menu dihapus", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(rvMenu, "Gagal menghapus menu", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(rvMenu, "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleAvailability(menuId: Int, isAvailable: Boolean) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.toggleMenuAvailability(
                    token  = session.getToken(),
                    menuId = menuId,
                    body   = mapOf("is_available" to isAvailable)
                )
                if (response.isSuccessful && response.body() != null) {
                    adapter.updateItem(response.body()!!)
                } else {
                    Snackbar.make(rvMenu, "Gagal memperbarui ketersediaan", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(rvMenu, "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        layoutLoading.visibility = if (loading) View.VISIBLE else View.GONE
        rvMenu.visibility        = if (loading) View.GONE    else View.VISIBLE
    }
}
