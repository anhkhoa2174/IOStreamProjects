package vn.iostream.apps.file_locker_x.viewmodels

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.iostream.apps.core.iofile.FileSystemItem
import vn.iostream.apps.core.iofile.FileUtils
import vn.iostream.apps.file_locker_x.R
import vn.iostream.apps.file_locker_x.features.Share
import vn.iostream.apps.file_locker_x.models.FileItem
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.pathString

@HiltViewModel
class BatchViewModel @Inject constructor() : ViewModel() {
    enum class StatusType {
        Init, Loading, Loaded, LoadFailed, Processing, Processed, Failed, Pausing, Paused, Stopping, Stopped
    }

    enum class UpdateItemsFlag {
        Add, Update, Delete
    }

    enum class RemoveMode {
        All, Selected, NonExisted
    }

    private var _argv: Share.Argv? = null
    private var _fileManagerViewModel: FileManagerViewModel? = null

    private val _items = mutableStateListOf<FileItem>()
    val items = MutableStateFlow(_items)

    private val _availableItems = mutableStateListOf<FileItem>()

    private var _featureType = MutableStateFlow(FileItem.FeatureType.Encode)
    val featureType = _featureType.asStateFlow()

    private var _status = MutableStateFlow(StatusType.Init)
    val status = _status.asStateFlow()

    private var _password = MutableStateFlow(String())
    val password = _password.asStateFlow()

    private var _keepOriginal = MutableStateFlow(false)
    val keepOriginal = _keepOriginal.asStateFlow()

    private var _overwriteExisting = MutableStateFlow(false)
    val overwriteExisting = _overwriteExisting.asStateFlow()

    private var _addFilesVisible = MutableStateFlow(true)
    val addFilesVisible = _addFilesVisible.asStateFlow()

    private var _passwordEnabled = MutableStateFlow(true)
    val passwordEnabled = _passwordEnabled.asStateFlow()

    private var _keepOriginalEnabled = MutableStateFlow(true)
    val keepOriginalEnabled = _keepOriginalEnabled.asStateFlow()

    private var _overwriteExistingEnabled = MutableStateFlow(true)
    val overwriteExistingEnabled = _overwriteExistingEnabled.asStateFlow()

    private var _processAllButtonVisible = MutableStateFlow(true)
    val processAllButtonVisible = _processAllButtonVisible.asStateFlow()

    private var _processAllButtonEnabled = MutableStateFlow(true)
    val processAllButtonEnabled = _processAllButtonEnabled.asStateFlow()

    /*
    * resume-pause & stop buttons
    * */

    private val _resumePauseStopButtonsPanelVisible = MutableStateFlow(false)
    val resumePauseStopButtonsPanelVisible = _resumePauseStopButtonsPanelVisible.asStateFlow()

    /*
    * resume-pause button
    * */

    private var _resumePauseButtonText = MutableStateFlow("Resume")
    val resumePauseButtonText = _resumePauseButtonText.asStateFlow()

    private var _resumePauseButtonVisible = MutableStateFlow(false)
    val resumePauseButtonVisible = _resumePauseButtonVisible.asStateFlow()

    private var _resumePauseButtonEnabled = MutableStateFlow(false)
    val resumePauseButtonEnabled = _resumePauseButtonEnabled.asStateFlow()

    /*
    * stop button
    * */

    private var _stopButtonVisible = MutableStateFlow(false)
    val stopButtonVisible = _stopButtonVisible.asStateFlow()

    private var _stopButtonEnabled = MutableStateFlow(false)
    val stopButtonEnabled = _stopButtonEnabled.asStateFlow()

    /*
    * Other controls
    */

    private var _selectMode = MutableStateFlow(false)
    val selectMode = _selectMode.asStateFlow()

    /*
    * Methods
    * */

    fun refreshGlobalVariables(fileManagerViewModel: FileManagerViewModel) {
        _fileManagerViewModel = fileManagerViewModel
    }

    private fun prepareArgv() =
        Share.Argv(password.value, keepOriginal.value, overwriteExisting.value)

