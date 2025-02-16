package co.iostream.apps.code_pocket.components

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat.getColor
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import co.iostream.apps.code_pocket.utils.BarcodeType


fun getQRCodeBitmap(value: String): Bitmap? {
    val size = 768 //pixels
    val hints = hashMapOf<EncodeHintType, Any>().also {
        // Make the QR code buffer border narrower
        it[EncodeHintType.MARGIN] = 1
        it[EncodeHintType.CHARACTER_SET] = "UTF-8"
        it[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
    }

    var bits: BitMatrix? = null

    for (i in 1..40) {
        try {
            hints[EncodeHintType.QR_VERSION] = i
            bits = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, size, size, hints)
            break
        } catch (_: Exception) {
        }
    }

    if (bits == null) return null

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
        for (x in 0 until size) {
            for (y in 0 until size) {
                val color = if (bits[x, y]) Color.Black else Color.White
                it.setPixel(x, y, color.toArgb())
            }
        }
    }

    return bitmap
}

// You must handle invalid data yourself
@Composable
fun getBarCodeBitMap(URL: String) {
    if (BarcodeType.QR_CODE.isValueValid(URL)) {
        Barcode(
            modifier = Modifier
                .width(150.dp)
                .height(150.dp),
            resolutionFactor = 10, // Optionally, increase the resolution of the generated image
            type = BarcodeType.QR_CODE, // pick the type of barcode you want to render
            value = URL // The textual representation of this code
        )
    }
    if (!BarcodeType.CODE_128.isValueValid(URL)) {
        Text("this is not code 128 compatible")
    }
}

@Composable
fun QRCode(value: String) {
    val bitmap = getQRCodeBitmap(value)

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
        )
    }
}