package vn.iostream.apps.file_locker_x.ui.composables

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import vn.iostream.apps.core.iofile.FileUtils
import vn.iostream.apps.file_locker_x.R
import vn.iostream.apps.file_locker_x.models.FileItem
import vn.iostream.apps.file_locker_x.ui.theme.Theme
import vn.iostream.apps.file_locker_x.viewmodels.FileManagerViewModel

@Composable
fun FloatButton(
    fileManagerViewModel: FileManagerViewModel, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun addFileFromUri(uri: Uri?, isTree: Boolean) {
        if (uri == null) return

        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        val finalUri = if (!isTree) uri
        else DocumentsContract.buildDocumentUriUsingTree(
            uri, DocumentsContract.getTreeDocumentId(uri)
        )

        FileUtils.getPathFromUri(context, finalUri)?.let { path ->
            coroutineScope.launch {
                fileManagerViewModel.addOne(FileItem(path))
            }
        }
    }

    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data?.clipData != null) {
                    val count = result.data?.clipData?.itemCount ?: 0

                    for (i in 0 until count) addFileFromUri(
                        result.data?.clipData?.getItemAt(i)?.uri,
                        false
                    )
                } else addFileFromUri(result.data?.data, false)
            }
        }

    val directoryPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                addFileFromUri(result.data?.data, true)
            }
        }

    Column(
        modifier = modifier
    ) {
        MultiFloatingActionButton(
            fabIcon = FabIcon(
                iconRes = R.drawable.baseline_add_24,
                iconResAfterRotate = R.drawable.baseline_add_24,
                iconRotate = 135f
            ),
            fabOption = FabOption(
                iconTint = Color.White,
                showLabels = true,
                backgroundTint = Theme,
            ),
            itemsMultiFab = listOf(
                MultiFabItem(
                    tag = "AddFiles",
                    icon = R.drawable.baseline_insert_drive_file_24,
                    label = stringResource(id = R.string.add_files),
                ),
                MultiFabItem(
                    tag = "AddAFolder",
                    icon = R.drawable.baseline_folder_24,
                    label = stringResource(id = R.string.add_a_folder),
                ),
            ),
            onFabItemClicked = {
                if (it.tag == "AddFiles") {
                    var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.putExtra(
                        DocumentsContract.EXTRA_INITIAL_URI,
                        Environment.getExternalStorageDirectory().toUri()
                    )
                    intent.type = "*/*"
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent = Intent.createChooser(intent, "Add files")
                    filePickerLauncher.launch(intent)
                } else {
                    var intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent = Intent.createChooser(intent, "Add a folder")
                    directoryPickerLauncher.launch(intent)
                }
            },
            fabTitle = "MultiFloatActionButton", showFabTitle = false,
        )
    }
}