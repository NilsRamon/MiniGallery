package de.nilsramon.minigallery.main

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewModelScope
import com.google.android.material.slider.Slider
import de.nilsramon.minigallery.R
import de.nilsramon.minigallery.databinding.ActivityMainBinding
import de.nilsramon.minigallery.util.getBitmapFromView
import de.nilsramon.minigallery.util.saveToGallery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MainActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE = 100
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)
        binding.blurSlider.bind(mainViewModel.radius, mainViewModel::setRadius)
        binding.saturationSlider.bind(mainViewModel.saturation, mainViewModel::setSaturation)
        binding.selected.setImageURI(mainViewModel.mUri)

        mainViewModel.blurEffectFlow.onEach {
            binding.selected.setRenderEffect(it)
        }.launchIn(mainViewModel.viewModelScope)

        mainViewModel.saturationEffectFlow.onEach {
            binding.selected.setRenderEffect(it)
        }.launchIn(mainViewModel.viewModelScope)

        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                binding.blurSlider.value = 0f
                binding.saturationSlider.value = 0f
                binding.selected.setRenderEffect(null)
                mainViewModel.setImageUri(uri)
                binding.selected.setImageURI(mainViewModel.mUri)
            }

        binding.openGallery.setOnClickListener {
            getContent.launch("image/*")
        }


        binding.savePicture.setOnClickListener {
            getBitmapFromView(binding.surfaceContainer, this) { bitmap ->
                saveToGallery(applicationContext, bitmap, "MiniGalleryEdits") {
                    if (it) {
                        Toast.makeText(this,
                            "Image saved to Gallery", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this,
                            "Image could not be saved.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun Slider.bind(flow: Flow<Float>, setter: (Float) -> Unit) {
        flow.onEach { newValue -> value = newValue }
            .launchIn(mainViewModel.viewModelScope)
        addOnChangeListener { _, value, _ ->
            setter(value)
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission Granted, enjoy!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this,
                    "Storage Permission Denied, App experience might be affected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}