    private fun updateItems(newItem: FileItem, actionFlag: UpdateItemsFlag) {
        var itemsEdited = false

        when (actionFlag) {
            UpdateItemsFlag.Add -> {
                _items.add(newItem)
                itemsEdited = true

                viewModelScope.launch {
                    _fileManagerViewModel?.addOne(newItem)
                }
            }

            UpdateItemsFlag.Delete -> {
                val index =
                    _items.indexOfFirst { it.inputInfo.pathString == newItem.inputInfo.pathString }
                if (index > -1) {
                    _items.removeAt(index)
                    itemsEdited = true
                }
            }

            UpdateItemsFlag.Update -> {
                val index =
                    _items.indexOfFirst { it.inputInfo.pathString == newItem.inputInfo.pathString }
                if (index > -1) {
                    _items[index] = newItem.clone()
                    itemsEdited = true
                }
            }
        }

        if (itemsEdited) {
            if (_status.value != StatusType.Loading) {
                _processAllButtonVisible.value = true
                _resumePauseStopButtonsPanelVisible.value = false
            }
        }
    }

    private fun enableInputControls(value: Boolean) {
        _addFilesVisible.value = value
    }

    private fun enableOutputControl(value: Boolean) {
        _passwordEnabled.value = value
        _keepOriginalEnabled.value = value
        _overwriteExistingEnabled.value = value

        _processAllButtonEnabled.value = value
        _resumePauseButtonEnabled.value = value
        _stopButtonEnabled.value = value
    }

    fun setFeatureType(_newFeatureType: FileItem.FeatureType) {
        _featureType.value = _newFeatureType
    }

    fun setPassword(value: String) {
        _password.value = value
    }

    fun setKeepOriginal(value: Boolean) {
        _keepOriginal.value = value
    }

    fun setOverwriteExisting(value: Boolean) {
        _overwriteExisting.value = value
    }

    fun setSelectMode(value: Boolean) {
        _selectMode.value = value
    }

    private fun updateStatus(value: StatusType) {
        val prevStatus = _status.value
        _status.value = value

        when (value) {
            StatusType.Init -> {
                enableInputControls(true)
                enableOutputControl(false)
            }

            StatusType.Loading -> {
                enableInputControls(false)
                enableOutputControl(false)
            }

            StatusType.Loaded -> {
                enableInputControls(true)
                enableOutputControl(_availableItems.size > 0)
            }

            StatusType.LoadFailed -> {
                enableInputControls(true)
                enableOutputControl(_availableItems.size > 0)
            }

            StatusType.Processing -> {
                enableInputControls(false)
                enableOutputControl(false)

                _resumePauseButtonEnabled.value = true
                _stopButtonEnabled.value = true

                _processAllButtonVisible.value = false
                _resumePauseStopButtonsPanelVisible.value = true
                _resumePauseButtonVisible.value = true
                _stopButtonVisible.value = true

                for (index in 0 until _items.size) {
                    val updatedItem = _items[index]
                    updatedItem.isEnabled = false
                    updateOne(updatedItem)
                }
            }

            StatusType.Processed -> {
                _availableItems.removeIf { it.status.any(FileSystemItem.S.Processed) }
                _fileManagerViewModel?.addFiles(_items.toList())

                enableInputControls(true)
                enableOutputControl(_availableItems.size > 0)

                _processAllButtonVisible.value = true
                _processAllButtonEnabled.value = true
                _resumePauseStopButtonsPanelVisible.value = false

                for (index in 0 until _items.size) {
                    val updatedItem = _items[index]
                    updatedItem.isEnabled = true
                    updateOne(updatedItem)
                }
            }

            StatusType.Pausing -> {
                enableInputControls(false)
                enableOutputControl(false)
            }

            StatusType.Paused -> {
                enableInputControls(false)
                enableOutputControl(_availableItems.size > 0)

                _processAllButtonVisible.value = false
                _resumePauseStopButtonsPanelVisible.value = true
                _resumePauseButtonVisible.value = true
                _stopButtonVisible.value = true
            }

            StatusType.Stopping -> {
                if (prevStatus == StatusType.Paused) {
                    enableInputControls(true)
                    enableOutputControl(_availableItems.size > 0)

                    _processAllButtonVisible.value = true
                    _resumePauseStopButtonsPanelVisible.value = false
                } else {
                    enableInputControls(false)
                    enableOutputControl(false)
                }
            }

            StatusType.Stopped -> {
                _availableItems.removeIf { it.status.any(FileSystemItem.S.Processed) }
                _fileManagerViewModel?.addFiles(_items.toList())

                enableInputControls(true)
                enableOutputControl(_availableItems.size > 0)

                _processAllButtonVisible.value = true
                _resumePauseStopButtonsPanelVisible.value = false

                for (index in 0 until _items.size) {
                    val updatedItem = _items[index]
                    updatedItem.isEnabled = true
                    updateOne(updatedItem)
                }
            }

            StatusType.Failed -> {
                enableInputControls(true)
                enableOutputControl(_availableItems.size > 0)

                for (index in 0 until _items.size) {
                    val updatedItem = _items[index]
                    updatedItem.isEnabled = true
                    updateOne(updatedItem)
                }
            }
        }
    }

