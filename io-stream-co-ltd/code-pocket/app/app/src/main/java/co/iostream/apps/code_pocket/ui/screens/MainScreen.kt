package co.iostream.apps.code_pocket.ui.screens

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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import co.iostream.apps.code_pocket.R
import co.iostream.apps.code_pocket.components.FabIcon
import co.iostream.apps.code_pocket.components.HeaderBar
import co.iostream.apps.code_pocket.components.MultiFabItem
import co.iostream.apps.code_pocket.components.MultiFloatingActionButton
import co.iostream.apps.code_pocket.components.QRCode
import co.iostream.apps.code_pocket.components.getQRCodeBitmap
import co.iostream.apps.code_pocket.customDialog
import co.iostream.apps.code_pocket.data.entities.CodeItemEntity
import co.iostream.apps.code_pocket.domain.models.CodeItemLabel
import co.iostream.apps.code_pocket.domain.models.CodeItemZone
import co.iostream.apps.code_pocket.domain.models.CodeLabel
import co.iostream.apps.code_pocket.navigation.BottomNavigator
import co.iostream.apps.code_pocket.navigation.MeNavigatorGraph
import co.iostream.apps.code_pocket.navigation.OthersNavigatorGraph
import co.iostream.apps.code_pocket.navigation.RootNavigatorGraph
import co.iostream.apps.code_pocket.navigation.TrashBottomNavigatorGraph
import co.iostream.apps.code_pocket.ui.theme.Foreground2
import co.iostream.apps.code_pocket.utils.DateUtils
import co.iostream.apps.code_pocket.utils.FileUtils
import co.iostream.apps.code_pocket.viewmodels.MainViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule


@Composable
fun MainScreen(
    navController: NavHostController,
    zone: CodeItemZone,
    mainViewModel: MainViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val isScanning by mainViewModel.isScanning.collectAsState()
    val currentEditingItem by mainViewModel.currentEditingItem.collectAsState()



    LaunchedEffect(zone) {
        mainViewModel.setCurrentZone(zone)
        coroutineScope.launch {
            mainViewModel.getByZone(zone)
        }
    }

    var code by rememberSaveable { mutableStateOf("") }


    Scaffold(
        topBar = { TopBar(navController, mainViewModel, zone) },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                ListContainerBox(navController, mainViewModel, zone)
            }
        },
        floatingActionButton = {
            if (zone != CodeItemZone.PROMOTION) FloatButton(
                navController, mainViewModel, zone, onScan = { mainViewModel.enableScanning(true) }
            )
        },
        bottomBar = { BottomNavigator(navController) },
    )
}

@Composable
private fun TopBar(navController: NavHostController, mainViewModel: MainViewModel, zone: CodeItemZone) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isMenuOpened by remember { mutableStateOf(false) }

    HeaderBar(title = stringResource(id = R.string.app_name), right = {
        IconButton(
            onClick = { isMenuOpened = !isMenuOpened },
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_menu_24),
                contentDescription = null,
            )
        }

        DropdownMenu(expanded = isMenuOpened, onDismissRequest = { isMenuOpened = false }) {
            DropdownMenuItem(onClick = { openUsage(context)}, text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_question_mark_24),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(text = stringResource(id = R.string.how_to_use))
                }
            })
            DropdownMenuItem(onClick = { navController.navigate(RootNavigatorGraph.SETTINGS) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(text = stringResource(id = R.string.settings))
                    }
                }
            )
            DropdownMenuItem(onClick = {
                if (!customDialog.getState()) {
                    customDialog.title =
                        context.getString(R.string.remove_list)
                    customDialog.subTitle =
                        context.getString(R.string.remove_list_subtitle)
                    customDialog.onConfirmCallback = {
                        coroutineScope.launch {
                            mainViewModel.temporaryDeleteAll()
                        }
                    }
                }

                customDialog.enable(!customDialog.getState())
            },
                enabled = !mainViewModel.isItemEmpty(),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_24),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 10.dp)
                        )

                        Text(text = stringResource(id = R.string.remove_all_files))
                    }
                }
            )
            if (zone == CodeItemZone.ME){
                DropdownMenuItem(onClick = {
                    navController.navigate(TrashBottomNavigatorGraph.ME)
                },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_restore_from_trash_24),
                                contentDescription = null,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Text(text = stringResource(id = R.string.my_codes_trash))
                        }
                    }
                )
            }
            else {
                DropdownMenuItem(onClick = {
                    navController.navigate(TrashBottomNavigatorGraph.OTHERS)
                },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_restore_from_trash_24),
                                contentDescription = null,
                                modifier = Modifier.padding(end = 10.dp)
                            )

                            Text(text = stringResource(id = R.string.other_codes_trash))
                        }
                    }
                )
            }
        }
    })
}

