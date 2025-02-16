package vn.iostream.apps.file_locker_x.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.DocumentsContract
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.iostream.apps.core.iofile.FileSystemItem
import vn.iostream.apps.core.iofile.FileUtils
import vn.iostream.apps.file_locker_x.R
import vn.iostream.apps.file_locker_x.configs.AppTypes
import vn.iostream.apps.file_locker_x.configs.Constants
import vn.iostream.apps.file_locker_x.customDialog
import vn.iostream.apps.file_locker_x.models.FileItem
import vn.iostream.apps.file_locker_x.navigation.ContentBottomBar
import vn.iostream.apps.file_locker_x.navigation.MiscNavigatorGraph
import vn.iostream.apps.file_locker_x.navigation.ProcessingNavigatorGraph
import vn.iostream.apps.file_locker_x.ui.composables.DialogItem
import vn.iostream.apps.file_locker_x.ui.composables.DropdownMenuItem
import vn.iostream.apps.file_locker_x.ui.composables.FloatButton
import vn.iostream.apps.file_locker_x.ui.composables.HintView
import vn.iostream.apps.file_locker_x.ui.composables.InputPasswordBox
import vn.iostream.apps.file_locker_x.ui.composables.MenuItem
import vn.iostream.apps.file_locker_x.ui.composables.OnLifecycleEvent
import vn.iostream.apps.file_locker_x.ui.composables.TopBar
import vn.iostream.apps.file_locker_x.ui.theme.Foreground2
import vn.iostream.apps.file_locker_x.ui.theme.Theme
import vn.iostream.apps.file_locker_x.ui.theme.ThemeDarker
import vn.iostream.apps.file_locker_x.ui.theme.seed
import vn.iostream.apps.file_locker_x.utils.CryptographyUtils.Companion.decryptBuffer
import vn.iostream.apps.file_locker_x.utils.DateUtils
import vn.iostream.apps.file_locker_x.viewmodels.FileManagerViewModel
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.pathString

@Composable
fun FileManagerScreen(
    navController: NavHostController, fileManagerViewModel: FileManagerViewModel,
) {
    val currentItem = fileManagerViewModel.currentItem.collectAsState()
    val status = fileManagerViewModel.status.collectAsState()
    val addFilesVisible = fileManagerViewModel.addFilesVisible.collectAsState()
    val isSelecting = fileManagerViewModel.isSelecting.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_RESUME -> {
                fileManagerViewModel.updateStatus(FileManagerViewModel.StatusType.Loaded)
                coroutineScope.launch {
                    fileManagerViewModel.initialize()
                }
            }

            else -> {}
        }
    }

    BackHandler(
        currentItem.value?.isEnabled == true || isSelecting.value || fileManagerViewModel.any(
            status.value,
            FileManagerViewModel.StatusType.Processing,
            FileManagerViewModel.StatusType.Loading
        )
    ) {
        if (isSelecting.value) {
            fileManagerViewModel.setSelectMode(false)
        }
    }

    Scaffold(
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                ContentTopBar(
                    navController = navController, fileManagerViewModel = fileManagerViewModel
                )
                ListContainerBox(navController, fileManagerViewModel)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent
            ) {
                ContentBottomBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (addFilesVisible.value) {
                FloatButton(
                    fileManagerViewModel = fileManagerViewModel,
                    modifier = Modifier.offset(y = 56.dp)
                )
            }
        },
    )
}

@Composable
private fun SearchBox(fileManagerViewModel: FileManagerViewModel) {
    val context = LocalContext.current
    val searchKeyword by fileManagerViewModel.searchKeyword.collectAsState()

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
                .height(52.dp),
            shape = RoundedCornerShape(50),
            value = searchKeyword,
            onValueChange = {
                fileManagerViewModel.setSearchKeyword(it)
                fileManagerViewModel.applyFilter()
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
                    fileManagerViewModel.setSearchKeyword(String())
                    fileManagerViewModel.applyFilter()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            },
        )
    }
}