    suspend fun pressResumeOrPause(context: Context) {
        if (_resumePauseButtonText.value == context.getString(R.string.pause)) {
            updateStatus(StatusType.Pausing)
            _resumePauseButtonText.value = context.getString(R.string.resume)
        } else {
            _resumePauseButtonText.value = context.getString(R.string.pause)
            processAll(context, true)
        }
    }

    suspend fun pressProcessAll(context: Context) {
        _resumePauseButtonText.value = context.getString(R.string.pause)
        processAll(context, false)
    }

    fun pressStop() {
        updateStatus(
            if (_status.value == StatusType.Paused) StatusType.Stopped else StatusType.Stopping
        )
    }

    private fun isAvailableFileItem(
        item: FileItem, type: FileItem.FeatureType = _featureType.value
    ): Boolean {
        return (type == FileItem.FeatureType.Encode && item.itemType == FileItem.FileItemType.Normal) || (type == FileItem.FeatureType.Decode && item.itemType != FileItem.FileItemType.Normal)
    }

    fun addOne(item: FileItem, type: FileItem.FeatureType) {
        val duplicateItem = _items.find { it.inputInfo.pathString == item.inputInfo.pathString }
        if (duplicateItem != null) {
            updateItems(duplicateItem, UpdateItemsFlag.Update)
            return
        }

        if (type == FileItem.FeatureType.Decode && !Files.isRegularFile(Path(item.inputInfo.pathString))) return

        item.tryLoadFooter(item.inputInfo.pathString, true)
        item.loadFileTypeAndOriginalExtension(true)

        if (isAvailableFileItem(item, type)) _availableItems.add(item)

        updateItems(item, UpdateItemsFlag.Add)
    }

    fun <T> any(value: T, vararg values: StatusType): Boolean {
        return values.any { it == value }
    }