@Composable
private fun ListTopBar(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val searchKeyword by mainViewModel.searchKeyword.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            value = searchKeyword,
            onValueChange = {
                mainViewModel.setSearchKeyword(it)

            },
            placeholder = {
                Text(text = context.getString(R.string.search), fontSize = 16.sp)
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "") },
            trailingIcon = {
                IconButton(onClick = {
                    mainViewModel.setSearchKeyword(String())
                    //mainViewModel.applyFilter()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            },
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListContainerBox(
    navController: NavHostController, mainViewModel: MainViewModel, zone: CodeItemZone
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    var isItemDetailOpened by remember { mutableStateOf(false) }
    val items by mainViewModel.items.collectAsState()

    val keyword by mainViewModel.searchKeyword.collectAsState()

    val selectedItem by mainViewModel.selectedItem.collectAsState()

    val labelMenu = mapOf(
        CodeLabel(CodeItemLabel.RED, stringResource(id = R.string.red_description)) to Color.Red,
        CodeLabel(CodeItemLabel.BLUE, stringResource(id = R.string.blue_description)) to Color.Blue,
        CodeLabel(
            CodeItemLabel.GREEN,
            stringResource(id = R.string.green_description)
        ) to Color.Green,
        CodeLabel(
            CodeItemLabel.VIOLET,
            stringResource(id = R.string.purple_description)
        ) to Color.Cyan,
        CodeLabel(
            CodeItemLabel.WHITE,
            stringResource(id = R.string.white_description)
        ) to Color.White,
        CodeLabel(
            CodeItemLabel.YELLOW,
            stringResource(id = R.string.yellow_description)
        ) to Color.Yellow,
    )

    val shareLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    fun shareQRCode(item: CodeItemEntity) {
        val bitmap = getQRCodeBitmap(item.code) ?: return

        val uri = FileUtils.saveCacheImage(context, bitmap)

        val intent = Intent(Intent.ACTION_SEND)
        intent.setDataAndType(uri, "image/png")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareLauncher.launch(Intent.createChooser(intent, item.description))
    }

    fun copyToClipboard(text: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("copied_text", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "Copied", Toast.LENGTH_LONG).show()
    }

    if (items.isEmpty() || items.all { it.isDeleted }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(R.drawable.hint_icon),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Foreground2
            )

            Text(
                text = stringResource(id = R.string.add_code_hint),
                fontSize = 14.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
                color = Foreground2
            )

            if (zone != CodeItemZone.PROMOTION) {
                Button(onClick = {
                    val url =
                        if (zone == CodeItemZone.OTHERS) OthersNavigatorGraph.ADD
                        else MeNavigatorGraph.ADD

                    navController.navigate(url)
                }) {
                    Text(
                        text = stringResource(id = R.string.add_a_code),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 15.dp)
        ) {
            Row{
                ListTopBar(mainViewModel)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredItems = items.filter { !it.isDeleted && it.description.contains(keyword, ignoreCase = true) }
                itemsIndexed(
                    //items = items,
                    items = filteredItems,
                    key = { _, item -> item.id.toString() },
                    itemContent = { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = if (index == items.size - 1) 96.dp else 0.dp)
                                .combinedClickable(onClick = {
                                    mainViewModel.setSelectedItem(item)
                                    isItemDetailOpened = true
                                })
                        ) {
                            Row(
                                modifier = Modifier
                                    .height(80.dp)
                                    .clip(shape = RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {}) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_qr_code_24),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Column {
                                    Row {
                                        Column(
                                            modifier = Modifier
                                                .weight(1F)
                                                .height(72.dp),
                                            horizontalAlignment = Alignment.Start,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.description,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                )

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    var isLabelChoseOpened by remember {
                                                        mutableStateOf(
                                                            false
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            isLabelChoseOpened = !isLabelChoseOpened
                                                        }
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(
                                                                id = R.drawable.baseline_label_24
                                                            ), contentDescription = null,
                                                            tint = item.getColorByLabel()
                                                        )
                                                    }

                                                    DropdownMenu(
                                                        expanded = isLabelChoseOpened,
                                                        onDismissRequest = {
                                                            isLabelChoseOpened = false
                                                        },
                                                        modifier = Modifier.padding(vertical = 0.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .height(165.dp)
                                                                .animateContentSize()
                                                                .verticalScroll(rememberScrollState())
                                                        ) {
                                                            labelMenu.forEach { (label, color) ->
                                                                DropdownMenuItem(
                                                                    onClick = {
                                                                        coroutineScope.launch {
                                                                            item.label = label.label
                                                                            mainViewModel.updateItem(
                                                                                item
                                                                            )
                                                                        }
                                                                        isLabelChoseOpened = false
                                                                    },
                                                                    text = {
                                                                        Row {
                                                                            Icon(
                                                                                painter = painterResource(
                                                                                    id = R.drawable.baseline_label_24
                                                                                ),
                                                                                contentDescription = null,
                                                                                tint = color,
                                                                                modifier = Modifier.padding(
                                                                                    end = 10.dp
                                                                                )
                                                                            )
                                                                            Text(
                                                                                text = label.colorName
                                                                            )
                                                                        }
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                if (item.code.isNotEmpty()) {
                                                    // if not Empty, create var temp to save item to show in "Code location"
                                                    // Condition: if code.length > 10 -> Show 5 char of the begin, and length 10, otherwise show all
                                                    val temp = if (item.code.length > 5) {
                                                        item.code.slice(0..2) + "•••••"
                                                    } else if (item.code.length in 3..5) {
                                                        item.code.slice(0..1) + "•••••"
                                                    } else if (item.code.length == 2) {
                                                        item.code[0] + "•••••"
                                                    } else {
                                                        "•••••"
                                                    }
                                                    Text(
                                                        text = temp,
                                                        fontSize = 12.sp,
                                                        color = Color(0xffaaaaaa)
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.baseline_edit_calendar_24),
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .padding(end = 5.dp)
                                                    )
                                                    Text(
                                                        text = DateUtils.format(
                                                            item.createdAt, "dd-MM-YYYY hh:mm"
                                                        ),
                                                        fontSize = 12.sp,
                                                        color = Color(0xffaaaaaa)
                                                    )
                                                }
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.height(72.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            var isItemContextMenuOpened by remember {
                                                mutableStateOf(
                                                    false
                                                )
                                            }

                                            IconButton(onClick = {
                                                isItemContextMenuOpened = !isItemContextMenuOpened
                                            }) {
                                                Icon(
                                                    painter = painterResource(R.drawable.baseline_more_vert_24),
                                                    contentDescription = null,
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = isItemContextMenuOpened,
                                                onDismissRequest = {
                                                    isItemContextMenuOpened = false
                                                },
                                            ) {
                                                DropdownMenuItem(onClick = {
                                                    shareQRCode(item)
                                                }, text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.baseline_share_24),
                                                            contentDescription = null,
                                                            modifier = Modifier.padding(end = 10.dp)
                                                        )
                                                        Text(text = stringResource(id = R.string.share))
                                                    }
                                                })
                                                DropdownMenuItem(
                                                    onClick = { copyToClipboard(item.code) },
                                                    text = {

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                painter = painterResource(id = R.drawable.baseline_content_copy_24),
                                                                contentDescription = null,
                                                                modifier = Modifier.padding(end = 10.dp)
                                                            )
                                                            Text(text = stringResource(id = R.string.copy))
                                                        }

                                                    })
                                                DropdownMenuItem(onClick = {
                                                    val url =
                                                        if (zone == CodeItemZone.OTHERS) OthersNavigatorGraph.EDIT + "/${item.id}"
                                                        else MeNavigatorGraph.EDIT + "/${item.id}"

                                                    navController.navigate(url)
                                                }, text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.edit),
                                                            contentDescription = null,
                                                            modifier = Modifier.padding(end = 10.dp)
                                                        )
                                                        Text(text = stringResource(id = R.string.edit))
                                                    }
                                                })

                                                DropdownMenuItem(onClick = {
                                                    if (!customDialog.getState()) {
                                                        customDialog.title =
                                                            context.getString(R.string.remove_from_list_title)
                                                        customDialog.subTitle =
                                                            context.getString(R.string.remove_from_list_subtitle)
                                                        customDialog.onConfirmCallback = {
                                                            coroutineScope.launch {
                                                                mainViewModel.temporaryDeleteOne(item)
                                                            }
                                                        }
                                                    }

                                                    customDialog.enable(!customDialog.getState())
                                                }, text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.baseline_delete_24),
                                                            contentDescription = null,
                                                            modifier = Modifier.padding(end = 10.dp)
                                                        )
                                                        Text(text = stringResource(id = R.string.remove))
                                                    }
                                                })
                                                // set outdate
                                                var showDialog by remember { mutableStateOf(false) }
                                                var selectedItem by remember {
                                                    mutableStateOf<CodeItemEntity?>(
                                                        null
                                                    )
                                                }

                                                DropdownMenuItem(onClick = {
                                                    selectedItem =
                                                        item // `item` là CodeItemEntity bạn muốn đặt lịch xóa
                                                    showDialog = true
                                                }, text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.baseline_auto_delete_24),
                                                            contentDescription = null,
                                                            modifier = Modifier.padding(end = 10.dp)
                                                        )
                                                        Text(text = stringResource(id = R.string.set_outdate))
                                                    }
                                                })

                                                if (showDialog && selectedItem != null) {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        AlertDialog(
                                                            onDismissRequest = {
                                                                showDialog = false
                                                            },
                                                            title = { Text(text = stringResource(id = R.string.set_outdate)) },
                                                            text = {
                                                                selectedItem?.let { item ->
                                                                    ScheduleDeletionScreen(
                                                                        item = item,
                                                                        onScheduleDeletion = { item, delay ->
                                                                            CoroutineScope(
                                                                                Dispatchers.IO).launch {
                                                                                delay(delay)
                                                                                mainViewModel.temporaryDeleteOne(item)
                                                                            }
                                                                            showDialog = false
                                                                        }
                                                                    )
                                                                }
                                                            },
                                                            confirmButton = {
                                                                Button(onClick = {
                                                                    showDialog = false
                                                                }) {
                                                                    Text(text = "Close")
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .wrapContentHeight()
                                                                .fillMaxWidth()
                                                                .height(490.dp)
                                                        )
                                                    }
                                                }

                                                DropdownMenuItem(onClick = {
                                                    coroutineScope.launch {
                                                        mainViewModel.moveQRCode(item)
                                                    }
                                                    selectedItem =
                                                        item // `item` là CodeItemEntity bạn muốn đặt lịch xóa
                                                    showDialog = true
                                                }, text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.baseline_drive_file_move_24),
                                                            contentDescription = null,
                                                            modifier = Modifier.padding(end = 10.dp)
                                                        )
                                                        Text(text = stringResource(id = R.string.move))
                                                    }
                                                })

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
            }

            if (isItemDetailOpened && selectedItem != null) {
                var isOptionOpen by remember { mutableStateOf(false) }

                AlertDialog(
                    modifier = Modifier.fillMaxWidth(0.84f),
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false
                    ),
                    shape = MaterialTheme.shapes.small,
                    onDismissRequest = {
                        isItemDetailOpened = false
                        mainViewModel.setSelectedItem(null)
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                selectedItem?.description?.let {
                                    Text(
                                        text = it,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(25.dp))

                            // YourCode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        selectedItem?.code?.let {
                                            if (it.isNotEmpty()) QRCode(it)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Adding a Spacer of height 20dp
                            //                    Spacer(modifier = Modifier.height(20.dp))
                            var isCodeValueShowed by remember { mutableStateOf(true) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clickable { isCodeValueShowed = !isCodeValueShowed },
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Check codeValue is Empty

                                selectedItem?.code?.let {
                                    if (it.isNotEmpty()) {
                                        // if not Empty, create var temp to save item to show in "Code location"
                                        // Condition: if code.length > 10 -> Show 5 char of the begin, and length 10, otherwise show all
                                        val temp = if (!isCodeValueShowed) {
                                            if (it.length > 5) {
                                                it.slice(0..2) + "•••••"
                                            } else if (it.length in 3..5) {
                                                it.slice(0..1) + "•••••"
                                            } else if (it.length == 2) {
                                                it[0] + "•••••"
                                            } else {
                                                "•••••"
                                            }
                                        } else {
                                            it
                                        }

                                        Text(
                                            text = temp, fontSize = 15.sp, color = Color(0xffaaaaaa)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(15.dp))

                            selectedItem?.createdAt?.let {
                                Text(
                                    fontSize = 12.sp,
                                    color = Foreground2,
                                    text = DateUtils.format(it, "dd-MM-YYYY hh:mm")
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { isOptionOpen = !isOptionOpen },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_menu_24),
                                    contentDescription = null,
                                )
                            }

                            DropdownMenu(
                                expanded = isOptionOpen,
                                onDismissRequest = { isOptionOpen = false }) {
                                DropdownMenuItem(onClick = { }, text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_qr_code_24),
                                            contentDescription = null,
                                        )
                                        Text(text = stringResource(id = R.string.QRcode))
                                    }
                                })
                                DropdownMenuItem(onClick = {
                                    navController.navigate(
                                        RootNavigatorGraph.SETTINGS
                                    )
                                },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_qr_code_24),
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 10.dp)
                                            )
                                            Text(text = stringResource(id = R.string.Barcoder))
                                        }
                                    })
                            }

                            TextButton(onClick = {
                                selectedItem?.let { shareQRCode(it) }
                            }) {
                                Text(
                                    text = stringResource(id = R.string.share),
                                    color = Foreground2
                                )
                            }
                        }
                    },


                    )
            }
        }
    }

}

