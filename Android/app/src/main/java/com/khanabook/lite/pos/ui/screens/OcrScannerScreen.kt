package com.khanabook.lite.pos.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.khanabook.lite.pos.domain.util.TextRecognitionHelper
import com.khanabook.lite.pos.ui.theme.DarkBrown1
import com.khanabook.lite.pos.ui.theme.PrimaryGold
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScannerScreen(
    onTextScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Expert Fix: Single instance of Helper tied to Screen lifecycle
    val textRecognitionHelper = remember { TextRecognitionHelper() }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            textRecognitionHelper.processUri(
                context, 
                it, 
                onSuccess = { text, _ -> if (text.isNotBlank()) onTextScanned(text) },
                onFailure = { /* Handle Gallery Error */ }
            )
        }
    }

    // Expert Fix: Proper Resource Cleanup
    DisposableEffect(context) {
        onDispose {
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (e: Exception) {
                Log.e("OCR_SCREEN", "Error unbinding camera", e)
            }
            textRecognitionHelper.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Menu Item", color = PrimaryGold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
                    }
                },
                actions = {
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = PrimaryGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBrown1)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Black)) {
            if (hasCameraPermission) {
                CameraPreview(
                    onTextScanned = onTextScanned,
                    helper = textRecognitionHelper
                )
            } else {
                PermissionDeniedContent(
                    onRequestPermission = { launcher.launch(Manifest.permission.CAMERA) }
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    onTextScanned: (String) -> Unit,
    helper: TextRecognitionHelper
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Expert Fix: Atomic flags to throttle processing and stop scanning after success
    var isProcessing = remember { false }
    var isFinished = remember { false }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            // Throttling: Skip frames if already busy or successful
                            if (isProcessing || isFinished) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                isProcessing = true
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                
                                helper.processImage(image,
                                    onSuccess = { text, _ ->
                                        if (text.isNotBlank() && !isFinished) {
                                            isFinished = true // Lock scanning once successful
                                            onTextScanned(text)
                                        }
                                        isProcessing = false
                                        imageProxy.close()
                                    },
                                    onFailure = {
                                        isProcessing = false
                                        imageProxy.close()
                                    }
                                )
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CAMERA", "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
fun PermissionDeniedContent(onRequestPermission: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Camera access is required to scan menus.",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
        ) {
            Text("Grant Permission", color = DarkBrown1)
        }
        TextButton(onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }) {
            Text("Open Settings", color = PrimaryGold)
        }
    }
}
