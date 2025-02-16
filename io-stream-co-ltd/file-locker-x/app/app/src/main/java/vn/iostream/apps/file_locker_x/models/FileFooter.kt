 package vn.iostream.apps.file_locker_x.models

import com.google.gson.Gson
import kotlinx.serialization.Serializable
import vn.iostream.apps.core.IOSize
import vn.iostream.apps.core.iofile.FileUtils
import vn.iostream.apps.file_locker_x.configs.Constants
import vn.iostream.apps.file_locker_x.utils.BitUtils
import vn.iostream.apps.file_locker_x.utils.CryptographyUtils
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString
import kotlin.math.max

@Serializable
class FooterExtra {
    var Dimension: IOSize<Int>? = null

    var Size: Long = 0
    var CreationTime: Long = 0
    var LastWriteTime: Long = 0
    var IsThumbnailEncrypted: Boolean = false

    fun toJSON(): String = Gson().toJson(this)

    companion object {
        fun isEmpty(extra: FooterExtra): Boolean = extra.Dimension == null && extra.Size == 0L
    }
}

@Serializable
class FooterMetadata {
    var Version: Int = 1
    var Platform: String = "Android"
    var IsFile: Boolean = false
    var FileType: FileUtils.Type = FileUtils.Type.Unknown
    var OriginalName: String = String()
    var ThumbnailSize: Int = 0
    var Extras: String = String()

    constructor(fileType: FileUtils.Type, originalName: String, extra: FooterExtra) {
        IsFile = fileType != FileUtils.Type.Directory
        FileType = fileType
        OriginalName = originalName
        Extras = extra.toJSON()
    }
}

class MetadataProxy {
    var Version: Int = 1
    var Platform: String = "Android"
    var IsFile: Boolean = false
    var FileType: Int = 0
    var OriginalName: String = String()
    var ThumbnailSize: Int = 0
    var Extras: String = String()
}

@Serializable
class FileFooter {
    companion object {
        const val SIGNATURE = "63f97fd0fe4081f2d8fea920"

        fun create(): FileFooter = FileFooter()

        fun create(byteArray: ByteArray): FileFooter? {
            val footer = FileFooter()
            if (footer.load(byteArray)) return footer
            return null
        }

        fun create(path: String): FileFooter? {
            val footer = FileFooter()
            if (footer.load(path)) return footer
            return null
        }

        fun isLockedFile(byteArray: ByteArray): Boolean = create(byteArray) != null

        fun isLockedFile(path: String): Boolean = create(path) != null

        @OptIn(ExperimentalEncodingApi::class)
        fun appendToFile(filePath: String, metadata: FooterMetadata, thumbnailBytes: ByteArray?) {
            thumbnailBytes?.let {
                metadata.ThumbnailSize = it.size
            }

            val fos = FileOutputStream(filePath, true)

            val metadataB64Str = Base64.encode(Gson().toJson(metadata).toByteArray(Charsets.UTF_8))
            val metadataEncryptedBuffer = CryptographyUtils.encrypt(
                metadataB64Str,
                Constants.Token,
                Constants.Salt,
                Constants.Iterations,
                Constants.KeyLength
            )

            try {
                thumbnailBytes?.let {
                    if (it.isNotEmpty()) fos.write(it)
                }
                fos.write(metadataEncryptedBuffer)

                val footerMetadataSize = ByteArray(4)
                BitUtils.write4BytesToBuffer(
                    footerMetadataSize, 0, metadataEncryptedBuffer.size
                )
                fos.write(footerMetadataSize)

                fos.write(SIGNATURE.toByteArray(Charsets.UTF_8))

                fos.flush()
            } catch (e: Exception) {
                throw e
            } finally {
                fos.close()
            }
        }
    }

    var signature = SIGNATURE.toByteArray(Charsets.UTF_8)
    var metaSize: Int = 0
    var metadata: FooterMetadata? = null
        get
    var extra: FooterExtra? = null
        get
        private set

    var metaSizeBuffer: ByteArray = byteArrayOf()
    var footerMetadataBuffer: ByteArray = byteArrayOf()
    var footerThumbnailBuffer: ByteArray = byteArrayOf()

    private fun loadSignature(raf: RandomAccessFile): Boolean {
        try {
            raf.seek(0L.coerceAtLeast(raf.length() - signature.size))
            raf.read(signature, 0, signature.size)

            return signature.isNotEmpty() && signature.toString(Charsets.UTF_8) == SIGNATURE
        } catch (e: Exception) {
            return false
        }
    }

    private fun loadSignature(byteArray: ByteArray): Boolean {
        try {
            signature = ByteArray(SIGNATURE.length)
            byteArray.copyInto(signature, 0, byteArray.size - signature.size, byteArray.size)

            return signature.isNotEmpty() && signature.toString(Charsets.UTF_8) == SIGNATURE
        } catch (e: Exception) {
            return false
        }
    }

    private fun loadMetaSize(raf: RandomAccessFile): Boolean {
        try {
            metaSizeBuffer = ByteArray(Int.SIZE_BYTES)

            raf.seek(max(0L, raf.length() - signature.size - metaSizeBuffer.size))
            val byteRead = raf.read(metaSizeBuffer, 0, metaSizeBuffer.size)

            if (byteRead != metaSizeBuffer.size) return false

            val byteBuffer = ByteBuffer.wrap(metaSizeBuffer)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            metaSize = byteBuffer.int

            return metaSize > 0
        } catch (e: Exception) {
            return false
        }
    }

