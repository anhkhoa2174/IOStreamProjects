package vn.iostream.apps.file_locker_x.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import vn.iostream.apps.core.iofile.FileSystemItem
import vn.iostream.apps.core.iofile.FileUtils
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.pathString

@Serializable
class FileItem() : FileSystemItem() {
    enum class FileItemType {
        Normal, EncodedFile, EncodedFolder
    }

    enum class FeatureType {
        Encode, Decode
    }

    private var _outputInfo: String = String()
    var outputInfo: Path
        get() = Path(_outputInfo)
        set(value) {
            _outputInfo = value.pathString
        }

    var id: Long = 0

    var footer: FileFooter? = null
        get
        protected set

    val isLocked get() = footer != null

    var itemType: FileItemType = FileItemType.Normal
        get
        protected set

    var originalExtension: String = String()
        get
        protected set

    var isAvailable: Boolean = true

    protected var _thumbnailPath: String = String()
    var thumbnailPath: String
        get() = _thumbnailPath
        set(value) {
            _thumbnailPath = value
        }

    var cacheImagePath: String = String()

    var tempThumbnailPath: String = String()

    companion object {
        fun create(inputPath: String): FileItem {
            val item = FileItem(inputPath)
            item.tryLoadFooter(inputPath, false)
            return item
        }
    }

    fun updateAndGetIsAvailable(featureType: FileItem.FeatureType, raise: Boolean): Boolean {
        isAvailable =
            (featureType == FileItem.FeatureType.Encode && itemType == FileItem.FileItemType.Normal) || (featureType == FileItem.FeatureType.Decode && itemType != FileItem.FileItemType.Normal)

        return isAvailable
    }

    constructor(path: String) : this() {
        _inputInfo = path
    }

    constructor(_id: Long, path: String) : this() {
        id = _id
        _inputInfo = path
    }

    fun tryLoadFooter(path: String, reload: Boolean): Boolean {
        if (footer != null && !reload) return true

        val footer = FileFooter.create(path)
        if (footer != null) {
            this.footer = footer
            _fileType = footer.metadata!!.FileType
        }

        return this.footer != null
    }

    fun moveOutputPathToInputPath() {
        inputInfo = Path(outputInfo.pathString)
        loadFileTypeAndOriginalExtension(true)
    }

    fun loadFileTypeAndOriginalExtension(raise: Boolean) {
        _fileType = FileUtils.getTypeByExtension(inputInfo.extension)
        itemType = FileItemType.Normal
        originalExtension = String()

        if (inputInfo.isRegularFile()) {
            val footer = FileFooter.create(inputInfo.pathString)
            if (footer?.metadata != null) {
                _fileType = FileUtils.getTypeByExtension(footer.metadata!!.OriginalName)

                if (footer.metadata!!.Version >= 2) {
                    itemType =
                        if (footer.metadata!!.FileType != FileUtils.Type.Directory) FileItemType.EncodedFile
                        else FileItemType.EncodedFolder
                } else {
                    itemType = if (footer.metadata!!.IsFile) FileItemType.EncodedFile
                    else FileItemType.EncodedFolder
                }

                originalExtension = Path(footer.metadata!!.OriginalName).extension
            } else {
                originalExtension = inputInfo.extension
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun clone(): FileItem {
        val binaryData = ProtoBuf.encodeToByteArray(serializer(), this)
        return ProtoBuf.decodeFromByteArray(serializer(), binaryData)
    }
}