@Composable
private fun ContentTopBar(
    navController: NavHostController, fileManagerViewModel: FileManagerViewModel
) {
    val status = fileManagerViewModel.status.collectAsState()

    TopBar(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer), title = {
        Row {
            SearchBox(fileManagerViewModel)
        }
    }, right = {
        var showSetting by remember { mutableStateOf(false) }
        val processing = fileManagerViewModel.any(
            status.value,
            FileManagerViewModel.StatusType.Loading,
            FileManagerViewModel.StatusType.Processing
        )

        Row(
            modifier = Modifier.size(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                modifier = Modifier.size(24.dp), onClick = {
                    if (!processing) {
                        showSetting = !showSetting
                    }
                }, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.more_horiz),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        DropdownMenu(expanded = showSetting, onDismissRequest = { showSetting = false }) {
            val menuItems = listOf(
                MenuItem(title = stringResource(id = R.string.batch_locker),
                    iconResId = R.drawable.baseline_lock_24,
                    onClick = {
                        navController.navigate(ProcessingNavigatorGraph.BATCH + "/" + FileItem.FeatureType.Encode.name)
                    }),
                MenuItem(title = stringResource(id = R.string.batch_unlocker),
                    iconResId = R.drawable.baseline_lock_open_24,
                    onClick = {
                        navController.navigate(ProcessingNavigatorGraph.BATCH + "/" + FileItem.FeatureType.Decode.name)
                    }),
            )

            menuItems.forEach { item ->
                DropdownMenuItem(item = item)
            }
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SortBox(fileManagerViewModel: FileManagerViewModel) {
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState()
    val showBottomSheet = remember { mutableStateOf(false) }

    val sortOptions = AppTypes.SORT_TYPES.toList()

    val sortOrder by fileManagerViewModel.sortOrder.collectAsState()

    AppTypes.SORT_TYPES[sortOrder]?.let {
        TextButton(onClick = { showBottomSheet.value = true }) {
            Text(text = "${context.getString(R.string.sort_by)}: ${context.getString(it)}")
        }
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet.value = false }, sheetState = sheetState
        ) {
            LazyColumn {
                items(sortOptions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = {
                                fileManagerViewModel.setSortOrder(it.first)
                                fileManagerViewModel.applyFilter()
                            }), verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = it.first == sortOrder, onClick = {})
                        Text(context.getString(it.second))
                    }
                }
            }
        }
    }
}

