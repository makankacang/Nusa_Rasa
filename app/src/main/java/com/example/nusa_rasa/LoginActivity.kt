package com.example.nusa_rasa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private val ADMIN_EMAIL = "admin@nusarasa.id"
    private val ADMIN_PASSWORD = "admin123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val tvError = findViewById<TextView>(R.id.tvError)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val loadingOverlay = findViewById<FrameLayout>(R.id.loadingOverlay)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            tilEmail.error = null
            tilPassword.error = null
            tvError.visibility = View.GONE

            if (email.isEmpty()) {
                tilEmail.error = "Email wajib diisi"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilPassword.error = "Password wajib diisi"
                return@setOnClickListener
            }

            loadingOverlay.visibility = View.VISIBLE

            loadingOverlay.postDelayed({
                loadingOverlay.visibility = View.GONE

                if (email == ADMIN_EMAIL && password == ADMIN_PASSWORD) {
                    startActivity(Intent(this, admin::class.java))
                } else {
                    tvError.text = "Email atau password salah"
                    tvError.visibility = View.VISIBLE
                }
            }, 800)
        }
    }
}
