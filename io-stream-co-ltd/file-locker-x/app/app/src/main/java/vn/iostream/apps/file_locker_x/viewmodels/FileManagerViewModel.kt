package vn.iostream.apps.file_locker_x.viewmodels

import android.os.Build
import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import vn.iostream.apps.core.iofile.FileSystemItem
import vn.iostream.apps.core.iofile.FileUtils
import vn.iostream.apps.data.entities.FileEntity
import vn.iostream.apps.data.repositories.FileRepository
import vn.iostream.apps.file_locker_x.configs.AppTypes
import vn.iostream.apps.file_locker_x.features.Share
import vn.iostream.apps.file_locker_x.models.FileFooter
import vn.iostream.apps.file_locker_x.models.FileItem
import vn.iostream.apps.file_locker_x.utils.BitUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.pathString

@HiltViewModel
class FileManagerViewModel @Inject constructor(private val fileRepository: FileRepository) :
    ViewModel() {
    enum class StatusType {
        Init, Loading, Loaded, LoadFailed, Processing, Processed, ProcessFailed
    }

    enum class RemoveMode {
        All, Selected, NonExisted
    }

    private var _argv: Share.Argv? = null

    private var _status = MutableStateFlow(StatusType.Init)
    val status = _status.asStateFlow()

    private val itemsLocker = ReentrantLock()

    val SOURCE_ITEMS = mutableStateListOf<FileItem>()

    private val _items = mutableStateListOf<FileItem>()
    val items = MutableStateFlow(_items)

    private var _searchKeyword = MutableStateFlow(String())
    val searchKeyword = _searchKeyword.asStateFlow()

    private val _sortOrder = MutableStateFlow(AppTypes.SortType.AZ)
    val sortOrder = _sortOrder.asStateFlow()

    //

    private var _currentItem = MutableStateFlow<FileItem?>(null)
    var currentItem = _currentItem.asStateFlow()

    fun setCurrentItem(newItem: FileItem?) {
        _currentItem.value = newItem
    }

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

    private var _keepOriginalFileEnabled = MutableStateFlow(true)
    val keepOriginalEnabled = _keepOriginalFileEnabled.asStateFlow()

    private var _overwriteExistingEnabled = MutableStateFlow(true)
    val overwriteExistingEnabled = _overwriteExistingEnabled.asStateFlow()

    /*
    * Other controls
    */

    private var _isSelecting = MutableStateFlow(false)
    val isSelecting = _isSelecting.asStateFlow()

    /*
    * Methods
    * */

    fun <T> any(value: T, vararg values: StatusType): Boolean {
        return values.any { it == value }
    }

    private fun prepareArgv() =
        Share.Argv(_password.value, _keepOriginal.value, _overwriteExisting.value)

    fun sortAZ() {
        val sorted = _items.sortedBy { it.inputInfo.name }
        _items.clear()
        _items.addAll(sorted)
    }

    fun sortZA() {
        val sorted = _items.sortedByDescending { it.inputInfo.name }
        _items.clear()
        _items.addAll(sorted)
    }

    fun sortInit() {
        _items.clear()
        _items.addAll(SOURCE_ITEMS)
    }

    fun sortMostRecent() {
        val sorted = _items.sortedByDescending { it.inputInfo.getLastModifiedTime().toMillis() }
        _items.clear()
        _items.addAll(sorted)
    }

    fun sortLeastRecent() {
        val sorted = _items.sortedBy { it.inputInfo.getLastModifiedTime().toMillis() }
        _items.clear()
        _items.addAll(sorted)
    }

    private var _renameDialogVisible = MutableStateFlow(false)
    val renameDialogVisible = _renameDialogVisible.asStateFlow()

    fun setRenameDialogVisible(value: Boolean) {
        _renameDialogVisible.value = value
    }

    private var _renameValue = MutableStateFlow(String())
    val renameValue = _renameValue.asStateFlow()

    fun setRenameValue(newName: String) {
        _renameValue.value = newName
    }

    fun applyFilter() {
        val filteredItems = mutableListOf<FileItem>()

        if (_searchKeyword.value.isEmpty()) {
            filteredItems.addAll(SOURCE_ITEMS)
        } else {
            for (item in SOURCE_ITEMS) {
                if (item.inputInfo.name.contains(_searchKeyword.value, ignoreCase = true)) {
                    filteredItems.add(item)
                }
            }
        }

        when (_sortOrder.value) {
            AppTypes.SortType.AZ -> {
                filteredItems.sortBy { it.inputInfo.name }
            }

            AppTypes.SortType.ZA -> {
                filteredItems.sortByDescending { it.inputInfo.name }
            }

            AppTypes.SortType.Oldest -> {
                filteredItems.sortBy {
                    Files.readAttributes(it.inputInfo, BasicFileAttributes::class.java)
                        .creationTime().toMillis()
                }
            }

            AppTypes.SortType.Newest -> {
                filteredItems.sortByDescending {
                    Files.readAttributes(it.inputInfo, BasicFileAttributes::class.java)
                        .creationTime().toMillis()
                }
            }
        }

        _items.clear()
        _items.addAll(filteredItems)
    }

    private fun enableInputControls(value: Boolean) {
        _addFilesVisible.value = value
    }

    private fun enableOutputControl(value: Boolean) {
        _passwordEnabled.value = value
        _keepOriginalFileEnabled.value = value
    }

    fun updateStatus(value: StatusType) {
        _status.value = value

        when (_status.value) {
            StatusType.Init -> {
                enableInputControls(true)
                enableOutputControl(false)
            }

            StatusType.Loaded -> {
                enableInputControls(true)
                enableOutputControl(_currentItem.value != null)

                if (_currentItem.value != null) {
                    _currentItem.value!!.isEnabled = true
                    updateFileItem(_currentItem.value)
                }
            }

            StatusType.Processing -> {
                enableInputControls(false)
                enableOutputControl(false)

                if (_currentItem.value != null) {
                    _currentItem.value!!.isEnabled = false
                    updateFileItem(_currentItem.value)
                }
            }

            StatusType.Processed -> {
                enableInputControls(true)
                enableOutputControl(true)

                if (_currentItem.value != null) {
                    _currentItem.value!!.isEnabled = true
                    updateFileItem(_currentItem.value)
                }
            }

            StatusType.ProcessFailed -> {
                enableInputControls(true)
                enableOutputControl(true)

                if (_currentItem.value != null) {
                    _currentItem.value!!.isEnabled = true
                    updateFileItem(_currentItem.value)
                }
            }

            else -> {}
        }
    }

    fun setSearchKeyword(value: String) {
        _searchKeyword.value = value
    }

    fun setSortOrder(value: AppTypes.SortType) {
        _sortOrder.value = value
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
        _isSelecting.value = value

        if (!value) {
            for (index in SOURCE_ITEMS.indices) {
                val i = SOURCE_ITEMS[index]
                i.isSelected = false
                updateOne(i)
            }
        }
    }

    suspend fun addOne(newItem: FileItem) {
        val existingEntities = mutableListOf<FileEntity>()

        withContext(Dispatchers.IO) {
            existingEntities.addAll(fileRepository.getByPath(newItem.inputInfo.pathString))
        }

        if (existingEntities.isNotEmpty()) {
            val existingItem =
                SOURCE_ITEMS.find { it.inputInfo.pathString == newItem.inputInfo.pathString }

            if (existingItem != null) {
                updateOne(existingItem)
            }
        } else {
            newItem.tryLoadFooter(newItem.inputInfo.pathString, true)

            if (newItem.inputInfo.pathString.isNotEmpty()) {
                SOURCE_ITEMS.add(newItem)

                withContext(Dispatchers.IO) {
                    newItem.id = fileRepository.insert(FileEntity(newItem.inputInfo.pathString))
                }

                applyFilter()
            }
        }
    }

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            addFiles(fileRepository.getAll().map { item -> FileItem(item.id, item.path) })
        }
    }

    fun addFiles(items: List<FileItem>) {
        if (_status.value == StatusType.Loading || items.isEmpty()) return

        updateStatus(StatusType.Loading)

        try {
            itemsLocker.lock()

            for (item in items) {
                val filePath = Path(item.inputInfo.pathString)

                if (!filePath.exists()) continue

                val duplicateItem =
                    SOURCE_ITEMS.find { it.inputInfo.pathString == item.inputInfo.pathString }
                if (duplicateItem != null) {
                    updateOne(duplicateItem)
                    continue
                }

                item.tryLoadFooter(item.inputInfo.pathString, true)
                item.loadFileTypeAndOriginalExtension(true)

                SOURCE_ITEMS.add(item)
            }

            applyFilter()

            updateStatus(StatusType.Loaded)
        } catch (e: Exception) {
            updateStatus(StatusType.LoadFailed)
        } finally {
            itemsLocker.unlock()
        }
    }

    fun updateOne(file: FileItem) {
        val index = SOURCE_ITEMS.indexOfFirst { it.id == file.id }

        if (index > -1) {
            SOURCE_ITEMS[index] = file.clone()
            applyFilter()
            //return true
        }
        //return false
    }

    suspend fun removeOne(file: FileItem) {
        withContext(Dispatchers.IO) {
            val affectedRows = fileRepository.deleteByPath(file.inputInfo.pathString)
            if (affectedRows > 0) SOURCE_ITEMS.remove(file)
        }

        applyFilter()
    }

    suspend fun removeMany(mode: RemoveMode) {
        when (mode) {
            RemoveMode.All -> {
                SOURCE_ITEMS.clear()
                withContext(Dispatchers.IO) {
                    fileRepository.deleteAll()
                }
            }

            RemoveMode.Selected -> {
                val filteredItems = SOURCE_ITEMS.filter { item -> item.isSelected }
                withContext(Dispatchers.IO) {
                    filteredItems.forEach { item -> fileRepository.deleteByPath(item.inputInfo.pathString) }
                }
                SOURCE_ITEMS.removeAll(filteredItems)
            }

            RemoveMode.NonExisted -> {
                val filteredItems = SOURCE_ITEMS.filter { item -> !item.inputInfo.exists() }
                withContext(Dispatchers.IO) {
                    filteredItems.forEach { item -> fileRepository.deleteByPath(item.inputInfo.pathString) }
                }
                SOURCE_ITEMS.removeAll(filteredItems)
            }
        }

        applyFilter()
    }

    fun updateFileItem(updatedItem: FileItem?) {
        _currentItem.value = updatedItem
        if (updatedItem != null) {
            _currentItem.value = updatedItem.clone()
            updateOne(updatedItem)
        }
    }

    suspend fun moveSelectedToDocuments(keepSource: Boolean) {
        val selectedItems = SOURCE_ITEMS.filter { it.isSelected }

        for (item in selectedItems) {
            moveToDocuments(item, keepSource)
        }
    }

    suspend fun deleteSelectedPermanently() {
        val selectedItems = SOURCE_ITEMS.filter { it.isSelected }

        for (item in selectedItems) {
            deletePermanently(item)
        }
    }

    fun selectItem(item: FileItem) {
        if (_isSelecting.value) {
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


    suspend fun moveToDocuments(item: FileItem, keepSource: Boolean): Boolean {
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
                }
                val previous = item.inputInfo.pathString
                item.inputInfo = Path(destinationPath)

                fileRepository.getAll().forEach {
                    it.path = destinationPath
                    val affectedRows = fileRepository.update(it)
                    if (affectedRows > 0) updateOne(item)
                }

                true
            } catch (e: Exception) {
                false
            }
        }

        return result
    }

    suspend fun deletePermanently(item: FileItem): Boolean {
        val itemPath = item.inputInfo

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

    suspend fun renameFile(
        item: FileItem, newName: String, changeOriginalNameInFooter: Boolean
    ): Boolean {
        val newFilePath = Path(item.inputInfo.parent.pathString, newName)

        if (changeOriginalNameInFooter) {
            val nameArrayBytes = BitUtils.removeAndGetLatestBytesFromFile(
                item.inputInfo.toString(), item.inputInfo.nameCount.toLong()
            )
            val instanceFooter = FileFooter.create(nameArrayBytes)
            if (instanceFooter != null) {
                instanceFooter.metadata?.let {
                    it.OriginalName = newFilePath.pathString
                    FileFooter.appendToFile(
                        item.inputInfo.toString(), it, instanceFooter.footerThumbnailBuffer
                    )
                }
            }
        }

        val oldFile = item.inputInfo.toFile()
        val newFile = newFilePath.toFile()
        item.inputInfo = newFilePath
        withContext(Dispatchers.IO) {
            val itemEntity = fileRepository.getById(item.id)
            itemEntity.path = newFilePath.pathString
            fileRepository.update(itemEntity)
        }
        return oldFile.renameTo(newFile)
    }

    suspend fun processFile(): Boolean {
        if (_currentItem.value == null) return false

        updateStatus(StatusType.Processing)
        _argv = prepareArgv()

        if (_argv!!.password.isEmpty()) {
            updateStatus(StatusType.ProcessFailed)
            return false
        }

        val fileIndex =
            SOURCE_ITEMS.indexOfFirst { it.inputInfo.pathString == _currentItem.value!!.inputInfo.pathString }
        if (fileIndex == -1) {
            updateStatus(StatusType.ProcessFailed)
            return false
        }

        val file = SOURCE_ITEMS[fileIndex]

        try {
            file.status.set(FileSystemItem.S.Processing)
            updateFileItem(file)

            if (file.itemType == FileItem.FileItemType.Normal) Share.encodeOne(_argv!!, file)
            else Share.decodeOne(_argv!!, file)

            file.status.set(FileSystemItem.S.Processed)
            file.moveOutputPathToInputPath()

            updateStatus(StatusType.Processed)

            return true
        } catch (e: Exception) {
            file.status.set(FileSystemItem.S.ProcessFailed)
            updateStatus(StatusType.ProcessFailed)

            return false
        } finally {
            file.isEnabled = true
            updateOne(file)
            updateFileItem(file)
        }
    }

    init {
        updateStatus(StatusType.Loaded)
    }
}

