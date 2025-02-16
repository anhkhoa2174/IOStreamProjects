package co.iostream.apps.code_pocket.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CryptographyUtils private constructor() {
    companion object {
        private val defaultSalt = "275010a649c4d5690f10dc49b9418456".toByteArray()

        fun encryptFile(
            inputFilePath: String,
            outputFilePath: String,
            password: String,
            progressCallback: ((progress: Double) -> Unit)?,
            errorCallback: ((e: Exception) -> Unit),
        ) {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keySpec = PBEKeySpec(password.toCharArray(), defaultSalt, 2048, 384)

            val derivedKey = factory.generateSecret(keySpec).encoded

            val key = ByteArray(32)
            val iv = ByteArray(16)

            System.arraycopy(derivedKey, 0, key, 0, key.size)
            System.arraycopy(derivedKey, key.size, iv, 0, iv.size)

            val secretKeySpec = SecretKeySpec(key, "AES")
            val ivSpec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)

            var fis: FileInputStream? = null
            var cos: CipherOutputStream? = null

            try {
                fis = FileInputStream(inputFilePath)
                cos = CipherOutputStream(FileOutputStream(outputFilePath), cipher)

                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read = fis.read(buffer)

                if (progressCallback != null) {
                    val total = File(inputFilePath).length()
                    var done = 0L

                    while (read > -1) {
                        cos.write(buffer, 0, read)
                        read = fis.read(buffer)

                        if (read > -1) {
                            done += read
                            progressCallback(done.toDouble() / total)
                        }
                    }
                } else {
                    while (read > -1) {
                        cos.write(buffer, 0, read)
                        read = fis.read(buffer)
                    }
                }

                cos.flush()
            } catch (e: Exception) {
                errorCallback(e)
            } finally {
                fis?.close()
                cos?.close()
            }
        }

        fun decryptFile(
            inputFilePath: String,
            outputFilePath: String,
            password: String,
            progressCallback: ((progress: Double) -> Unit)?,
            errorCallback: ((e: Exception) -> Unit),
        ) {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keySpec = PBEKeySpec(password.toCharArray(), defaultSalt, 2048, 384)

            val derivedKey = factory.generateSecret(keySpec).encoded

            val key = ByteArray(32)
            val iv = ByteArray(16)

            System.arraycopy(derivedKey, 0, key, 0, key.size)
            System.arraycopy(derivedKey, key.size, iv, 0, iv.size)

            val secretKeySpec = SecretKeySpec(key, "AES")
            val ivSpec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)

            var cis: CipherInputStream? = null
            var fos: FileOutputStream? = null

            try {
                cis = CipherInputStream(FileInputStream(inputFilePath), cipher)
                fos = FileOutputStream(outputFilePath)

                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read = cis.read(buffer)


                if (progressCallback != null) {
                    val total = File(inputFilePath).length()
                    var done = 0L

                    while (read > -1) {
                        fos.write(buffer, 0, read)
                        read = cis.read(buffer)

                        if (read > -1) {
                            done += read
                            progressCallback(done.toDouble() / total)
                        }
                    }
                } else {
                    while (read > -1) {
                        fos.write(buffer, 0, read)
                        read = cis.read(buffer)
                    }
                }
            } catch (e: Exception) {
                errorCallback(e)
            } finally {
                cis?.close()
                fos?.close()
            }
        }
    }

}