package co.iostream.apps.code_pocket.components

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.common.Barcode
import co.iostream.apps.code_pocket.R
import co.iostream.apps.code_pocket.ui.utils.MenuButton
import co.iostream.apps.code_pocket.ui.utils.TransparentClipLayout
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    onCodeChanged: (String) -> Unit = {},
    setDisableScanning: () -> Unit = {},
){
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(5f)
    ) {
        var isCamSwitched by remember { mutableStateOf(true)}
        var isEnableTorch by remember { mutableStateOf(false)}
        var lensFacing = if (isCamSwitched) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT

        Box(){
            BarcodeScanner(onScannedSuccessCallback = { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val result = barcodes.firstOrNull()
                    if (result != null) {
                        result.displayValue?.let { onCodeChanged(it) }
                    }
                }
                setDisableScanning() },
                lensFacing = lensFacing,
                isEnableTorch = isEnableTorch
            )

            TransparentClipLayout(
                modifier = Modifier.fillMaxSize(),
                width = 300.dp,
                height = 300.dp,
                offsetY = 250.dp
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(text = "Place a barcode inside the viewfinder to scan it")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MenuButton(id = R.drawable.baseline_image_24,
                        onClick = {})
                    MenuButton(id = R.drawable.baseline_highlight_24,
                        onClick = {
                            isEnableTorch = !isEnableTorch
                        })
                    MenuButton(id = R.drawable.baseline_cameraswitch_24,
                        onClick = {
                            isCamSwitched = !isCamSwitched
                        })
                    MenuButton(id = R.drawable.baseline_batch_prediction_24)
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun BarcodeScanner(
    onScannedSuccessCallback: (( List<Barcode>) -> Unit)?,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    isEnableTorch: Boolean = false
){
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    var code by remember { mutableStateOf("") }

    // Get Context
    var context = LocalContext.current
    val previewView = PreviewView(context).also {
        it.scaleType = PreviewView.ScaleType.FILL_CENTER
    }

    // Set up Camera
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

    // Set up ImageCapture
    val imageCapture = ImageCapture.Builder()
        .build()

    val imageAnalyzer = ImageAnalysis.Builder().build().also {
        it.setAnalyzer(cameraExecutor, BarcodeAnalyser { barcodes ->
            if (onScannedSuccessCallback != null) {
                onScannedSuccessCallback(barcodes)
            }
        })
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.size(width = screenWidth, height = screenHeight),
                update = { previewView ->
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    var cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                    cameraProviderFuture.addListener({
                        try {
                            // Unbind use cases before rebinding
                            cameraProvider.unbindAll()

                            // Bind use cases to camera
                            var camera = cameraProvider.bindToLifecycle(
                                context as ComponentActivity,
                                cameraSelector,
                                preview,
                                imageCapture,
                                imageAnalyzer
                            )
                            camera.cameraControl.enableTorch(isEnableTorch)
                        } catch (exc: Exception) {
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            )
        }
    }
}