package vn.iostream.apps.file_locker_x.features

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.iostream.apps.core.iofile.FileUtils
import vn.iostream.apps.core.iofile.MediaUtils
import vn.iostream.apps.file_locker_x.configs.Constants
import vn.iostream.apps.file_locker_x.models.FileFooter
import vn.iostream.apps.file_locker_x.models.FileItem
import vn.iostream.apps.file_locker_x.models.FooterExtra
import vn.iostream.apps.file_locker_x.models.FooterMetadata
import vn.iostream.apps.file_locker_x.utils.BitUtils
import vn.iostream.apps.file_locker_x.utils.CryptographyUtils
import vn.iostream.apps.file_locker_x.utils.ZipUtils
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

class Share {
    data class Argv(
        val password: String, val keepOriginal: Boolean, val overwriteExisting: Boolean
    )

    companion object {
        suspend fun encodeOne(argv: Argv, item: FileItem) {
            if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) throw Exception()
            if (!item.inputInfo.exists()) throw Exception("File path not found.")

            val isFile = item.inputInfo.isRegularFile()

            val outputFolderPath = item.inputInfo.parent.pathString

            val tempInputFileOrFolderPath = Path(outputFolderPath, UUID.randomUUID().toString())

            val tempOutputFilePath = Path(outputFolderPath, UUID.randomUUID().toString())
            val outputZipFilePath = Path(outputFolderPath, UUID.randomUUID().toString())

            try {
                withContext(Dispatchers.IO) {
                    Files.createDirectories(Path(outputFolderPath))

                    if (isFile) {
                        Files.copy(
                            Path(item.inputInfo.pathString),
                            tempInputFileOrFolderPath,
                            StandardCopyOption.COPY_ATTRIBUTES,
                            StandardCopyOption.REPLACE_EXISTING
                        )

                        CryptographyUtils.encryptFile(
                            tempInputFileOrFolderPath,
                            tempOutputFilePath,
                        ) { throw it }
                    } else {
                        ZipUtils.createFromDirectory(
                            item.inputInfo.pathString, outputZipFilePath.toString()
                        )

                        CryptographyUtils.encryptFile(
                            outputZipFilePath,
                            tempOutputFilePath,
                        ) { throw it }
                    }

                    val footerMetadata = FooterMetadata(
                        if (isFile) FileUtils.Type.Unknown else FileUtils.Type.Directory,
                        item.inputInfo.pathString,
                        FooterExtra()
                    )


                    footerMetadata.IsFile = isFile

                    var thumbnailBytes = byteArrayOf()

                    try {
                        if (item.FileType == FileUtils.Type.Video) {
                            thumbnailBytes = MediaUtils.generateThumbnail(item.inputInfo.pathString)
                        }
                    } catch (e: Exception) {
                        Log.d("DevTag", e.message, e.cause)
                    }

                    FileFooter.appendToFile(
                        tempOutputFilePath.toString(), footerMetadata, thumbnailBytes
                    )

                    val outputName = Path(item.inputInfo.name).nameWithoutExtension
                    val outputExtension = FileUtils.getExtension(item.inputInfo.name)

                    item.outputInfo = Path(outputFolderPath, "$outputName$outputExtension")

                    if (argv.keepOriginal) item.outputInfo =
                        Path(FileUtils.nextAvailableFileNameAdvanced(item.outputInfo.pathString))

                    if (!isFile && !argv.keepOriginal) FileUtils.deleteFileOrDirectory(item.outputInfo.pathString)
                    Files.move(
                        tempOutputFilePath,
                        Path(item.outputInfo.pathString),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )
                }
            } catch (e: Exception) {
                throw e
            } finally {
                withContext(Dispatchers.IO) {
                    FileUtils.deleteFileOrDirectory(tempInputFileOrFolderPath.toString())
                    FileUtils.deleteFileOrDirectory(tempOutputFilePath.toString())
                    FileUtils.deleteFileOrDirectory(outputZipFilePath.toString())
                }
            }
        }

        suspend fun decodeOne(argv: Argv, item: FileItem) {
            if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) throw Exception()
            if (!item.inputInfo.exists()) throw Exception("File path not found.")

            val outputFolderPath = item.inputInfo.parent.pathString

            val tempInputFileOrFolderPath =
                Path(outputFolderPath, UUID.randomUUID().toString()).pathString
            val tempOutputFileOrFolderPath =
                Path(outputFolderPath, UUID.randomUUID().toString()).pathString

            try {
                val fileFooter = FileFooter.create(item.inputInfo.pathString) ?: throw Exception()

                withContext(Dispatchers.IO) {
                    Files.copy(
                        Path(item.inputInfo.pathString),
                        Path(tempInputFileOrFolderPath),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                    )

                    Files.createDirectories(Path(outputFolderPath))

                    BitUtils.removeLatestBytesFromFile(
                        tempInputFileOrFolderPath, fileFooter.getSize()
                    )

                    CryptographyUtils.decryptFile(
                        Path(tempInputFileOrFolderPath),
                        Path(tempOutputFileOrFolderPath),
                        argv.password,
                        Constants.Salt,
                        Constants.Iterations,
                        Constants.KeyLength
                    ) { throw it }

                    val outputName = Path(fileFooter.metadata!!.OriginalName).nameWithoutExtension
                    val outputExtension = FileUtils.getExtension(fileFooter.metadata!!.OriginalName)

                    if (!argv.keepOriginal) FileUtils.deleteFileOrDirectory(item.inputInfo.pathString)

                    item.outputInfo = Path(outputFolderPath, "$outputName$outputExtension")

                    if (argv.keepOriginal) item.outputInfo =
                        Path(FileUtils.nextAvailableFileNameAdvanced(item.outputInfo.pathString))

                    if (fileFooter.metadata!!.IsFile || fileFooter.metadata!!.FileType != FileUtils.Type.Directory) Files.copy(
                        Path(tempOutputFileOrFolderPath),
                        Path(item.outputInfo.pathString),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                    )
                    else ZipUtils.extractToDirectory(
                        tempOutputFileOrFolderPath,
                        item.outputInfo.pathString,
                        argv.overwriteExisting
                    )
                }
            } catch (e: Exception) {
                throw e
            } finally {
                withContext(Dispatchers.IO) {
                    FileUtils.deleteFileOrDirectory(tempInputFileOrFolderPath)
                    FileUtils.deleteFileOrDirectory(tempOutputFileOrFolderPath)
                }
            }
        }
    }
}
