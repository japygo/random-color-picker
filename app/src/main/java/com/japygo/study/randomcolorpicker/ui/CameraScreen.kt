package com.japygo.study.randomcolorpicker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onColorCaptured: (Color) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
            if (!granted) {
                Toast.makeText(context, "Camera permission needed", Toast.LENGTH_SHORT).show()
                onBack()
            }
        },
    )

    LaunchedEffect(key1 = true) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        var detectedColor by remember { mutableStateOf(Color.White) }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener(
                        {
                            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                val color = extractCenterColor(imageProxy)
                                imageProxy.close()

                                // Update UI on main thread
                                // Not strictly necessary as Compose handles state updates, but good practice
                                detectedColor = color
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis,
                                )
                            } catch (exc: Exception) {
                                Log.e("CameraScreen", "Use case binding failed", exc)
                            }

                        },
                        ContextCompat.getMainExecutor(ctx),
                    )

                    previewView
                },
                modifier = Modifier.fillMaxSize(),
            )

            // Reticle (Center Indicator)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
                    .border(2.dp, Color.White, CircleShape),
            )

            // Detected Color Preview & Capture
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp),
            ) {
                Button(
                    onClick = { onColorCaptured(detectedColor) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = detectedColor,
                    ),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                ) {
                    // Empty content, just color
                }
            }

            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
        }
    }
}

private fun extractCenterColor(image: ImageProxy): Color {
    val planes = image.planes
    val buffer = planes[0].buffer
    val width = image.width
    val height = image.height
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride

    // Center coordinates
    val centerX = width / 2
    val centerY = height / 2

    // Index of the center pixel
    val offset = (centerY * rowStride) + (centerX * pixelStride)

    // Read RGB (Note: ImageAnalysis usually gives YUV)
    // However, simplest way often is to get Bitmap directly or convert.
    // BUT simplest performant way for just center pixel:
    // Actually, ImageAnalysis default format is YUV_420_888.
    // Converting YUV to RGB effectively is complex manually.
    // Let's use Bitmap conversion if available or standard YUV conversion.

    // Warning: Direct buffer access for YUV is tricky.
    // Let's optimize: Just getting one pixel is easier than converting whole image.

    // Y plane
    val y = (buffer.get(offset).toInt() and 0xFF)

    // U and V planes
    val uPlane = planes[1]
    val vPlane = planes[2]

    // UV stride
    val uvRowStride = uPlane.rowStride
    val uvPixelStride = uPlane.pixelStride

    val uvX = centerX / 2
    val uvY = centerY / 2

    val uvOffset = (uvY * uvRowStride) + (uvX * uvPixelStride)

    val u = (uPlane.buffer.get(uvOffset).toInt() and 0xFF) - 128
    val v = (vPlane.buffer.get(uvOffset).toInt() and 0xFF) - 128

    // YUV to RGB
    val r = (y + (1.370705 * v)).toInt().coerceIn(0, 255)
    val g = (y - (0.337633 * u) - (0.698001 * v)).toInt().coerceIn(0, 255)
    val b = (y + (1.732446 * u)).toInt().coerceIn(0, 255)

    return Color(r, g, b)
}
