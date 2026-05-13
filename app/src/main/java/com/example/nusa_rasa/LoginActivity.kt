package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.model.LoginRequest
import com.example.nusa_rasa.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tvError: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var loadingOverlay: View
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        session = SessionManager(this)

        // Jika sudah login, langsung ke Dashboard
        if (session.isLoggedIn()) {
            goToDashboard()
            return
        }

        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        tilEmail      = findViewById(R.id.tilEmail)
        tilPassword   = findViewById(R.id.tilPassword)
        etEmail       = findViewById(R.id.etEmail)
        etPassword    = findViewById(R.id.etPassword)
        tvError       = findViewById(R.id.tvError)
        btnLogin      = findViewById(R.id.btnLogin)
        loadingOverlay = findViewById(R.id.loadingOverlay)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener { attemptLogin() }

        // Hapus error saat user mulai mengetik
        etEmail.setOnFocusChangeListener    { _, _ -> clearError() }
        etPassword.setOnFocusChangeListener { _, _ -> clearError() }
    }

    private fun attemptLogin() {
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        // Validasi lokal
        if (email.isEmpty()) {
            tilEmail.error = "Email tidak boleh kosong"
            return
        }
        if (password.isEmpty()) {
            tilPassword.error = "Password tidak boleh kosong"
            return
        }
        tilEmail.error    = null
        tilPassword.error = null

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(email, password)
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    session.saveSession(
                        token      = body.token,
                        adminId    = body.admin.id,
                        adminName  = body.admin.name,
                        adminEmail = body.admin.email
                    )
                    goToDashboard()
                } else {
                    val errorMsg = when (response.code()) {
                        401  -> "Email atau password salah"
                        404  -> "Akun tidak ditemukan"
                        else -> "Login gagal (${response.code()})"
                    }
                    showError(errorMsg)
                }
            } catch (e: Exception) {
                showError("Tidak dapat terhubung ke server. Periksa koneksi internet.")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        loadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled        = !loading
    }

    private fun showError(msg: String) {
        tvError.text       = msg
        tvError.visibility = View.VISIBLE
    }

    private fun clearError() {
        tvError.visibility = View.GONE
        tilEmail.error     = null
        tilPassword.error  = null
    }
}
