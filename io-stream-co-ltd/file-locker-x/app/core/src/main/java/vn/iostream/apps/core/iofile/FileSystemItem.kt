package vn.iostream.apps.core.iofile

import kotlinx.serialization.Serializable
import vn.iostream.apps.core.IOItem
import vn.iostream.apps.core.IOStatus
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path
import kotlin.io.path.pathString

@Serializable
open class FileSystemItem() : IOItem() {
    enum class S {
        Ready,

        ProcessInQueue, Processing,

        Processed, ProcessFailed,

        ProcessPaused, ProcessStopped,
    }

    protected var _inputInfo: String = String()
    var inputInfo: Path
        get() = Path(_inputInfo)
        set(value) {
            _inputInfo = value.pathString
        }

    val creationTime: Long?
        get() {
            if (_inputInfo.isEmpty()) return null

            val attrs = Files.readAttributes(inputInfo, BasicFileAttributes::class.java)
            return attrs.creationTime().toMillis()
        }

    var status: IOStatus<S> = IOStatus.initialize<S> { }

    protected var _fileType: FileUtils.Type = FileUtils.Type.Unknown
    var FileType: FileUtils.Type
        get() = _fileType
        set(value) {
            _fileType = value
        }

    protected var _messageText: String = String()
    var MessageText: String
        get() = _messageText
        set(value) {
            _messageText = value
        }

    fun setFileType(fileType: FileUtils.Type, notify: Boolean) {
        _fileType = fileType
    }

    fun setMessage(error: String, notify: Boolean) {
        _messageText = error
    }

    constructor(path: String) : this() {
        _inputInfo = path
        _fileType = FileUtils.getType(path)
    }
}