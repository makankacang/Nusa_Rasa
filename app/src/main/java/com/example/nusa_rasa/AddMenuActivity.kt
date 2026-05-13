package com.example.nusa_rasa

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.nusa_rasa.api.RetrofitClient
import com.example.nusa_rasa.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddMenuActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE = 101
    }

    private lateinit var session: SessionManager

    private lateinit var toolbar: Toolbar
    private lateinit var imgPreview: ImageView
    private lateinit var layoutUploadPlaceholder: LinearLayout
    private lateinit var btnUploadImage: MaterialButton
    private lateinit var etMenuName: TextInputEditText
    private lateinit var etMenuPrice: TextInputEditText
    private lateinit var actvKategori: AutoCompleteTextView
    private lateinit var etMenuDesc: TextInputEditText
    private lateinit var tvAvailabilityStatus: TextView
    private lateinit var switchMenuAvailable: SwitchMaterial
    private lateinit var layoutUploading: View
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton

    private var selectedImageUri: Uri? = null
    private var editMenuId: Int = -1   // -1 = mode tambah baru

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_menu)

        session = SessionManager(this)

        bindViews()
        setupToolbar()
        setupKategoriDropdown()
        setupImagePicker()
        setupSwitch()
        setupButtons()

        // Jika edit, isi field dengan data yang dikirim
        editMenuId = intent.getIntExtra("menu_id", -1)
        if (editMenuId != -1) prefillForEdit()
    }

    private fun bindViews() {
        toolbar                  = findViewById(R.id.toolbar)
        imgPreview               = findViewById(R.id.imgPreview)
        layoutUploadPlaceholder  = findViewById(R.id.layoutUploadPlaceholder)
        btnUploadImage           = findViewById(R.id.btnUploadImage)
        etMenuName               = findViewById(R.id.etMenuName)
        etMenuPrice              = findViewById(R.id.etMenuPrice)
        actvKategori             = findViewById(R.id.actvKategori)
        etMenuDesc               = findViewById(R.id.etMenuDesc)
        tvAvailabilityStatus     = findViewById(R.id.tvAvailabilityStatus)
        switchMenuAvailable      = findViewById(R.id.switchMenuAvailable)
        layoutUploading          = findViewById(R.id.layoutUploading)
        btnCancel                = findViewById(R.id.btnCancel)
        btnSave                  = findViewById(R.id.btnSave)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupKategoriDropdown() {
        val kategoriOptions = listOf("makanan", "minuman", "sayuran")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriOptions)
        actvKategori.setAdapter(adapter)
        actvKategori.threshold = 0
        actvKategori.setOnClickListener { actvKategori.showDropDown() }
    }

    private fun setupImagePicker() {
        val openGallery = {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE)
        }
        layoutUploadPlaceholder.setOnClickListener { openGallery() }
        btnUploadImage.setOnClickListener { openGallery() }
    }

    private fun setupSwitch() {
        switchMenuAvailable.isChecked = true
        tvAvailabilityStatus.text = "Tersedia"
        switchMenuAvailable.setOnCheckedChangeListener { _, isChecked ->
            tvAvailabilityStatus.text = if (isChecked) "Tersedia" else "Habis"
        }
    }

    private fun setupButtons() {
        btnCancel.setOnClickListener { finish() }
        btnSave.setOnClickListener   { saveMenu() }
    }

    private fun prefillForEdit() {
        toolbar.title = "Edit Menu"
        etMenuName.setText(intent.getStringExtra("menu_name"))
        etMenuPrice.setText(intent.getLongExtra("menu_price", 0).toString())
        actvKategori.setText(intent.getStringExtra("menu_kategori"), false)
        etMenuDesc.setText(intent.getStringExtra("menu_description"))
        val isAvailable = intent.getBooleanExtra("menu_available", true)
        switchMenuAvailable.isChecked = isAvailable
        tvAvailabilityStatus.text = if (isAvailable) "Tersedia" else "Habis"

        // Muat gambar existing
        val imageUrl = intent.getStringExtra("menu_image_url")
        if (!imageUrl.isNullOrBlank()) {
            val fullUrl = if (imageUrl.startsWith("http")) imageUrl
            else "${RetrofitClient.BASE_URL.trimEnd('/')}$imageUrl"
            Glide.with(this).load(fullUrl)
                .placeholder(R.drawable.ic_food_placeholder)
                .centerCrop()
                .into(imgPreview)
            imgPreview.visibility             = View.VISIBLE
            layoutUploadPlaceholder.visibility = View.GONE
            btnUploadImage.visibility         = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this).load(uri).centerCrop().into(imgPreview)
                imgPreview.visibility             = View.VISIBLE
                layoutUploadPlaceholder.visibility = View.GONE
                btnUploadImage.visibility         = View.VISIBLE
            }
        }
    }

    private fun saveMenu() {
        val name     = etMenuName.text.toString().trim()
        val priceStr = etMenuPrice.text.toString().trim()
        val kategori = actvKategori.text.toString().trim()
        val desc     = etMenuDesc.text.toString().trim()

        // Validasi dasar
        if (name.isEmpty())     { etMenuName.error = "Nama wajib diisi"; return }
        if (priceStr.isEmpty()) { etMenuPrice.error = "Harga wajib diisi"; return }
        if (kategori.isEmpty()) { actvKategori.error = "Kategori wajib dipilih"; return }

        val price = priceStr.toLongOrNull()
        if (price == null || price <= 0) {
            etMenuPrice.error = "Harga tidak valid"
            return
        }

        setUploading(true)

        // Buat RequestBody untuk field teks
        val nameBody     = name.toRequestBody("text/plain".toMediaType())
        val priceBody    = price.toString().toRequestBody("text/plain".toMediaType())
        val kategoriBody = kategori.toRequestBody("text/plain".toMediaType())
        val descBody     = desc.toRequestBody("text/plain".toMediaType())
        val availBody    = switchMenuAvailable.isChecked.toString()
            .toRequestBody("text/plain".toMediaType())

        // Buat MultipartBody.Part untuk gambar (jika dipilih)
        val imagePart: MultipartBody.Part? = selectedImageUri?.let { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return@let null
                val tempFile = File.createTempFile("menu_img", ".jpg", cacheDir)
                tempFile.outputStream().use { inputStream.copyTo(it) }
                val requestBody = tempFile.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
            } catch (e: Exception) {
                null
            }
        }

        lifecycleScope.launch {
            try {
                val response = if (editMenuId == -1) {
                    RetrofitClient.instance.createMenu(
                        token       = session.getToken(),
                        name        = nameBody,
                        price       = priceBody,
                        kategori    = kategoriBody,
                        description = descBody,
                        isAvailable = availBody,
                        image       = imagePart
                    )
                } else {
                    RetrofitClient.instance.updateMenu(
                        token       = session.getToken(),
                        menuId      = editMenuId,
                        name        = nameBody,
                        price       = priceBody,
                        kategori    = kategoriBody,
                        description = descBody,
                        isAvailable = availBody,
                        image       = imagePart
                    )
                }

                if (response.isSuccessful) {
                    Snackbar.make(
                        btnSave,
                        if (editMenuId == -1) "Menu berhasil ditambahkan!" else "Menu berhasil diperbarui!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Snackbar.make(btnSave, "Gagal menyimpan menu (${response.code()})", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(btnSave, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                setUploading(false)
            }
        }
    }

    private fun setUploading(uploading: Boolean) {
        layoutUploading.visibility = if (uploading) View.VISIBLE else View.GONE
        btnSave.isEnabled          = !uploading
        btnCancel.isEnabled        = !uploading
    }
}