@Composable
private fun ListTopBar(viewModel: FileManagerViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val items = viewModel.items.collectAsState()
        val status = viewModel.status.collectAsState()
        val selectMode = viewModel.isSelecting.collectAsState()
        val selectedItemsCount = items.value.filter { it.isSelected }

        Crossfade(targetState = selectMode.value, label = "") {
            when (it) {
                true -> {
                    var showSetting by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.setSelectMode(false) }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Theme
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedItemsCount.size} ${stringResource(id = if (selectedItemsCount.size == 1) R.string.item else R.string.items)}",
                                style = TextStyle(color = Theme)
                            )
                            IconButton(onClick = {
                                if (!customDialog.getState()) {
                                    customDialog.title =
                                        context.getString(R.string.remove_selected_items_from_list_title)
                                    customDialog.subTitle =
                                        context.getString(R.string.remove_selected_items_from_list_subtitle)
                                    customDialog.onConfirmCallback = {
                                        coroutineScope.launch {
                                            viewModel.removeMany(FileManagerViewModel.RemoveMode.Selected)
                                        }
                                    }
                                }

                                customDialog.enable(!customDialog.getState())
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_playlist_remove_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = Theme
                                )
                            }
                            IconButton(
                                onClick = { showSetting = !showSetting },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = Theme
                                )
                            }

                            DropdownMenu(
                                expanded = showSetting,
                                onDismissRequest = { showSetting = false }) {
                                val menuItems = listOf(
                                    MenuItem(
                                        title = stringResource(id = R.string.move_to_documents),
                                        iconResId = R.drawable.baseline_content_cut_24,
                                        dialogItem = DialogItem(context = context,
                                            titleResIdInt = R.string.move_to_documents_title,
                                            subtitleResIdInt = R.string.move_to_documents_subtitle,
                                            coroutineScope = coroutineScope,
                                            onConfirmCallback = {
                                                viewModel.moveSelectedToDocuments(false)
                                            }),
                                    ), MenuItem(
                                        title = stringResource(id = R.string.copy_to_documents),
                                        iconResId = R.drawable.baseline_content_copy_24,
                                        dialogItem = DialogItem(context = context,
                                            titleResIdInt = R.string.copy_to_documents_title,
                                            subtitleResIdInt = R.string.copy_to_documents_subtitle,
                                            coroutineScope = coroutineScope,
                                            onConfirmCallback = {
                                                viewModel.moveSelectedToDocuments(true)
                                            }),
                                    ), MenuItem(
                                        title = stringResource(id = R.string.delete_permanently),
                                        iconResId = R.drawable.baseline_delete_24,
                                        dialogItem = DialogItem(context = context,
                                            titleResIdInt = R.string.delete_permanently_title,
                                            subtitleResIdInt = R.string.delete_permanently_subtitle,
                                            coroutineScope = coroutineScope,
                                            onConfirmCallback = { viewModel.deleteSelectedPermanently() }),
                                    )
                                )

                                menuItems.forEach { item ->
                                    DropdownMenuItem(item = item)
                                }
                            }
                        }
                    }
                }

                false -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SortBox(fileManagerViewModel = viewModel)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${items.value.count()} ${stringResource(id = if (items.value.count() == 1) R.string.item else R.string.items)}")

                            IconButton(onClick = {
                                if (!customDialog.getState()) {
                                    customDialog.title =
                                        context.getString(R.string.remove_all_of_items_title)
                                    customDialog.subTitle =
                                        context.getString(R.string.remove_all_of_items_subtitle)
                                    customDialog.onConfirmCallback = {
                                        coroutineScope.launch {
                                            viewModel.removeMany(FileManagerViewModel.RemoveMode.All)
                                        }
                                    }
                                }

                                customDialog.enable(!customDialog.getState())
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_playlist_remove_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (!viewModel.any(
                                            status.value,
                                            FileManagerViewModel.StatusType.Loading,
                                            FileManagerViewModel.StatusType.Processing
                                        )
                                    ) LocalContentColor.current else Color.Transparent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListContainerBox(
    navController: NavHostController,
    fileManagerViewModel: FileManagerViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isSelecting = fileManagerViewModel.isSelecting.collectAsState()
    val items = fileManagerViewModel.items.collectAsState()
    val currentItem = fileManagerViewModel.currentItem.collectAsState()

    val status = fileManagerViewModel.status.collectAsState()

    val passwordEnabled = fileManagerViewModel.passwordEnabled.collectAsState()
    val keepOriginalEnabled = fileManagerViewModel.keepOriginalEnabled.collectAsState()
    val overwriteExistingEnabled = fileManagerViewModel.overwriteExistingEnabled.collectAsState()

    val password = fileManagerViewModel.password.collectAsState()
    val keepOriginal = fileManagerViewModel.keepOriginal.collectAsState()
    val overwriteExisting = fileManagerViewModel.overwriteExisting.collectAsState()

    val renameDialogVisible = fileManagerViewModel.renameDialogVisible.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }
    var isDialogOpened by remember { mutableStateOf(false) }

    val shareLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val openLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    if (items.value.isEmpty()) {
        val hintIcon = painterResource(R.drawable.baseline_upload_file)
        val title = stringResource(id = R.string.no_items_here)
        val subtitle = stringResource(id = R.string.add_files_and_folders)

        HintView(
            hintIconPainter = hintIcon, title = title, subTitle = subtitle
        )
    } else {
        Column {
            ListTopBar(fileManagerViewModel)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                itemsIndexed(
                    items = items.value,
                ) { index, item ->
                    val message =
                        if (!item.inputInfo.exists()) stringResource(id = R.string.this_file_no_longer_exists) else if (item.itemType == FileItem.FileItemType.EncodedFile) stringResource(
                            id = R.string.this_is_a_locked_file
                        ) else if (item.itemType == FileItem.FileItemType.EncodedFolder) stringResource(
                            id = R.string.this_is_a_locked_folder
                        )
                        else String()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (index == items.value.size - 1) 96.dp else 0.dp)
                            .background(if (isSelecting.value && item.isSelected) seed else Color.Transparent)
                            .combinedClickable(onClick = {
                                if (!item.isEnabled || fileManagerViewModel.any(
                                        status.value,
                                        FileManagerViewModel.StatusType.Processing,
                                        FileManagerViewModel.StatusType.Loading
                                    )
                                ) return@combinedClickable

                                if (isSelecting.value) {
                                    item.isSelected = !item.isSelected
                                    fileManagerViewModel.updateOne(item)
                                } else {
                                    fileManagerViewModel.updateOne(item)

                                    val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                    if (!item.inputInfo.exists() || item.itemType != FileItem.FileItemType.Normal) {
                                        toast.setGravity(Gravity.CENTER, 0, 0)
                                        toast.show()
                                    } else {
                                        val itemUri = FileUtils.getFileProviderUri(
                                            context, item.inputInfo.pathString
                                        )

                                        if (item.inputInfo.isRegularFile()) {
                                            val itemType =
                                                FileUtils.getTypeByExtension(item.inputInfo.extension)

                                            if (itemType == FileUtils.Type.Image) {
                                                navController.navigate(
                                                    "${MiscNavigatorGraph.MEDIAVIEW}/${
                                                        URLEncoder.encode(
                                                            item.inputInfo.pathString,
                                                            StandardCharsets.UTF_8.toString()
                                                        )
                                                    }"
                                                )
                                            } else {
                                                val intent = Intent(Intent.ACTION_VIEW, itemUri)
                                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                openLauncher.launch(
                                                    Intent.createChooser(
                                                        intent,
                                                        context.getString(R.string.open_file)
                                                    )
                                                )
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                val contentUri = FileUtils.getDocumentUriFromPath(
                                                    context, item.inputInfo.pathString
                                                )
                                                if (contentUri != null) {
                                                    val folderUri = try {
                                                        DocumentsContract.buildDocumentUriUsingTree(
                                                            contentUri,
                                                            DocumentsContract.getTreeDocumentId(
                                                                contentUri
                                                            )
                                                        )
                                                    } catch (e: Exception) {
                                                        contentUri
                                                    }
                                                    val intent = Intent(Intent.ACTION_VIEW)
                                                    intent.setDataAndType(
                                                        folderUri,
                                                        DocumentsContract.Document.MIME_TYPE_DIR
                                                    )
                                                    intent.putExtra(
                                                        DocumentsContract.EXTRA_INITIAL_URI,
                                                        folderUri
                                                    )
                                                    openLauncher.launch(
                                                        Intent.createChooser(
                                                            intent,
                                                            context.getString(R.string.open_folder)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }, onLongClick = {
                                fileManagerViewModel.setSelectMode(true)
                                item.isSelected = true
                                fileManagerViewModel.updateOne(item)
                            }), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            IconButton(modifier = Modifier.height(32.dp), onClick = {
                                if (!item.isEnabled || fileManagerViewModel.any(
                                        status.value,
                                        FileManagerViewModel.StatusType.Processing,
                                        FileManagerViewModel.StatusType.Loading
                                    )
                                ) return@IconButton

                                fileManagerViewModel.selectItem(item)
                            }) {
                                Icon(
                                    painter = if (isSelecting.value && item.isSelected) painterResource(
                                        R.drawable.baseline_check_circle_24
                                    ) else if (item.inputInfo.isRegularFile() && item.itemType != FileItem.FileItemType.EncodedFolder) painterResource(
                                        R.drawable.baseline_insert_drive_file_24
                                    ) else painterResource(R.drawable.baseline_folder_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = if (item.isSelected && isSelecting.value) Theme else LocalContentColor.current
                                )
                            }

                            if (!item.isSelected && item.itemType != FileItem.FileItemType.Normal) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .offset(24.dp, 20.dp),
                                    tint = Theme
                                )
                            }
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
                                    Text(
                                        text = item.inputInfo.name,
                                        style = TextStyle(
                                            color = Foreground2,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (item.inputInfo.exists()) TextDecoration.None else TextDecoration.LineThrough
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )

                                    Row {
                                        if (item.inputInfo.isRegularFile() && FileUtils.getExtension(
                                                item.inputInfo.extension
                                            ).isNotEmpty()
                                        ) {
                                            Text(
                                                text = ".${FileUtils.getExtension(item.inputInfo.name)}, ",
                                                style = TextStyle(color = Foreground2)
                                            )
                                        }
                                        if (item.inputInfo.isRegularFile()) {
                                            Text(
                                                text = "${
                                                    FileUtils.getReadableFileSize(
                                                        item.inputInfo.fileSize()
                                                    )
                                                }, ", style = TextStyle(
                                                    color = Foreground2, fontSize = 12.sp
                                                )
                                            )
                                        }
                                        item.creationTime?.let {
                                            Text(
                                                text = DateUtils.formatRelative(
                                                    context, it
                                                ), style = TextStyle(
                                                    color = Foreground2, fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = FileUtils.getBriefOfPath(item.inputInfo.parent.pathString),
                                        style = TextStyle(color = Foreground2, fontSize = 12.sp),
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .height(7.dp)
                                            .offset(0.dp, 4.dp)
                                    ) {

                                        if (status.value == FileManagerViewModel.StatusType.Processing && item.status.any(
                                                FileSystemItem.S.Processing
                                            )
                                        ) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(color = Theme)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.height(72.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    var isMenuOpened by remember { mutableStateOf(false) }

                                    if (item.isEnabled && !isSelecting.value) {
                                        if (item.itemType != FileItem.FileItemType.Normal) {
                                            OnClickButton(item, fileManagerViewModel) {
                                                coroutineScope.launch {
                                                    isDialogOpened = true
                                                    item.isEnabled = true
                                                    fileManagerViewModel.updateOne(item)
                                                    fileManagerViewModel.updateFileItem(item)
                                                    fileManagerViewModel.updateStatus(
                                                        FileManagerViewModel.StatusType.Loaded
                                                    )
                                                }
                                            }
                                        }
                                        IconButton(onClick = {
                                            coroutineScope.launch {
                                                isDialogOpened = true
                                                item.isEnabled = true
                                                fileManagerViewModel.updateOne(item)
                                                fileManagerViewModel.updateFileItem(item)
                                                fileManagerViewModel.updateStatus(
                                                    FileManagerViewModel.StatusType.Loaded
                                                )
                                            }
                                        }) {
                                            Icon(
                                                painterResource(if (item.itemType == FileItem.FileItemType.Normal) R.drawable.baseline_lock_24 else R.drawable.baseline_lock_open_24),
                                                contentDescription = null,
                                            )
                                        }
                                        IconButton(onClick = { isMenuOpened = !isMenuOpened }) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = null,
                                            )
                                        }
                                    }

                                    DropdownMenu(expanded = isMenuOpened,
                                        onDismissRequest = { isMenuOpened = false }) {

                                        val optionalMenuItems = listOf(
                                            MenuItem(
                                                title = stringResource(id = R.string.share),
                                                iconResId = R.drawable.baseline_share_24,
                                                onClick = {
                                                    val itemUri = FileUtils.getFileProviderUri(
                                                        context, item.inputInfo.pathString
                                                    )

                                                    if (itemUri != null) {
                                                        val intent = Intent(Intent.ACTION_SEND)
                                                        intent.type = "*/*"
                                                        intent.putExtra(
                                                            Intent.EXTRA_STREAM, itemUri
                                                        )
                                                        shareLauncher.launch(
                                                            Intent.createChooser(
                                                                intent,
                                                                context.getString(R.string.share_file)
                                                            )
                                                        )
                                                    }
                                                },
                                            )
                                        )
                                        val menuItems = listOf(
                                            MenuItem(
                                                title = stringResource(id = R.string.rename),
                                                iconResId = R.drawable.baseline_drive_file_rename_outline_24,
                                                dialogItem = DialogItem(context = context,
                                                    titleResIdInt = R.string.rename,
                                                    subtitleResIdInt = R.string.rename_subtitle,
                                                    coroutineScope = coroutineScope,
                                                    onConfirmCallback = {
                                                        fileManagerViewModel.setCurrentItem(item)
                                                        fileManagerViewModel.setRenameDialogVisible(
                                                            true
                                                        )
                                                    }),
                                            ),

                                            MenuItem(
                                                title = stringResource(id = R.string.move_to_documents),
                                                iconResId = R.drawable.baseline_content_cut_24,
                                                dialogItem = DialogItem(context = context,
                                                    titleResIdInt = R.string.move_to_documents_title,
                                                    subtitleResIdInt = R.string.move_to_documents_subtitle,
                                                    coroutineScope = coroutineScope,
                                                    onConfirmCallback = {
                                                        fileManagerViewModel.moveToDocuments(
                                                            item, false
                                                        )
                                                    }),
                                            ),

                                            MenuItem(
                                                title = stringResource(id = R.string.copy_to_documents),
                                                iconResId = R.drawable.baseline_content_copy_24,
                                                dialogItem = DialogItem(context = context,
                                                    titleResIdInt = R.string.copy_to_documents_title,
                                                    subtitleResIdInt = R.string.copy_to_documents_subtitle,
                                                    coroutineScope = coroutineScope,
                                                    onConfirmCallback = {
                                                        fileManagerViewModel.moveToDocuments(
                                                            item, true
                                                        )
                                                    }),
                                            ),
                                            MenuItem(
                                                title = stringResource(id = R.string.remove_from_list),
                                                iconResId = R.drawable.baseline_playlist_remove_24,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        fileManagerViewModel.removeOne(item)
                                                        isMenuOpened = false
                                                    }
                                                },
                                            ),
                                            MenuItem(
                                                title = stringResource(id = R.string.delete_permanently),
                                                iconResId = R.drawable.baseline_delete_24,
                                                dialogItem = DialogItem(context = context,
                                                    titleResIdInt = R.string.delete_permanently_title,
                                                    subtitleResIdInt = R.string.delete_permanently_subtitle,
                                                    coroutineScope = coroutineScope,
                                                    onConfirmCallback = {
                                                        fileManagerViewModel.deletePermanently(
                                                            item
                                                        )
                                                    },
                                                )
                                            ),
                                        )

                                        if (item.inputInfo.isRegularFile()) {
                                            optionalMenuItems.forEach { item ->
                                                DropdownMenuItem(item = item)
                                            }
                                        }

                                        menuItems.forEach { item ->
                                            DropdownMenuItem(item = item)
                                        }
                                    }
                                }
                            }
                            Divider(thickness = (1).dp)
                        }
                    }
                }
            }
        }
    }

    val renameValue = fileManagerViewModel.renameValue.collectAsState()
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val width = 60

    if (renameDialogVisible.value && currentItem.value != null) {
        // AlertDialog.....
        val focusRequester = remember { FocusRequester() } // Tạo FocusRequester

        AlertDialog(
            onDismissRequest = {
                fileManagerViewModel.setCurrentItem(null)
                fileManagerViewModel.setRenameDialogVisible(false)
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (currentItem.value != null) {
                                val success = fileManagerViewModel.renameFile(
                                    currentItem.value!!, renameValue.value, !keepOriginal.value
                                )
                                if (success) {
                                    fileManagerViewModel.updateOne(currentItem.value!!)
                                    fileManagerViewModel.setRenameDialogVisible(value = false)
                                }
                            }
                        }
                    }
                ) {
                    Text(text = "OK")
                }
            },

            text = {
                Column {
                    val oldFileName = currentItem.value?.inputInfo?.name ?: ""
                    val fileNameWithoutExtension = oldFileName.substringBeforeLast(".")
                    val fileExtension = oldFileName.substringAfterLast(".", "")
                    var textFieldValue by remember { mutableStateOf(TextFieldValue(fileNameWithoutExtension)) }

                    LaunchedEffect(renameDialogVisible.value, currentItem.value) {
                        textFieldValue = TextFieldValue(
                            text = "$fileNameWithoutExtension.$fileExtension",
                            selection = TextRange(0, fileNameWithoutExtension.length)
                        )

                        focusRequester.requestFocus()
                    }

                    TextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            fileManagerViewModel.setRenameValue(newValue.text.substringBeforeLast(".")
                                    + if (fileExtension.isNotEmpty()) ".$fileExtension" else "")
                        },
                        label = {
                            Text(
                                stringResource(R.string.new_file_name), color = ThemeDarker
                            )
                        },
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.new_file_name)) },
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .defaultMinSize(
                                minWidth = 0.dp, minHeight = 0.dp
                            )
                            .widthIn(min = 0.dp, max = (screenWidth * width / 100))
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentItem.value!!.isEnabled && !isSelecting.value) {
                            if (currentItem.value!!.itemType != FileItem.FileItemType.Normal) {
                                Checkbox(
                                    checked = keepOriginal.value,
                                    onCheckedChange = { fileManagerViewModel.setKeepOriginal(it) },
                                    enabled = keepOriginalEnabled.value,

                                )
                                Text(
                                    text = stringResource(id = R.string.keep_original_name),
                                    modifier = Modifier.clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        onClick = { fileManagerViewModel.setKeepOriginal(!keepOriginal.value) },
                                    )
                                )
                            }
                        }
                    }
                }
            }
        )
    }


    /*if (isDialogOpened && (currentItem.value != null || mainViewModel.any(
            status.value, MainViewModel.StatusType.PROCESSING_ONE, MainViewModel.StatusType.LOADING
        ))
    ) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(0.84f),
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = true
            ),
            shape = MaterialTheme.shapes.small,
            onDismissRequest = {
                isDialogOpened = false
                if (currentItem.value != null) {

                    currentItem.value!!.isEnabled = false
                    mainViewModel.updateOne(currentItem.value!!)
                    mainViewModel.updateFileItem(currentItem.value)
                    isDialogOpened = !(currentItem.value?.isEnabled ?: false)

                    mainViewModel.setPassword("")
                    mainViewModel.setKeepOriginal(false)
                    mainViewModel.setOverwriteExisting(false)

                    if (!mainViewModel.any(
                            status.value,
                            MainViewModel.StatusType.PROCESSING_ONE,
                            MainViewModel.StatusType.LOADING
                        )
                    ) mainViewModel.updateFileItem(null)
                }
            },
            title = {
                Column {
                    Text(
                        text = currentItem.value?.inputInfo?.parent?.pathString ?: "",
                        style = TextStyle(color = Foreground2, fontSize = 12.sp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    Row {
                        if (currentItem.value?.inputInfo?.isRegularFile() == true) {
                            Text(
                                text = "${
                                    currentItem.value?.inputInfo?.fileSize()?.let {
                                        FileUtils.getReadableFileSize(it)
                                    }
                                }",
                                style = TextStyle(color = Foreground2, fontSize = 12.sp),
                                maxLines = 1,
                            )
                        }
                        Text(
                            text = DateUtils.formatRelative(
                                context,
                                currentItem.value?.inputInfo?.getLastModifiedTime()?.toMillis()
                                    ?: 0L
                            ), style = TextStyle(color = Foreground2, fontSize = 12.sp)
                        )
                    }
                }
            },
            text = {
                Column {
                    InputPasswordBox(
                        value = mainViewModel.password.collectAsState().value,

                        enabled = passwordEnabled.value, 70,
                        onChangeCallback = { mainViewModel.setPassword(it) },
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = keepOriginal.value,
                            onCheckedChange = { mainViewModel.setKeepOriginal(it) },
                            enabled = keepOriginalEnabled.value,
                        )
                        Text(
                            text = stringResource(id = R.string.keep_original_files),
                            modifier = Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { mainViewModel.setKeepOriginal(!keepOriginal.value) },
                            )
                        )
                    }
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                    ) {
//                        Checkbox(
//                            checked = overwriteExisting.value,
//                            onCheckedChange = { mainViewModel.setOverwriteExisting(it) },
//                            enabled = overwriteExistingEnabled.value,
//                        )
//                        Text(
//                            text = stringResource(id = R.string.overwrite_existing),
//                            modifier = Modifier.clickable(
//                                interactionSource = interactionSource,
//                                indication = null,
//                                onClick = { mainViewModel.setOverwriteExisting(!overwriteExisting.value) },
//                            )
//                        )
//                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    currentItem.value?.isEnabled?.let {
                        Button(
                            shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Theme
                            ), onClick = {
                                coroutineScope.launch {
                                    val m =
                                        if (currentItem.value?.itemType == FileItem.FileItemType.Normal) R.string.lock_success else R.string.unlock_success
                                    val result = mainViewModel.processFile()
                                    if (result) {
                                        mainViewModel.setPassword("")
                                        mainViewModel.setKeepOriginal(false)
                                        mainViewModel.setOverwriteExisting(false)
                                        isDialogOpened = false
                                        Toast.makeText(
                                            context, context.getString(m), Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }, enabled = it
                        ) {
                            Text(
                                text = if (currentItem.value?.itemType == FileItem.FileItemType.Normal) stringResource(
                                    id = R.string.lock
                                )
                                else stringResource(id = R.string.unlock), color = Color.White
                            )
                        }
                    }
                }
            },
        )
    }*/

    if (isDialogOpened && (currentItem.value != null || fileManagerViewModel.any(
            status.value,
            FileManagerViewModel.StatusType.Processing,
            FileManagerViewModel.StatusType.Loading
        ))
    ) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(0.84f),
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = true // Cho phép dismiss khi nhấn ra ngoài
            ),
            shape = MaterialTheme.shapes.small,
            onDismissRequest = {
                isDialogOpened = false // Đóng dialog khi nhấn ra ngoài
                if (currentItem.value != null) {
                    currentItem.value!!.isEnabled = false
                    fileManagerViewModel.updateOne(currentItem.value!!)
                    fileManagerViewModel.updateFileItem(currentItem.value)
                    isDialogOpened = !(currentItem.value?.isEnabled ?: false)

                    fileManagerViewModel.setPassword("")
                    fileManagerViewModel.setKeepOriginal(false)
                    fileManagerViewModel.setOverwriteExisting(false)

                    if (!fileManagerViewModel.any(
                            status.value,
                            FileManagerViewModel.StatusType.Processing,
                            FileManagerViewModel.StatusType.Loading
                        )
                    ) fileManagerViewModel.updateFileItem(null)
                }
            },
            title = { DialogTitle(currentItem.value) },
            text = {
                DialogContent(
                    fileManagerViewModel,
                    passwordEnabled.value,
                    keepOriginal.value,
                    keepOriginalEnabled.value,
                    interactionSource
                )
            },
            confirmButton = {
                currentItem.value?.let {
                    DialogConfirmButton(
                        it,
                        fileManagerViewModel,
                        isDialogOpened,
                        { isDialogOpened = it },
                        coroutineScope,
                        LocalContext.current
                    )
                }
            },
        )
    }

    if (isDialogOpened) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(0.84f),
            properties = DialogProperties(
                usePlatformDefaultWidth = false, dismissOnClickOutside = true
            ),
            shape = MaterialTheme.shapes.small,
            onDismissRequest = {
                isDialogOpened = true
                if (currentItem.value != null) {
                    currentItem.value!!.isEnabled = true
                    fileManagerViewModel.updateOne(currentItem.value!!)
                    fileManagerViewModel.updateFileItem(currentItem.value)
                    isDialogOpened = !(currentItem.value?.isEnabled ?: false)

                    fileManagerViewModel.setPassword("")
                    fileManagerViewModel.setKeepOriginal(false)
                    fileManagerViewModel.setOverwriteExisting(false)

                    if (!fileManagerViewModel.any(
                            status.value,
                            FileManagerViewModel.StatusType.Processing,
                            FileManagerViewModel.StatusType.Loading
                        )
                    ) fileManagerViewModel.updateFileItem(null)
                }
            },
            /*title = {
            Column {
                Text(
                    text = currentItem.value?.inputInfo?.parent?.pathString ?: "",
                    style = TextStyle(color = Foreground2, fontSize = 12.sp),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                Row {
                    if (currentItem.value?.inputInfo?.isRegularFile() == true) {
                        Text(
                            text = "${
                                currentItem.value?.inputInfo?.fileSize()?.let {
                                    FileUtils.getReadableFileSize(it)
                                }
                            }",
                            style = TextStyle(color = Foreground2, fontSize = 12.sp),
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = DateUtils.formatRelative(
                            context,
                            currentItem.value?.inputInfo?.getLastModifiedTime()?.toMillis()
                                ?: 0L
                        ), style = TextStyle(color = Foreground2, fontSize = 12.sp)
                    )
                }
            }
        },*/
            text = {
                Column {
                    InputPasswordBox(
                        value = fileManagerViewModel.password.collectAsState().value,

                        enabled = passwordEnabled.value, 70,
                        onChangeCallback = { fileManagerViewModel.setPassword(it) },
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = keepOriginal.value,
                            onCheckedChange = { fileManagerViewModel.setKeepOriginal(it) },
                            enabled = keepOriginalEnabled.value,
                        )
                        Text(
                            text = stringResource(id = R.string.keep_original_files),
                            modifier = Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { fileManagerViewModel.setKeepOriginal(!keepOriginal.value) },
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    currentItem.value?.isEnabled?.let {
                        Button(
                            shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Theme
                            ), onClick = {
                                coroutineScope.launch {
                                    val m =
                                        if (currentItem.value?.itemType == FileItem.FileItemType.Normal) R.string.lock_success else R.string.unlock_success
                                    val result = fileManagerViewModel.processFile()
                                    if (result) {
                                        fileManagerViewModel.setPassword("")
                                        fileManagerViewModel.setKeepOriginal(false)
                                        fileManagerViewModel.setOverwriteExisting(false)
                                        isDialogOpened = false
                                        Toast.makeText(
                                            context, context.getString(m), Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }, enabled = it
                        ) {
                            Text(
                                text = if (currentItem.value?.itemType == FileItem.FileItemType.Normal) stringResource(
                                    id = R.string.lock
                                )
                                else stringResource(id = R.string.unlock), color = Color.White
                            )
                        }
                    }
                }
            },
        )
        if (currentItem.value?.footer?.extra?.IsThumbnailEncrypted == true) {
            val footerThumbnailBuffer = currentItem.value!!.footer!!.footerThumbnailBuffer
            val thumbnailBytes = decryptBuffer(
                footerThumbnailBuffer,
                password.toString(),
                Constants.Salt,
                Constants.Iterations,
                Constants.KeyLength
            )
            val outputDir = context.cacheDir // context being the Activity pointer
            val outputFile = File.createTempFile("prefix", ".extension", outputDir)
            currentItem.value!!.tempThumbnailPath = outputFile.toString()
            AsyncImage(
                model = outputFile,
                contentDescription = null,
            )
        }

    }
}