    private fun loadMetaSize(byteArray: ByteArray): Boolean {
        try {
            metaSizeBuffer = ByteArray(Int.SIZE_BYTES)

            byteArray.copyInto(
                metaSizeBuffer,
                0,
                byteArray.size - signature.size - metaSizeBuffer.size,
                byteArray.size - signature.size
            )

            val byteBuffer = ByteBuffer.wrap(metaSizeBuffer)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            metaSize = byteBuffer.int

            return metaSize > 0
        } catch (e: Exception) {
            return false
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun loadMetadata(raf: RandomAccessFile): Boolean {
        try {
            footerMetadataBuffer = ByteArray(metaSize)

            raf.seek(
                max(
                    0L,
                    raf.length() - signature.size - metaSizeBuffer.size - footerMetadataBuffer.size
                )
            )
            val byteRead = raf.read(footerMetadataBuffer, 0, footerMetadataBuffer.size)
            if (byteRead != footerMetadataBuffer.size) return false

            var metadataStr: String

            try {
                val metadataB64Buffer = CryptographyUtils.decrypt(
                    footerMetadataBuffer,
                    Constants.Token,
                    Constants.Salt,
                    Constants.Iterations,
                    Constants.KeyLength
                )
                val metadataBuffer = Base64.decode(metadataB64Buffer)

                metadataStr = metadataBuffer.toString(Charsets.UTF_8)
                metadata = Gson().fromJson(
                    metadataBuffer.toString(Charsets.UTF_8), FooterMetadata::class.java
                )

            } catch (e: Exception) {
                metadataStr = footerMetadataBuffer.toString(Charsets.UTF_8)
                metadata = Gson().fromJson(
                    footerMetadataBuffer.toString(Charsets.UTF_8), FooterMetadata::class.java
                )

                metadata?.let {
                    if (it.FileType == null) {
                        val metadataProxy = Gson().fromJson(metadataStr, MetadataProxy::class.java)

                        if (metadata != null && metadataProxy != null) {
                            metadata!!.FileType = FileUtils.Type.fromInt(metadataProxy.FileType)
                            metadata!!.IsFile = metadata!!.FileType != FileUtils.Type.Directory
                        }
                    }
                }

            }

            return metadata != null
        } catch (e: Exception) {
            return false
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun loadMetadata(byteArray: ByteArray): Boolean {
        try {
            footerMetadataBuffer = ByteArray(metaSize)

            byteArray.copyInto(
                footerMetadataBuffer,
                0,
                byteArray.size - signature.size - metaSizeBuffer.size - footerMetadataBuffer.size,
                byteArray.size - signature.size - metaSizeBuffer.size
            )

            var metadataStr: String

            try {
                val metadataB64Buffer = CryptographyUtils.decrypt(
                    footerMetadataBuffer,
                    Constants.Token,
                    Constants.Salt,
                    Constants.Iterations,
                    Constants.KeyLength
                )
                val metadataBuffer = Base64.decode(metadataB64Buffer)

                metadataStr = metadataBuffer.toString(Charsets.UTF_8)
                metadata = Gson().fromJson(metadataStr, FooterMetadata::class.java)

            } catch (e: Exception) {
                metadataStr = footerMetadataBuffer.toString(Charsets.UTF_8)
                metadata = Gson().fromJson(metadataStr, FooterMetadata::class.java)
            }

            val metadataProxy = Gson().fromJson(metadataStr, MetadataProxy::class.java)

            if (metadata != null && metadataProxy != null) {
                metadata!!.FileType = FileUtils.Type.fromInt(metadataProxy.FileType)
                metadata!!.IsFile = metadata!!.FileType != FileUtils.Type.Directory
            }

            return metadata != null
        } catch (e: Exception) {
            return false
        }
    }

    private fun loadThumbnail(raf: RandomAccessFile): Boolean {
        if (metadata == null) return false

        if (metadata!!.ThumbnailSize == 0) return true
        footerThumbnailBuffer = ByteArray(metadata!!.ThumbnailSize)

        try {
            raf.seek(raf.length() - signature.size - metaSizeBuffer.size - footerMetadataBuffer.size - footerThumbnailBuffer.size)

            val byteRead = raf.read(footerThumbnailBuffer, 0, footerThumbnailBuffer.size)
            return byteRead == footerThumbnailBuffer.size
        } catch (e: Exception) {
            return false
        }
    }

    private fun loadThumbnail(byteArray: ByteArray): Boolean {
        if (metadata == null) return false

        if (metadata!!.ThumbnailSize == 0) return true
        footerThumbnailBuffer = ByteArray(metadata!!.ThumbnailSize)

        try {
            byteArray.copyInto(
                footerThumbnailBuffer,
                0,
                byteArray.size - signature.size - metaSizeBuffer.size - footerMetadataBuffer.size - footerThumbnailBuffer.size,
                byteArray.size - signature.size - metaSizeBuffer.size - footerMetadataBuffer.size
            )

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun load(filePath: String): Boolean {
        val path = Path(filePath)

        if (!path.exists() || path.isDirectory()) return false

        val raf = RandomAccessFile(path.pathString, "r")

        if (!loadSignature(raf)) return false
        if (!loadMetaSize(raf)) return false
        if (!loadMetadata(raf)) return false
        if (!loadThumbnail(raf)) return false

        return true
    }

    fun load(byteArray: ByteArray): Boolean {
        if (byteArray.isEmpty()) return false

        if (!loadSignature(byteArray)) return false
        if (!loadMetaSize(byteArray)) return false
        if (!loadMetadata(byteArray)) return false
        if (!loadThumbnail(byteArray)) return false

        return true
    }

    fun getSize(): Long =
        (signature.size + metaSizeBuffer.size + footerMetadataBuffer.size + footerThumbnailBuffer.size).toLong()
}



