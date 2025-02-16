package co.iostream.apps.code_pocket.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import co.iostream.apps.code_pocket.R
import co.iostream.apps.code_pocket.components.BarcodeScanner
import co.iostream.apps.code_pocket.components.QRCode
import co.iostream.apps.code_pocket.components.getQRCodeBitmap
import co.iostream.apps.code_pocket.data.entities.CodeItemEntity
import co.iostream.apps.code_pocket.domain.models.CodeItem
import co.iostream.apps.code_pocket.domain.models.CodeItemLabel
import co.iostream.apps.code_pocket.domain.models.CodeItemZone
import co.iostream.apps.code_pocket.domain.models.CodeLabel
import co.iostream.apps.code_pocket.navigation.MeNavigatorGraph
import co.iostream.apps.code_pocket.navigation.OthersNavigatorGraph
import co.iostream.apps.code_pocket.ui.utils.MenuButton
import co.iostream.apps.code_pocket.ui.utils.TransparentClipLayout
import co.iostream.apps.code_pocket.utils.FileUtils
import co.iostream.apps.code_pocket.viewmodels.MainViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddScreen(
    navController: NavHostController, zone: CodeItemZone, mainViewModel: MainViewModel
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val isScanning by mainViewModel.isScanning.collectAsState()
    val scannedResult by mainViewModel.scannedResult.collectAsState()

    val currentCode by mainViewModel.currentCode.collectAsState()
    val url = if (zone == CodeItemZone.OTHERS) OthersNavigatorGraph.ADD
    else MeNavigatorGraph.ADD

    var bitmap: Bitmap
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { it ->
        if (it.resultCode == Activity.RESULT_OK) {
            val data: Intent? = it.data

            if (data != null) {
                if (data.data != null) {
                    val uri: Uri? = data.data

                    uri?.let {
                        bitmap = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, it)
                            ImageDecoder.decodeBitmap(source)
                        }

                        var imageToProcess = InputImage.fromBitmap(bitmap, 90)

                        val options = BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
                        val scanner = BarcodeScanning.getClient(options)

                        scanner.process(imageToProcess).addOnSuccessListener { barcodes ->
                            if (barcodes.size > 0) {
                                if (barcodes.isNotEmpty()) {
                                    val result = barcodes.firstOrNull()
                                    if (result != null) {
                                        result.displayValue?.let { it ->
                                            mainViewModel.setCurrentCode(
                                                it
                                            )
                                            mainViewModel.enableScanning(false)
                                            navController.navigate(url)

                                            mainViewModel.setCurrentCode("")
                                        }
                                    }
                                }
                            }
                        }.addOnFailureListener { e ->
                            e.printStackTrace()
                        }.addOnCompleteListener {
                            // finish
                        }
                    }
                }
            }
        }
    }

    var description by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var labeltag by rememberSaveable { mutableStateOf(CodeItemLabel.WHITE) }
    var labelConvert = mapOf(
        CodeItemLabel.RED to Color.Red,
        CodeItemLabel.BLUE to Color.Blue,
        CodeItemLabel.GREEN to Color.Green,
        CodeItemLabel.VIOLET to Color.Cyan,
        CodeItemLabel.WHITE to Color.White,
        CodeItemLabel.YELLOW to Color.Yellow
    )


    val labelMenu = mapOf(
        CodeLabel(CodeItemLabel.RED, stringResource(id = R.string.red_description)) to Color.Red,
        CodeLabel(CodeItemLabel.BLUE, stringResource(id = R.string.blue_description)) to Color.Blue,
        CodeLabel(
            CodeItemLabel.GREEN, stringResource(id = R.string.green_description)
        ) to Color.Green,
        CodeLabel(
            CodeItemLabel.VIOLET, stringResource(id = R.string.purple_description)
        ) to Color.Cyan,
        CodeLabel(
            CodeItemLabel.WHITE, stringResource(id = R.string.white_description)
        ) to Color.White,
        CodeLabel(
            CodeItemLabel.YELLOW, stringResource(id = R.string.yellow_description)
        ) to Color.Yellow,
    )

    val shareLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    fun shareQRCode(code: String) {
        val bitmap = getQRCodeBitmap(code) ?: return
        val uri = FileUtils.saveCacheImage(context, bitmap)
        val intent = Intent(Intent.ACTION_SEND)
        intent.setDataAndType(uri, "image/png")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareLauncher.launch(Intent.createChooser(intent, "Share"))
    }

    fun copyToClipboard(text: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("copied_text", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "Copied", Toast.LENGTH_LONG).show()
    }

    BackHandler(isScanning) {
        mainViewModel.enableScanning(false)
    }

    if (isScanning) {
        var isCamSwitched by remember { mutableStateOf(false) }
        var isEnableTorch by remember { mutableStateOf(false) }
        var isBatchScan by remember { mutableStateOf(false) }
        var lensFacing =
            if (isCamSwitched) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(5f)
        ) {
            Box() {
                BarcodeScanner(
                    onScannedSuccessCallback = { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val result = barcodes.firstOrNull()
                            if (result != null) {
                                result.displayValue?.let { mainViewModel.setCurrentCode(it) }
                            }
                        }
                        mainViewModel.enableScanning(isBatchScan)
                    }, lensFacing = lensFacing, isEnableTorch = isEnableTorch
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_qr_code_scanner_24),
                            contentDescription = "Scan Icon",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Scan QR Code", style = TextStyle(
                                fontSize = 20.sp
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        MenuButton(id = R.drawable.baseline_image_24, onClick = {
                            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                            intent.putExtra(
                                DocumentsContract.EXTRA_INITIAL_URI,
                                Environment.getExternalStorageDirectory().toUri()
                            )
                            intent.type = "image/*"
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            intent.addCategory(Intent.CATEGORY_OPENABLE)
                            intent = Intent.createChooser(intent, "Add barcode from image")
                            filePickerLauncher.launch(intent)
                        })
                        MenuButton(id = R.drawable.baseline_highlight_24,
                            selected = isEnableTorch,
                            onClick = {
                                isEnableTorch = !isEnableTorch
                            })
                        MenuButton(id = R.drawable.baseline_cameraswitch_24, onClick = {
                            isCamSwitched = !isCamSwitched
                        })
                        MenuButton(id = R.drawable.baseline_batch_prediction_24,
                            selected = isBatchScan,
                            onClick = {
                                isBatchScan = !isBatchScan
                            })
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    if (isBatchScan) {
                        Surface(
                            modifier = Modifier
                                .width(300.dp)
                                .padding(1.dp),
                            tonalElevation = 40.dp,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_qr_code_24),
                                    contentDescription = null,
                                )
                                Text(text = currentCode)
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.width(120.dp), horizontalArrangement = Arrangement.Start) {
                    TextButton(
                        onClick = {
                            mainViewModel.setCurrentCode("")
                            navController.popBackStack()
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(stringResource(id = R.string.cancel))
                    }
                }

                Text(text = stringResource(id = R.string.add_title))

                Row(modifier = Modifier.width(120.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    mainViewModel.addOne(
                                        CodeItem(
                                            currentCode, description, labeltag, zone,false
                                        )
                                    )
                                }

                                mainViewModel.setCurrentCode("")
                                navController.popBackStack()
                            }
                        },
                    ) {
                        Text(stringResource(id = R.string.add), color = Color.White)
                    }
                }
            }
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            OutlinedTextField(
                                value = description,
                                onValueChange = { newText -> description = newText },
                                label = { Text(text = stringResource(id = R.string.title)) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(text = "Profile...", fontSize = 14.sp) },
                                shape = RoundedCornerShape(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column {
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Row(
//                                ) {
//                                    Row(
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        horizontalArrangement = Arrangement.End
//                                    ) {
//                                        var isEditLabelChoseOpened by remember { mutableStateOf(false) }
//
//                                        IconButton(
//                                            onClick = { isEditLabelChoseOpened = !isEditLabelChoseOpened }
//                                        ) {
//                                            Icon(
//                                                painter = painterResource(id = R.drawable.baseline_label_24),
//                                                contentDescription = null,
//                                                tint = labelConvert.getValue(labeltag)
//                                            )
//                                        }
//
//                                        DropdownMenu(
//                                            expanded = isEditLabelChoseOpened,
//                                            onDismissRequest = {
//                                                isEditLabelChoseOpened = false
//                                            },
//                                            modifier = Modifier.padding(vertical = 0.dp)
//                                        ) {
//                                            Column(
//                                                modifier = Modifier
//                                                    .height(165.dp)
//                                                    .animateContentSize()
//                                                    .verticalScroll(rememberScrollState())
//                                            ) {
//                                                labelMenu.forEach { (label, color) ->
                            //                                                    DropdownMenuItem(
//                                                        onClick = {
//                                                            labeltag = label.label
//                                                            isEditLabelChoseOpened = false
//                                                        },
//                                                        text = {
//                                                            Row {
//                                                                Icon(
//                                                                    painter = painterResource(id = R.drawable.baseline_label_24),
//                                                                    contentDescription = null,
//                                                                    tint = color,
//                                                                    modifier = Modifier.padding(end = 10.dp)
//                                                                )
//                                                                Text(text = label.colorName)
//                                                            }
//                                                        }
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }

                            OutlinedTextField(
                                value = currentCode,
                                onValueChange = { mainViewModel.setCurrentCode(it) },
                                label = { Text(text = stringResource(id = R.string.code)) },
                                trailingIcon = {
                                    IconButton(modifier = Modifier,
                                        onClick = { mainViewModel.enableScanning(true) }) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_qr_code_scanner_24),
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(text = "https://...", fontSize = 14.sp) },
                                shape = RoundedCornerShape(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(25.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (currentCode != "") Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (currentCode.isNotEmpty()) {
                                    QRCode(currentCode)
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp, 48.dp)
                                ) {
                                    IconButton(onClick = { copyToClipboard(code) }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_copy_all_24),
                                            contentDescription = "Copy"
                                        )
                                    }
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp, 48.dp)
                                ) {
                                    IconButton(onClick = { shareQRCode(currentCode) }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_share_24),
                                            contentDescription = "Share"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = { },
        bottomBar = { },
    )
}