    private fun cloneItems(addedItems: List<FileItem>) {
        if (any(_status.value, StatusType.Loading, StatusType.Processing)) return

        updateStatus(StatusType.Loading)

        fun progress(_newStatus: StatusType) {
            if (_newStatus == StatusType.Loaded) {
//                AppUtils.getHistoryFile()?.let {
//                    JsonUtils.save(
//                        _items.map { item -> LiteFileSystemItem(item.inputFileOrFolderPath) },
//                        it.canonicalPath
//                    )
//                }
            }
            updateStatus(_newStatus)
        }

        fun itemProgress(_newItem: FileItem) {
            updateItems(_newItem, UpdateItemsFlag.Add)

            if (isAvailableFileItem(_newItem)) {
                _availableItems.add(_newItem)
            }
        }

        try {
            _items.clear()
            _availableItems.clear()

            for (item in addedItems) {
                try {
                    itemProgress(item)
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            progress(StatusType.Loaded)
        } catch (e: Exception) {
            progress(StatusType.LoadFailed)
        }
    }

    fun loadBatchingFiles(
        addedItems: List<FileItem>, type: FileItem.FeatureType
    ) {
        updateStatus(StatusType.Init)

        val files =
            if (type == FileItem.FeatureType.Encode) addedItems.filter { it.inputInfo.exists() && it.itemType == FileItem.FileItemType.Normal }
            else addedItems.filter { it.inputInfo.exists() && it.itemType != FileItem.FileItemType.Normal }

        cloneItems(files)
    }

    fun updateOne(item: FileItem) {
        val clonedItem = item.clone()

        val itemIndex = _items.indexOfFirst { it.inputInfo.pathString == item.inputInfo.pathString }
        if (itemIndex > -1) _items[itemIndex] = clonedItem

        val availableIndex =
            _availableItems.indexOfFirst { it.inputInfo.pathString == item.inputInfo.pathString }
        if (availableIndex > -1) _availableItems[availableIndex] = clonedItem
    }

    fun removeOne(file: FileItem) {
        updateItems(file, UpdateItemsFlag.Delete)
    }

    fun removeMany(mode: RemoveMode) {
        when (mode) {
            RemoveMode.All -> {
                _items.removeIf { true }
                _availableItems.removeIf { true }
            }

            RemoveMode.Selected -> {
                _items.removeIf { it.isSelected }
                _availableItems.removeIf { it.isSelected }
            }

            RemoveMode.NonExisted -> {
                _items.removeIf { !it.inputInfo.exists() }
                _availableItems.removeIf { !it.inputInfo.exists() }
            }
        }

        if (_status.value != StatusType.Loading) {
            _processAllButtonVisible.value = true
            _resumePauseStopButtonsPanelVisible.value = false
        }
    }

    suspend fun moveSelectedToDocuments(keepSource: Boolean) {
        val selectedItems = _items.filter { it.isSelected }

        for (item in selectedItems) {
            moveToDocuments(item, keepSource)
        }
    }

    fun removeSelected() {
        _items.removeIf { it.isSelected }
    }

    suspend fun deleteSelectedPermanently() {
        val selectedItems = _items.filter { it.isSelected }

        for (item in selectedItems) {
            deletePermanently(item)
        }
    }

    fun selectItem(item: FileItem) {
        if (_selectMode.value) {
            item.isSelected = !item.isSelected
            updateOne(item)

            if (!item.isSelected) {
                val remainingSelectedItems = items.value.filter { it.isSelected }
                if (remainingSelectedItems.isEmpty()) {
                    setSelectMode(false)
                }
            }
        } else {
            setSelectMode(true)
            item.isSelected = true
            updateOne(item)
        }
    }

    suspend fun moveToDocuments(
        item: FileItem, keepSource: Boolean
    ): Boolean {
        val documentFolderPath =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            ) else File(
                Path(Environment.getExternalStorageDirectory().canonicalPath).resolve("Documents")
                    .toString()
            )

        if (!Files.exists(documentFolderPath.toPath())) {
            withContext(Dispatchers.IO) {
                Files.createDirectories(documentFolderPath.toPath())
            }
        }

        val inputFolderPath =
            if (item.inputInfo.isRegularFile()) item.inputInfo.parent.pathString else item.inputInfo.pathString
        if (documentFolderPath.canonicalPath.equals(inputFolderPath)) return true

        var destinationPath =
            Path(documentFolderPath.canonicalPath, Path(item.inputInfo.pathString).name).toString()
        destinationPath = FileUtils.nextAvailableFileNameAdvanced(destinationPath)

        var result: Boolean

        withContext(Dispatchers.IO) {
            result = try {
                if (keepSource) {
                    Files.copy(
                        Path(item.inputInfo.pathString),
                        Path(destinationPath),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                    )
                } else {
                    Files.move(
                        Path(item.inputInfo.pathString),
                        Path(destinationPath),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )

                    item.inputInfo = Path(destinationPath)
                    updateOne(item)
                }

                item.inputInfo = Path(destinationPath)
                updateOne(item)

                true
            } catch (e: Exception) {
                false
            }
        }

        return result
    }

    suspend fun deletePermanently(item: FileItem): Boolean {
        val itemPath = Path(item.inputInfo.pathString)

        if (!Files.exists(itemPath)) return true

        var result = false

        withContext(Dispatchers.IO) {
            if (Files.isDirectory(itemPath)) {
                result = FileUtils.deleteRecursively(itemPath.toString())
            } else if (Files.isRegularFile(itemPath)) {
                result = Files.deleteIfExists(itemPath)
            }
        }

        if (result) removeOne(item)

        return result
    }

    private suspend fun processAll(context: Context, isResuming: Boolean = false) {
        if (any(
                _status.value, StatusType.Loading, StatusType.Processing
            ) || _availableItems.size == 0
        ) return

        updateStatus(StatusType.Processing)

        _argv = prepareArgv()

        fun itemProgress(itemIndex: Int, itemStatus: FileSystemItem.S, itemError: String) {
            val item = _availableItems[itemIndex]
            item.status.set(if (itemStatus == FileSystemItem.S.ProcessInQueue) FileSystemItem.S.Processing else itemStatus)

            when (_status.value) {
                StatusType.Paused -> {
                    for (index in (itemIndex + 1) until _availableItems.size) {
                        val newAvailableItem = _availableItems[index]
                        newAvailableItem.status.set(FileSystemItem.S.ProcessPaused)
                        updateOne(newAvailableItem)
                    }
                }

                StatusType.Stopped -> {
                    for (index in (itemIndex + 1) until _availableItems.size) {
                        val newAvailableItem = _availableItems[index]
                        newAvailableItem.status.set(FileSystemItem.S.ProcessStopped)
                        updateOne(newAvailableItem)
                    }
                }

                StatusType.Processed -> {
                    item.moveOutputPathToInputPath()
                    item.tryLoadFooter(item.inputInfo.pathString, true)
                    item.isEnabled = true
                    updateOne(item)

                    _fileManagerViewModel?.let {
                        val mainItem =
                            _fileManagerViewModel!!.items.value.find { it.inputInfo.pathString == item.inputInfo.pathString }
                        if (mainItem != null) {
                            _fileManagerViewModel!!.updateOne(item)
                        }
                    }
                }

                else -> {}
            }

            when (itemStatus) {
                FileSystemItem.S.Processed -> {
                    item.moveOutputPathToInputPath()
                    item.tryLoadFooter(item.inputInfo.pathString, true)
                    item.isEnabled = true
                    updateOne(item)

                    _fileManagerViewModel?.let {
                        val mainItem =
                            _fileManagerViewModel!!.items.value.find { it.inputInfo.pathString == item.inputInfo.pathString }
                        if (mainItem != null) {
                            _fileManagerViewModel!!.updateOne(item)
                        }
                    }
                }

                FileSystemItem.S.ProcessFailed -> {
                    item.isEnabled = true
                    updateOne(item)

                    _fileManagerViewModel?.let {
                        val mainItem =
                            _fileManagerViewModel!!.items.value.find { it.inputInfo.pathString == item.inputInfo.pathString }
                        if (mainItem != null) {
                            _fileManagerViewModel!!.updateOne(item)
                        }
                    }
                }

                else -> {}
            }
        }

        if (!isResuming) {
            for (index in 0 until _availableItems.size) {
                val item = _availableItems[index]
                item.status.set(FileSystemItem.S.ProcessInQueue)
                updateOne(item)
            }
        }

        for (index in 0 until _availableItems.size) {
            val item = _availableItems[index]

            if (item.status.not(FileSystemItem.S.ProcessInQueue)) continue

            itemProgress(index, FileSystemItem.S.Processing, "")

            var error = String()

            if (_argv!!.password.isEmpty()) error = context.getString(R.string.no_password_provided)

            if (error.isNotEmpty()) {
                itemProgress(index, FileSystemItem.S.ProcessFailed, error)
            } else {
                try {
                    if (_featureType.value == FileItem.FeatureType.Encode) Share.encodeOne(
                        _argv!!, item
                    )
                    else if (_featureType.value == FileItem.FeatureType.Decode) Share.decodeOne(
                        _argv!!, item
                    )

                    itemProgress(index, FileSystemItem.S.Processed, String())
                } catch (e: IOException) {
                    itemProgress(
                        index, FileSystemItem.S.ProcessFailed, "File or folder does not exist."
                    )
                } catch (e: Exception) {
                    itemProgress(index, FileSystemItem.S.ProcessFailed, "Password incorrect.")
                }
            }

            if (index == _availableItems.size - 1) break

            when (_status.value) {
                StatusType.Pausing -> {
                    updateStatus(StatusType.Paused)
                    return
                }

                StatusType.Stopping -> {
                    updateStatus(StatusType.Stopped)
                    return
                }

                else -> {}
            }
        }

        updateStatus(StatusType.Processed)
    }

    fun reset(context: Context) {
        _argv = null
        _items.clear()
        _availableItems.clear()
        _featureType.value = FileItem.FeatureType.Encode
        _status.value = StatusType.Init
        _password.value = String()

        _addFilesVisible.value = true
        _passwordEnabled.value = true
        _keepOriginalEnabled.value = true

        _processAllButtonVisible.value = true
        _processAllButtonEnabled.value = true

        _resumePauseStopButtonsPanelVisible.value = false
        _resumePauseButtonText.value = context.getString(R.string.resume)
        _resumePauseButtonVisible.value = false
        _resumePauseButtonEnabled.value = false

        _stopButtonVisible.value = false
        _stopButtonEnabled.value = false
    }

    init {
        _status.value = StatusType.Init
        enableInputControls(true)
        enableOutputControl(false)
    }
}