@Composable
fun OnClickButton(item: FileItem, fileManagerViewModel: FileManagerViewModel, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        shape = CircleShape,
        modifier = Modifier
            .padding(16.dp)
            .size(width = 60.dp, height = 35.dp)
            .clip(CircleShape)
            .border(
                BorderStroke(width = 2.dp, color = Color.Unspecified), shape = CircleShape
            ),

        colors = ButtonDefaults.buttonColors(Color.Black)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_thumbnail),
                contentDescription = "Favorite Icon",
                tint = Color.White,
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
            )
        }
    }
}

@Composable
fun DialogTitle(currentItem: FileItem?) {
    Column {
        Text(
            text = currentItem?.inputInfo?.parent?.pathString ?: "",
            style = TextStyle(color = Foreground2, fontSize = 12.sp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
        Row {
            if (currentItem?.inputInfo?.isRegularFile() == true) {
                Text(
                    text = currentItem.inputInfo.fileSize()
                        .let { FileUtils.getReadableFileSize(it) },
                    style = TextStyle(color = Foreground2, fontSize = 12.sp),
                    maxLines = 1,
                )
            }
            Text(
                text = DateUtils.formatRelative(
                    LocalContext.current,
                    currentItem?.inputInfo?.getLastModifiedTime()?.toMillis() ?: 0L
                ), style = TextStyle(color = Foreground2, fontSize = 12.sp)
            )
        }
    }
}


@Composable
fun DialogContent(
    fileManagerViewModel: FileManagerViewModel,
    passwordEnabled: Boolean,
    keepOriginal: Boolean,
    keepOriginalEnabled: Boolean,
    interactionSource: MutableInteractionSource
) {
    Column {
        InputPasswordBox(
            value = fileManagerViewModel.password.collectAsState().value,
            enabled = passwordEnabled,
            width = 70,
            onChangeCallback = { fileManagerViewModel.setPassword(it) },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = keepOriginal,
                onCheckedChange = { fileManagerViewModel.setKeepOriginal(it) },
                enabled = keepOriginalEnabled,
            )
            Text(
                text = stringResource(id = R.string.keep_original_files),
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { fileManagerViewModel.setKeepOriginal(!keepOriginal) },
                )
            )
        }
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            Checkbox(
//                checked = overwriteExisting.value,
//                onCheckedChange = { mainViewModel.setOverwriteExisting(it) },
//                enabled = overwriteExistingEnabled.value,
//            )
//            Text(
//                text = stringResource(id = R.string.overwrite_existing),
//                modifier = Modifier.clickable(
//                    interactionSource = interactionSource,
//                    indication = null,
//                    onClick = { mainViewModel.setOverwriteExisting(!overwriteExisting.value) },
//                )
//            )
//        }
    }
}


@Composable
fun DialogConfirmButton(
    currentItem: FileItem?,
    fileManagerViewModel: FileManagerViewModel,
    isDialogOpened: Boolean,
    setDialogOpened: (Boolean) -> Unit,
    coroutineScope: CoroutineScope,
    context: Context
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        currentItem?.isEnabled?.let {
            Button(
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Theme),
                onClick = {
                    coroutineScope.launch {
                        val messageResId =
                            if (currentItem.itemType == FileItem.FileItemType.Normal) R.string.lock_success else R.string.unlock_success
                        val result = fileManagerViewModel.processFile()
                        if (result) {
                            fileManagerViewModel.setPassword("")
                            fileManagerViewModel.setKeepOriginal(false)
                            fileManagerViewModel.setOverwriteExisting(false)
                            setDialogOpened(false)
                            Toast.makeText(
                                context, context.getString(messageResId), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                enabled = it
            ) {
                Text(
                    text = if (currentItem.itemType == FileItem.FileItemType.Normal) stringResource(
                        id = R.string.lock
                    ) else stringResource(id = R.string.unlock), color = Color.White
                )
            }
        }
    }
}
