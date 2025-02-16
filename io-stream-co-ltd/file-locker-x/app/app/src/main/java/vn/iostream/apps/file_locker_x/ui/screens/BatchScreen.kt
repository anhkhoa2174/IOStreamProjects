package vn.iostream.apps.file_locker_x.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.iostream.apps.core.iofile.FileSystemItem
import vn.iostream.apps.core.iofile.FileUtils
import vn.iostream.apps.file_locker_x.R
import vn.iostream.apps.file_locker_x.configs.AdConfig
import vn.iostream.apps.file_locker_x.customDialog
import vn.iostream.apps.file_locker_x.models.FileItem
import vn.iostream.apps.file_locker_x.ui.composables.BannerAdView
import vn.iostream.apps.file_locker_x.ui.composables.DialogItem
import vn.iostream.apps.file_locker_x.ui.composables.DropdownMenuItem
import vn.iostream.apps.file_locker_x.ui.composables.HintView
import vn.iostream.apps.file_locker_x.ui.composables.InputPasswordBox
import vn.iostream.apps.file_locker_x.ui.composables.MenuItem
import vn.iostream.apps.file_locker_x.ui.composables.OnLifecycleEvent
import vn.iostream.apps.file_locker_x.ui.composables.TopBar
import vn.iostream.apps.file_locker_x.ui.theme.Foreground2
import vn.iostream.apps.file_locker_x.ui.theme.Theme
import vn.iostream.apps.file_locker_x.ui.theme.seed
import vn.iostream.apps.file_locker_x.utils.DateUtils
import vn.iostream.apps.file_locker_x.viewmodels.BatchViewModel
import vn.iostream.apps.file_locker_x.viewmodels.FileManagerViewModel
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.pathString

@Composable
fun BatchScreen(
    navController: NavHostController,
    initialFeatureType: FileItem.FeatureType,
    fileManagerViewModel: FileManagerViewModel,
    batchViewModel: BatchViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val status = batchViewModel.status.collectAsState()
    val selectMode = batchViewModel.selectMode.collectAsState()

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                batchViewModel.refreshGlobalVariables(fileManagerViewModel)
                batchViewModel.setFeatureType(initialFeatureType)
                coroutineScope.launch {
                    val addedItems = fileManagerViewModel.items.value.map { it.clone() }
                    batchViewModel.loadBatchingFiles(addedItems, initialFeatureType)
                }
            }

            Lifecycle.Event.ON_RESUME -> {
                batchViewModel.refreshGlobalVariables(fileManagerViewModel)
                batchViewModel.setFeatureType(initialFeatureType)
                coroutineScope.launch {
                    val addedItems = fileManagerViewModel.items.value.map { it.clone() }
                    batchViewModel.loadBatchingFiles(addedItems, initialFeatureType)
                }
            }

            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            batchViewModel.reset(context)
        }
    }

    val isProcessing = batchViewModel.any(
        status.value,
        BatchViewModel.StatusType.Loading,
        BatchViewModel.StatusType.Processing,
        BatchViewModel.StatusType.Pausing,
        BatchViewModel.StatusType.Stopping
    )

    BackHandler(true) {
        if (selectMode.value) {
            batchViewModel.setSelectMode(false)
        } else if (!isProcessing) {
            batchViewModel.removeMany(BatchViewModel.RemoveMode.All)
            navController.popBackStack()
        }
    }

    Scaffold(
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                ContentTopBar(batchViewModel, navController)
                ListContainerBox(navController, batchViewModel)
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent
            ) {
                ContentBottomBar(batchViewModel = batchViewModel)
            }
        },
    )
}

