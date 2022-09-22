package de.nilsramon.minigallery.main

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

@RequiresApi(Build.VERSION_CODES.S)
class MainViewModel : ViewModel() {

    private val _radius = MutableStateFlow(0f)
    val radius: StateFlow<Float> = _radius
    fun setRadius(newValue: Float) {
        _radius.value = newValue
    }

    private val _saturation = MutableStateFlow(0f)
    val saturation: StateFlow<Float> = _saturation
    fun setSaturation(newValue: Float) {
        _saturation.value = newValue
    }

    var mUri: Uri? = null
    fun setImageUri(uri: Uri?) {
        mUri = uri
    }

    val blurEffectFlow = combine(_radius, _radius) { x, y ->
        if (_saturation.value != 0f && _radius.value != 0f) {
            RenderEffect.createChainEffect(RenderEffect.createBlurEffect(
                x,
                y,
                Shader.TileMode.MIRROR
            ),
                RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(
                    ColorMatrix().apply {
                        setSaturation(_saturation.value)
                    }
                )))
        } else if (_radius.value != 0f) {
            RenderEffect.createBlurEffect(x, y, Shader.TileMode.MIRROR)
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val saturationEffectFlow = combine(_saturation, _saturation) { _, _ ->
        if (_radius.value != 0f && _saturation.value != 0f) {
            RenderEffect.createChainEffect(RenderEffect.createBlurEffect(
                _radius.value,
                _radius.value,
                Shader.TileMode.MIRROR
            ),
                RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(
                    ColorMatrix().apply {
                        setSaturation(_saturation.value)
                    }
                )))
        } else if (_saturation.value != 0f) {
            RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(
                ColorMatrix().apply {
                    setSaturation(_saturation.value)
                }
            ))
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

}