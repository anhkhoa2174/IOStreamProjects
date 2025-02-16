package vn.iostream.apps.file_locker_x.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import vn.iostream.apps.file_locker_x.configs.Constants
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.withLock
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.fileSize
import kotlin.math.ceil
import kotlin.math.log
import kotlin.math.pow

data class FilePack(val off: Int, val len: Int, var completed: Boolean, var isTaken: Boolean)

class CryptographyUtils private constructor() {
    companion object {
        private fun getCipher(
            password: String, salt: ByteArray, iteration: Int, keyLength: Int, mode: Int
        ): Cipher {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keySpec = PBEKeySpec(password.toCharArray(), salt, iteration, keyLength)

            val derivedKey = factory.generateSecret(keySpec).encoded

            val key = ByteArray(32)
            val iv = ByteArray(16)

            System.arraycopy(derivedKey, 0, key, 0, key.size)
            System.arraycopy(derivedKey, key.size, iv, 0, iv.size)

            val secretKeySpec = SecretKeySpec(key, "AES")
            val ivSpec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(mode, secretKeySpec, ivSpec)

            return cipher
        }

        fun encrypt(
            inputBytes: ByteArray, password: String, salt: ByteArray, iteration: Int, keyLength: Int
        ): ByteArray {
            val cipher = getCipher(password, salt, iteration, keyLength, Cipher.ENCRYPT_MODE)
            val results = cipher.doFinal(inputBytes)

            return results
        }

        fun encrypt(
            inputString: String, password: String, salt: ByteArray, iteration: Int, keyLength: Int
        ): ByteArray {
            val cipher = getCipher(password, salt, iteration, keyLength, Cipher.ENCRYPT_MODE)
            val results = cipher.doFinal(inputString.toByteArray())

            return results
        }

        fun decrypt(
            inputBytes: ByteArray, password: String, salt: ByteArray, iteration: Int, keyLength: Int
        ): ByteArray {
            val cipher = getCipher(password, salt, iteration, keyLength, Cipher.DECRYPT_MODE)
            val results = cipher.doFinal(inputBytes)

            return results
        }

        fun decrypt(
            inputString: String, password: String, salt: ByteArray, iteration: Int, keyLength: Int
        ): ByteArray {
            val cipher = getCipher(password, salt, iteration, keyLength, Cipher.DECRYPT_MODE)
            val results = cipher.doFinal(inputString.toByteArray())

            return results
        }

        @OptIn(ExperimentalEncodingApi::class)
        fun encryptToB64(
            inputString: String, password: String, salt: ByteArray, iteration: Int, keyLength: Int
        ): String {
            val cipher = getCipher(password, salt, iteration, keyLength, Cipher.ENCRYPT_MODE)
            val results = cipher.doFinal(inputString.toByteArray())

            val result: String = Base64.encode(results)
            return result
        }

        @OptIn(ExperimentalEncodingApi::class)
        fun decryptFromB64(
            inputString: String, password: String, salt: ByteArray, iteration: Int, keyLength: Int
        ): String {
            val encryptedBytes = Base64.decode(inputString)

            val cipher = getCipher(password, salt, iteration, keyLength, Cipher.DECRYPT_MODE)

            val decrypted = cipher.doFinal(encryptedBytes)
            val result = String(decrypted)

            return result
        }

        fun xorEncrypt(data: ByteArray, xorKey: ByteArray): ByteArray {
            val encryptedData = ByteArray(data.size)
            var xorKeyIndex = 0

            for (i in data.indices) {
                encryptedData[i] = data[i] xor xorKey[xorKeyIndex]
                xorKeyIndex = (xorKeyIndex + 1) % xorKey.size
            }

            return encryptedData
        }

        fun encryptFile(
            inputFilePath: Path,
            outputFilePath: Path,
            progressCallback: ((progress: Double) -> Unit) = {},
            errorCallback: ((e: Exception) -> Unit) = {},
        ) {
            val xorKey = ByteArray(12345)
            var EncryptedPackSize = 0
            val currentMemory = Runtime.getRuntime().freeMemory()
            val numberOfCores = Runtime.getRuntime().availableProcessors()

            var memoryLogarithm = log(
                ((currentMemory / numberOfCores).toDouble()), 2.0
            ).toInt() - 4
            if (memoryLogarithm < 1) memoryLogarithm = 1
            val packSize = 102400.toInt()

            val filePath = inputFilePath
            val fileSize = filePath.toFile().length()

            val numberOfPack = ceil((fileSize / packSize).toDouble()).toInt()
            val filePackPosition = mutableListOf<FilePack>()

            for (i in 0 until numberOfPack) filePackPosition.add(
                FilePack(
                    i * packSize, packSize, false, false
                )
            )
            filePackPosition.add(
                FilePack(
                    numberOfPack * packSize,
                    fileSize.toInt() - numberOfPack * packSize,
                    false,
                    false
                )
            )

            val reentrantLock = ReentrantLock()

            fun findUndonePair(): Pair<Int, FilePack?> {
                var index = -1
                var filePack: FilePack? = null

                reentrantLock.withLock {
                    index =
                        filePackPosition.indexOfFirst { !it.completed && !it.isTaken }
                    if (index != -1) {
                        filePack = filePackPosition[index]
                        filePackPosition[index].isTaken = true
                    }
                }

                return Pair(index, filePack)
            }

            GlobalScope.launch(Dispatchers.IO) {
                val start = System.nanoTime()

                val deferredResults = (0 until numberOfCores).map {
                    GlobalScope.async {
                        do {
                            val pair = findUndonePair()
                            if (pair.first == -1) break

                            val pack = pair.second ?: break

                            var data = ByteArray(pack.len)

                            val fis = FileInputStream(filePath.toFile())
                            fis.skip(pack.off.toLong())
                            fis.read(data, 0, pack.len)
                            fis.close()

                            val fos = FileOutputStream(outputFilePath.toFile().absolutePath + "_PART_${pair.first}")

                            val newdata = xorEncrypt(data, xorKey)
                            if (EncryptedPackSize == 0) {
                                EncryptedPackSize = newdata.size
                            }

                            val sizeBytes = ByteBuffer.allocate(4).putInt(newdata.size).array()
                            fos.write(sizeBytes)
                            fos.write(newdata)
                            fos.flush()
                            fos.close()

                            reentrantLock.withLock {
                                filePackPosition[pair.first].completed = true
                                filePackPosition[pair.first].isTaken = true
                            }
                        } while (true)
                    }
                }

                try {
                    deferredResults.awaitAll()
                    val fosOutput = FileOutputStream(outputFilePath.toFile())

                    for (i in 0 until numberOfPack + 1) {
                        val inputFile = File(outputFilePath.toFile().absolutePath + "_PART_$i")
                        val fisInput = FileInputStream(inputFile)

                        val buffer = ByteArray(2.0.pow(25).toInt())
                        var bytesRead: Int
                        while (fisInput.read(buffer).also { bytesRead = it } != -1) {
                            fosOutput.write(buffer, 0, bytesRead)
                        }
                        fisInput.close()
                        inputFile.delete()
                    }
                    fosOutput.flush()
                    fosOutput.close()

                    val end = System.nanoTime()
                    progressCallback(1.0)
                } catch (e: Exception) {
                    errorCallback(e)
                }
            }
        }

        fun xorDecrypt(data: ByteArray, xorKey: ByteArray): ByteArray {
            return xorEncrypt(data, xorKey)
        }

        fun decryptFile(
            inputFilePath: Path,
            outputFilePath: Path,
            password: String, salt: ByteArray, iteration: Int, keyLength: Int,
            progressCallback: ((progress: Double) -> Unit) = {},
            errorCallback: ((e: Exception) -> Unit) = {},
        ) {
            val xorKey = ByteArray(12345)
            val currentMemory = Runtime.getRuntime().freeMemory()
            val numberOfCores = Runtime.getRuntime().availableProcessors()

            var memoryLogarithm = log(
                ((currentMemory / numberOfCores).toDouble()), 2.0
            ).toInt() - 4
            if (memoryLogarithm < 1) memoryLogarithm = 1
            val packSize = 102400.toInt()

            val filePath = inputFilePath
            val fileSize = filePath.toFile().length()

            val numberOfPack = ceil((fileSize / packSize).toDouble()).toInt()
            val filePackPosition = mutableListOf<FilePack>()

            for (i in 0 until numberOfPack) filePackPosition.add(
                FilePack(
                    i * packSize, packSize, false, false
                )
            )
            filePackPosition.add(
                FilePack(
                    numberOfPack * packSize,
                    fileSize.toInt() - numberOfPack * packSize,
                    false,
                    false
                )
            )

            val reentrantLock = ReentrantLock()

            fun findUndonePair(): Pair<Int, FilePack?> {
                var index = -1
                var filePack: FilePack? = null

                reentrantLock.withLock {
                    index =
                        filePackPosition.indexOfFirst { !it.completed && !it.isTaken }
                    if (index != -1) {
                        filePack = filePackPosition[index]
                        filePackPosition[index].isTaken = true
                    }
                }

                return Pair(index, filePack)
            }

            GlobalScope.launch(Dispatchers.IO) {
                val start = System.nanoTime()

                val deferredResults = (0 until numberOfCores).map {
                    GlobalScope.async {
                        do {
                            val pair = findUndonePair()
                            if (pair.first == -1) break

                            val pack = pair.second ?: break

                            var encryptedData = ByteArray(pack.len)

                            val fis = FileInputStream(filePath.toFile())
                            fis.skip(pack.off.toLong())
                            fis.read(encryptedData, 0, pack.len)
                            fis.close()

                            val decryptedData = xorDecrypt(encryptedData, xorKey)

                            val decryptedFilePath = outputFilePath.toFile().absolutePath + "_PART_${pair.first}"
                            val fos = FileOutputStream(decryptedFilePath)

                            fos.write(decryptedData)
                            fos.flush()
                            fos.close()

                            reentrantLock.withLock {
                                filePackPosition[pair.first].completed = true
                                filePackPosition[pair.first].isTaken = true
                            }
                        } while (true)
                    }
                }

                try {
                    deferredResults.awaitAll()
                    val fosOutput = FileOutputStream(outputFilePath.toFile())

                    for (i in 0 until numberOfPack + 1) {
                        val inputFile = File(outputFilePath.toFile().absolutePath + "_PART_$i")
                        val fisInput = FileInputStream(inputFile)

                        val buffer = ByteArray(2.0.pow(25).toInt())
                        var bytesRead: Int
                        while (fisInput.read(buffer).also { bytesRead = it } != -1) {
                            fosOutput.write(buffer, 0, bytesRead)
                        }
                        fisInput.close()
                        inputFile.delete()
                    }
                    fosOutput.flush()
                    fosOutput.close()

                    val end = System.nanoTime()
                    Log.d("DevTag", "Decryption completed in ${end - start} nanoseconds")
                    progressCallback(1.0)
                } catch (e: Exception) {
                    errorCallback(e)
                }
            }
        }

        fun decryptBuffer(
            encryptedBuffer: ByteArray,
            password: String,
            salt: ByteArray,
            iteration: Int,
            keyLength: Int,
        ): ByteArray {
            val cipher = getCipher(password, salt, iteration, keyLength, Cipher.DECRYPT_MODE)

            var cis: CipherInputStream? = null
            var outputStream: ByteArrayOutputStream? = null

            return try {
                cis = CipherInputStream(ByteArrayInputStream(encryptedBuffer), cipher)
                outputStream = ByteArrayOutputStream()

                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read = cis.read(buffer)

                val total = encryptedBuffer.size.toLong()
                var done = 0L

                while (read > -1) {
                    outputStream.write(buffer, 0, read)
                    read = cis.read(buffer)

                    if (read > -1) {
                        done += read
                    }
                }

                outputStream.toByteArray()
            } catch (e: Exception) {
                //errorCallback(e)
                throw CryptographyException()
            } finally {
                cis?.close()
                outputStream?.close()
            }
        }

    }

}