@Composable
private fun ContentTopBar(batchViewModel: BatchViewModel, navController: NavHostController) {
    val status = batchViewModel.status.collectAsState()
    val featureType = batchViewModel.featureType.collectAsState()

    val keepOriginalEnabled = batchViewModel.keepOriginalEnabled.collectAsState()
    val overwriteExistingEnabled = batchViewModel.overwriteExistingEnabled.collectAsState()

    val keepOriginal = batchViewModel.keepOriginal.collectAsState()
    val overwriteExisting = batchViewModel.overwriteExisting.collectAsState()

    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val data: Intent? = it.data

            if (data != null) {
                val takeFlags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                if (data.clipData != null) {
                    val count = data.clipData?.itemCount ?: 0

                    for (i in 0 until count) {
                        val uri: Uri? = data.clipData?.getItemAt(i)?.uri

                        if (uri != null) {
                            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                            val filePath = FileUtils.getPathFromUri(context, uri)
                            filePath?.let {
                                batchViewModel.addOne(
                                    FileItem(filePath), featureType.value
                                )
                            }
                        }
                    }
                } else if (data.data != null) {
                    val uri: Uri? = data.data

                    if (uri != null) {
                        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                        val filePath = FileUtils.getPathFromUri(context, uri)
                        filePath?.let {
                            batchViewModel.addOne(
                                FileItem(filePath), featureType.value
                            )
                        }
                    }
                }
            }
        }
    }
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val data: Intent? = it.data

            if (data != null) {
                val uri: Uri? = data.data

                if (uri != null) {
                    val takeFlags: Int =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                    val treeUri = DocumentsContract.buildDocumentUriUsingTree(
                        uri, DocumentsContract.getTreeDocumentId(uri)
                    )
                    val filePath = FileUtils.getPathFromUri(context, treeUri)
                    filePath?.let {
                        batchViewModel.addOne(
                            FileItem(filePath), featureType.value
                        )
                    }
                }
            }
        }
    }
    val title =
        if (featureType.value == FileItem.FeatureType.Encode) stringResource(id = R.string.batch_locker)
        else stringResource(id = R.string.batch_unlocker)

    TopBar(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer), left = {
        TextButton(onClick = {
            navController.popBackStack()
        }) {
            Icon(
                Icons.Default.ArrowBack, null, modifier = Modifier.size(24.dp)
            )
        }
    }, title = {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center
            ),
            overflow = TextOverflow.Ellipsis,
        )
    }, right = {
        var showSetting by remember { mutableStateOf(false) }
        val processing = batchViewModel.any(
            status.value,
            BatchViewModel.StatusType.Loading,
            BatchViewModel.StatusType.Pausing,
            BatchViewModel.StatusType.Processing,
            BatchViewModel.StatusType.Stopping,
        )

        IconButton(
            onClick = {
                if (!processing) {
                    showSetting = !showSetting
                }
            }, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.more_horiz),
                null,
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(expanded = showSetting, onDismissRequest = { showSetting = false }) {
            DropdownMenuItem(onClick = {}, text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = keepOriginal.value,
                        enabled = keepOriginalEnabled.value,
                        onCheckedChange = {
                            batchViewModel.setKeepOriginal(it)
                        },
                    )
                    Text(stringResource(id = R.string.keep_original_files))
                }
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.Start
//                        ) {
//                            Checkbox(
//                                checked = overwriteExisting.value,
//                                enabled = overwriteExistingEnabled.value,
//                                onCheckedChange = {
//                                    batchViewModel.setOverwriteExisting(it)
//                                },
//                            )
//                            Text(stringResource(id = R.string.overwrite_existing))
//                        }
            })

            val menuItems = listOf(
                MenuItem(title = stringResource(id = R.string.add_files),
                    iconResId = R.drawable.baseline_insert_drive_file_24,
                    onClick = {
                        var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "*/*"
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent = Intent.createChooser(intent, "Add files")
                        filePickerLauncher.launch(intent)
                    }),
                MenuItem(title = stringResource(id = R.string.add_a_folder),
                    iconResId = R.drawable.baseline_folder_24,
                    onClick = {
                        var intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent = Intent.createChooser(intent, "Add a folder")
                        directoryPickerLauncher.launch(intent)
                    }),
            )

            menuItems.forEach { item ->
                DropdownMenuItem(item = item)
            }
        }
    })
}

