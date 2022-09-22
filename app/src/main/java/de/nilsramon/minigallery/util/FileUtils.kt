package de.nilsramon.minigallery.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.annotation.RequiresApi
import java.io.OutputStream

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("Recycle")
fun saveToGallery(
    context: Context,
    bitmap: Bitmap,
    albumName: String,
    callback: (Boolean) -> Unit
) {
    val filename = "${System.currentTimeMillis()}.png"
    val write: (OutputStream) -> Boolean = {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$albumName")
    }
    try {
        context.contentResolver.let {
            it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                it.openOutputStream(uri)?.let(write)
            }
        }
        callback(true)
    } catch (e: Exception) {
        callback(false)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
    activity.window?.let { window ->
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        view.getLocationInWindow(locationOfViewInWindow)
        try {
            PixelCopy.request(
                window,
                Rect(
                    locationOfViewInWindow[0],
                    locationOfViewInWindow[1],
                    locationOfViewInWindow[0] + view.width,
                    locationOfViewInWindow[1] + view.height
                ), bitmap, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    } else {
                        Log.d("getScreenShotFromView", "Error while Copying!")
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}