@Composable
private fun FloatButton(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    zone: CodeItemZone,
    onScan: () -> Unit = {}
) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp

    var url = if (zone == CodeItemZone.OTHERS) OthersNavigatorGraph.ADD
    else MeNavigatorGraph.ADD

    val context = LocalContext.current

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
                            MediaStore.Images
                                .Media.getBitmap(context.contentResolver, it)
                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, it)
                            ImageDecoder.decodeBitmap(source)
                        }

                        var imageToProcess = InputImage.fromBitmap(bitmap, 90)

                        val options =
                            BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                .build()
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
                                            navController.navigate(url)
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

    Column {
        MultiFloatingActionButton(
            fabIcon = FabIcon(
                iconRes = R.drawable.baseline_add_24,
                iconResAfterRotate = R.drawable.baseline_add_24,
                iconRotate = 135f
            ),

            itemsMultiFab = listOf(
                MultiFabItem(
                    tag = "ScanFromImage",
                    icon = R.drawable.baseline_image_24,
                    label = "Image",
                ),
                MultiFabItem(
                    tag = "ScanFromCamera",
                    icon = R.drawable.baseline_qr_code_scanner_24,
                    label = "Scan"
                ),
                MultiFabItem(
                    tag = "AddNew",
                    icon = R.drawable.baseline_qr_code_24,
                    label = "New"
                )
            ),
            onFabItemClicked = {
                when (it.tag) {
                    "ScanFromImage" -> {
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
                    }

                    "ScanFromCamera" -> {
                        url =
                            if (zone == CodeItemZone.OTHERS) OthersNavigatorGraph.SCAN else MeNavigatorGraph.SCAN
                        navController.navigate(url)
                    }

                    "AddNew" -> {
                        navController.navigate(url)
                    }
                }
            },
            fabTitle = "MultiFloatActionButton", showFabTitle = false,
        )
        Spacer(
            modifier = Modifier.height((80 + screenWidth.value * 5 / 100).dp)
        )
    }
}

