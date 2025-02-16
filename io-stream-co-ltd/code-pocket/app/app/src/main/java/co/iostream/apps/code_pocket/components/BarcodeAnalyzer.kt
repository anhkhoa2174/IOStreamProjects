package co.iostream.apps.code_pocket.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyser(
    val callback: (barcodes: List<Barcode>) -> Unit
) : ImageAnalysis.Analyzer {
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val options =
                BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
            val scanner = BarcodeScanning.getClient(options)
            val imageToProcess =
                InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

            scanner.process(imageToProcess).addOnSuccessListener { barcodes ->
                if (barcodes.size > 0) {
                    callback(barcodes)
                }
            }.addOnFailureListener {}.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}