@Composable
private fun ListTopBar(viewModel: BatchViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .height(52.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val items = viewModel.items.collectAsState()
        val selectMode = viewModel.selectMode.collectAsState()
        val status = viewModel.status.collectAsState()
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
                        IconButton(onClick = {
                            viewModel.setSelectMode(false)
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Theme
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedItemsCount.size} ${stringResource(id = if (selectedItemsCount.size > 1) R.string.items else R.string.item)}",
                                style = TextStyle(color = Theme)
                            )
                            IconButton(onClick = {
                                if (!customDialog.getState()) {
                                    customDialog.title =
                                        context.getString(R.string.remove_selected_items_from_list_title)
                                    customDialog.subTitle =
                                        context.getString(R.string.remove_selected_items_from_list_subtitle)
                                    customDialog.onConfirmCallback = {
                                        viewModel.removeSelected()
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

                            DropdownMenu(expanded = showSetting,
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
                                                viewModel.moveSelectedToDocuments(
                                                    false
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
                                                viewModel.moveSelectedToDocuments(
                                                    true
                                                )
                                            }),
                                    ),
                                    MenuItem(
                                        title = stringResource(id = R.string.delete_permanently),
                                        iconResId = R.drawable.baseline_delete_24,
                                        dialogItem = DialogItem(context = context,
                                            titleResIdInt = R.string.delete_permanently_title,
                                            subtitleResIdInt = R.string.delete_permanently_subtitle,
                                            coroutineScope = coroutineScope,
                                            onConfirmCallback = { viewModel.deleteSelectedPermanently() }),
                                    ),
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
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${items.value.count()} ${stringResource(id = if (items.value.count() > 1) R.string.items else R.string.item)}")

                            if (!viewModel.any(
                                    status.value,
                                    BatchViewModel.StatusType.Loading,
                                    BatchViewModel.StatusType.Pausing,
                                    BatchViewModel.StatusType.Processing,
                                    BatchViewModel.StatusType.Stopping
                                )
                            ) {
                                IconButton(onClick = {
                                    if (!customDialog.getState()) {
                                        customDialog.title =
                                            context.getString(R.string.remove_all_of_items_title)
                                        customDialog.subTitle =
                                            context.getString(R.string.remove_all_of_items_subtitle)
                                        customDialog.onConfirmCallback = {
                                            coroutineScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    viewModel.removeMany(BatchViewModel.RemoveMode.All)
                                                }
                                            }
                                        }
                                    }

                                    customDialog.enable(!customDialog.getState())
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_playlist_remove_24),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            } else {
                                IconButton(onClick = {}) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_playlist_remove_24),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Transparent
                                    )
                                }
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
private fun ListContainerBox(navController: NavHostController, batchViewModel: BatchViewModel) {
    val context = LocalContext.current

    val selectMode = batchViewModel.selectMode.collectAsState()
    val featureType = batchViewModel.featureType.collectAsState()
    val status = batchViewModel.status.collectAsState()
    val items = batchViewModel.items.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val shareLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val openLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    if (items.value.isEmpty()) {
        val hintIcon = when (featureType.value) {
            FileItem.FeatureType.Encode -> painterResource(R.drawable.baseline_lock_24)
            FileItem.FeatureType.Decode -> painterResource(R.drawable.baseline_lock_open_24)
        }
        val title = stringResource(id = R.string.no_items_here)
        val subtitle = stringResource(id = R.string.add_files_and_folders)

        HintView(
            hintIconPainter = hintIcon, title = title, subTitle = subtitle
        )
    } else {
        Column {
            ListTopBar(batchViewModel)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                itemsIndexed(items.value,
                    key = { _, item -> item.inputInfo.pathString }) { index, item ->
                    val message =
                        if (!item.inputInfo.exists()) stringResource(id = R.string.this_file_no_longer_exists) else if (item.itemType == FileItem.FileItemType.EncodedFile) stringResource(
                            id = R.string.this_is_a_locked_file
                        ) else stringResource(id = R.string.this_is_a_locked_folder)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (index == items.value.size - 1) 96.dp else 0.dp)
                            .background(if (selectMode.value && item.isSelected) seed else Color.Transparent)
                            .combinedClickable(onClick = {
                                if (!item.isEnabled || batchViewModel.any(
                                        status.value,
                                        BatchViewModel.StatusType.Pausing,
                                        BatchViewModel.StatusType.Loading,
                                        BatchViewModel.StatusType.Stopping,
                                        BatchViewModel.StatusType.Processing
                                    )
                                ) return@combinedClickable

                                if (selectMode.value) {
                                    item.isSelected = !item.isSelected
                                    batchViewModel.updateOne(item)
                                } else {
                                    batchViewModel.updateOne(item)
                                    val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)

                                    if (!item.inputInfo.exists()) {
                                        toast.setGravity(Gravity.CENTER, 0, 0)
                                        toast.show()
                                    } else if (item.itemType != FileItem.FileItemType.Normal) {
                                        toast.setGravity(Gravity.CENTER, 0, 0)
                                        toast.show()
                                    } else {
                                        val itemUri = FileUtils.getFileProviderUri(
                                            context, item.inputInfo.pathString
                                        )

                                        if (item.inputInfo.isRegularFile()) {
                                            val intent = Intent(Intent.ACTION_VIEW, itemUri)
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            openLauncher.launch(
                                                Intent.createChooser(
                                                    intent, context.getString(R.string.open_file)
                                                )
                                            )
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
                                batchViewModel.setSelectMode(true)
                                item.isSelected = true
                                batchViewModel.updateOne(item)
                            }), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            IconButton(modifier = Modifier.height(32.dp), onClick = {
                                if (!item.isEnabled || batchViewModel.any(
                                        status.value,
                                        BatchViewModel.StatusType.Pausing,
                                        BatchViewModel.StatusType.Loading,
                                        BatchViewModel.StatusType.Stopping,
                                        BatchViewModel.StatusType.Processing
                                    )
                                ) return@IconButton

                                batchViewModel.selectItem(item)
                            }) {
                                Icon(
                                    painter = if (selectMode.value && item.isSelected) painterResource(
                                        R.drawable.baseline_check_circle_24
                                    ) else if (item.inputInfo.isRegularFile() && item.itemType != FileItem.FileItemType.EncodedFolder) painterResource(
                                        R.drawable.baseline_insert_drive_file_24
                                    ) else painterResource(R.drawable.baseline_folder_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = if (item.isSelected && selectMode.value) Theme else LocalContentColor.current
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
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier
                                        .weight(1F)
                                        .height(72.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = item.inputInfo.name,
                                        style = TextStyle(
                                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Row {
                                        if (item.inputInfo.isRegularFile() && FileUtils.getExtension(
                                                item.inputInfo.name
                                            ).isNotEmpty()
                                        ) {
                                            Text(
                                                text = ".${FileUtils.getExtension(item.inputInfo.name)}, ",
                                                style = TextStyle(color = Foreground2)
                                            )
                                        }
                                        if (item.inputInfo.isRegularFile()) {
                                            Text(
                                                text = "${FileUtils.getReadableFileSize(item.inputInfo.fileSize())}, ",
                                                style = TextStyle(color = Foreground2)
                                            )
                                        }
                                        Text(
                                            text = DateUtils.formatRelative(
                                                context,
                                                item.inputInfo.getLastModifiedTime().toMillis()
                                            ),
                                            style = TextStyle(color = Foreground2, fontSize = 12.sp)
                                        )
                                    }

                                    Text(
                                        text = FileUtils.getBriefOfPath(item.inputInfo.parent.pathString),
                                        style = TextStyle(color = Foreground2, fontSize = 12.sp)
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .offset(0.dp, 7.dp)
                                    ) {
                                        if (item.status.any(FileSystemItem.S.Processing)) {
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

                                    if (item.isEnabled && !selectMode.value) {
                                        IconButton(
                                            onClick = { isMenuOpened = !isMenuOpened },
                                            enabled = item.isEnabled
                                        ) {
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
                                                title = stringResource(id = R.string.move_to_documents),
                                                iconResId = R.drawable.baseline_content_cut_24,
                                                dialogItem = DialogItem(context = context,
                                                    titleResIdInt = R.string.move_to_documents_title,
                                                    subtitleResIdInt = R.string.move_to_documents_subtitle,
                                                    coroutineScope = coroutineScope,
                                                    onConfirmCallback = {
                                                        batchViewModel.moveToDocuments(
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
                                                        batchViewModel.moveToDocuments(
                                                            item, true
                                                        )
                                                    }),
                                            ),
                                            MenuItem(
                                                title = stringResource(id = R.string.remove_from_list),
                                                iconResId = R.drawable.baseline_playlist_remove_24,
                                                onClick = {
                                                    batchViewModel.removeOne(item)
                                                    isMenuOpened = false
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
                                                        batchViewModel.deletePermanently(
                                                            item
                                                        )
                                                    }),
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
}

@Composable
private fun ContentBottomBar(batchViewModel: BatchViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp

    val featureType = batchViewModel.featureType.collectAsState()

    val password = batchViewModel.password.collectAsState()
    val passwordEnabled = batchViewModel.passwordEnabled.collectAsState()

    val processAllButtonVisible = batchViewModel.processAllButtonVisible.collectAsState()
    val resumePauseStopButtonsPanelVisible =
        batchViewModel.resumePauseStopButtonsPanelVisible.collectAsState()

    val processAllButtonEnabled = batchViewModel.processAllButtonEnabled.collectAsState()

    val resumePauseButtonText = batchViewModel.resumePauseButtonText.collectAsState()
    val resumePauseButtonVisible = batchViewModel.resumePauseButtonVisible.collectAsState()
    val resumePauseButtonEnabled = batchViewModel.resumePauseButtonEnabled.collectAsState()

    val stopButtonVisible = batchViewModel.stopButtonVisible.collectAsState()
    val stopButtonEnabled = batchViewModel.stopButtonEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height((56 + 60 + screenWidth.value * 5 / 100).dp),
    ) {
        Row {
            Spacer(modifier = Modifier.width(screenWidth * 5 / 100))

            Row(
                modifier = Modifier.clip(RoundedCornerShape(5.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (processAllButtonVisible.value) {
                    InputPasswordBox(password.value, passwordEnabled.value) {
                        batchViewModel.setPassword(it)
                    }
                    Button(
                        shape = RoundedCornerShape(0.dp), colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White,
                        ), enabled = processAllButtonEnabled.value, onClick = {
                            if (password.value.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.no_password_provided),
                                    Toast.LENGTH_SHORT
                                ).show()

                                return@Button
                            }

                            coroutineScope.launch {
                                batchViewModel.pressProcessAll(context)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.process_done),
                                    Toast.LENGTH_SHORT
                                ).show()
                                batchViewModel.setPassword("")
                            }
                        }, modifier = Modifier.height(56.dp)
                    ) {
                        Text(
                            text = if (featureType.value == FileItem.FeatureType.Encode) stringResource(
                                id = R.string.lock_all
                            ) else stringResource(
                                id = R.string.unlock_all
                            )
                        )
                    }
                }

                if (resumePauseStopButtonsPanelVisible.value) {
                    if (resumePauseButtonVisible.value) {
                        val alpha = if (resumePauseButtonEnabled.value) 1f else 0.3f
                        Button(
                            shape = RoundedCornerShape(0.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xff202020),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray,
                                disabledContentColor = Color.White,
                            ), enabled = resumePauseButtonEnabled.value, onClick = {
                                coroutineScope.launch {
                                    batchViewModel.pressResumeOrPause(context)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.process_done),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }, modifier = Modifier.height(56.dp)
                        ) {
                            Text(
                                text = resumePauseButtonText.value,
                                modifier = Modifier.alpha(alpha = alpha)
                            )
                        }
                    }

                    if (stopButtonVisible.value) {
                        val alpha = if (stopButtonEnabled.value) 1f else 0.3f

                        Button(
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xff333333),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray,
                                disabledContentColor = Color.White,
                            ),
                            enabled = stopButtonEnabled.value,
                            onClick = { batchViewModel.pressStop() },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.stop),
                                modifier = Modifier.alpha(alpha = alpha)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(screenWidth * 5 / 100))
        }

//        BannerAds()
    }
}

@Composable
private fun BannerAds() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(top = 4.dp)
            .padding(bottom = 4.dp)
    ) {
        BannerAdView(adUnitId = AdConfig.BATCH_BOTTOM)
    }
}