@Composable
fun ScheduleDeletionScreen(
    item: CodeItemEntity,
    onScheduleDeletion: (CodeItemEntity, Long) -> Unit
) {
    var deleteDate by remember { mutableStateOf("") }
    var deleteTime by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

            Text(
                text = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            TextField(
                value = deleteDate,
                onValueChange = { deleteDate = it },
                label = { Text("Enter Delete Date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Nhập thời gian xóa
            TextField(
                value = deleteTime,
                onValueChange = { deleteTime = it },
                label = { Text("Enter Delete Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút xác nhận
            Button(
                onClick = {
                    val deleteDateTime = "$deleteDate $deleteTime"
                    val delay = calculateDelay(deleteDateTime)
                    if (delay > 0) {
                        println("Delay: $delay ms")
                        onScheduleDeletion(item, delay)
                        deleteDate = ""
                        deleteTime = ""
                        errorMessage = ""
                    } else {
                        errorMessage =
                            "Invalid date or time format. Please use yyyy-MM-dd for date and HH:mm for time."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Schedule Deletion")
            }
        }
    }
}

fun calculateDelay(dateTime: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val deleteTime = format.parse(dateTime)
        deleteTime?.time?.minus(System.currentTimeMillis()) ?: -1
    } catch (e: ParseException) {
        -1
    }
}

fun openUsage(context: Context) {
    val url = "https://www.facebook.com/khang.quoc.902"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}




