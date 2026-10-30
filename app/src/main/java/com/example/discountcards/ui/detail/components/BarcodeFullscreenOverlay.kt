package com.example.discountcards.ui.detail.components

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun BarcodeFullscreenOverlay(
    bitmap: Bitmap,
    onClose: () -> Unit,
) {
    val context = LocalContext.current

    // Force screen brightness to 100% while the overlay is showing,
    // then restore the override on dispose.
    DisposableEffect(Unit) {
        (context as? Activity)?.window?.let { w ->
            val params = w.attributes
            params.screenBrightness = 1.0f
            w.attributes = params
        }
        onDispose {
            (context as? Activity)?.window?.let { w ->
                val params = w.attributes
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                w.attributes = params
            }
        }
    }

    // Always rotate 90° when the device is in portrait orientation so the
    // barcode fills the screen height. In landscape we keep the original
    // orientation so the barcode can fill the width.
    val displayMetrics = context.resources.displayMetrics
    val isPortrait = displayMetrics.heightPixels >= displayMetrics.widthPixels
    val displayBitmap = if (isPortrait) rotate90Clockwise(bitmap) else bitmap

    // 4% of each screen dimension as white padding so the barcode does not
    // visually blend into the screen edges.
    val paddingH = (displayMetrics.widthPixels * 0.04f).toInt()
    val paddingV = (displayMetrics.heightPixels * 0.04f).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(onClick = onClose)
    ) {
        Image(
            bitmap = displayBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .padding(
                    start = paddingH.dp,
                    end = paddingH.dp,
                    top = paddingV.dp,
                    bottom = paddingV.dp
                )
        )
    }
}

private fun rotate90Clockwise(src: Bitmap): Bitmap {
    val matrix = Matrix().apply {
        postRotate(90f)
        postTranslate(-src.height.toFloat(), 0f)
    }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
}
