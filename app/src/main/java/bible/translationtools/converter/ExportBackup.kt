package bible.translationtools.converter

import android.content.Context
import android.net.Uri
import bible.translationtools.converter.ImportBackup.Companion.APP_DATA_DIR
import bible.translationtools.converter.ImportBackup.Companion.FILES_DIR
import bible.translationtools.converter.ImportBackup.Companion.TRANSLATIONS_DIR
import bible.translationtools.converter.ImportBackup.Companion.USER_DATA_DIR
import dagger.hilt.android.qualifiers.ApplicationContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import java.io.File
import java.io.FileInputStream
import java.util.UUID
import javax.inject.Inject

class ExportBackup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val directoryProvider: DirectoryProvider
) {
    data class Result(val success: Boolean, val error: String?)

    operator fun invoke(backupUri: Uri): Result {
        return try {
            val uuid = UUID.randomUUID().toString()
            val tempZipFile = File(directoryProvider.internalCacheDir, "$uuid.zip")

            val appDataDir = directoryProvider.createTempDir("app_data")
            val userDataDir = directoryProvider.createTempDir("user_data")
            val filesDir = File(userDataDir, FILES_DIR)
            val translationsDir = File(filesDir, TRANSLATIONS_DIR)
            translationsDir.mkdirs()
            FileUtils.copyDirectory(directoryProvider.workspaceDir, translationsDir, null)

            ZipFile(tempZipFile).use { zipper ->
                val zp = ZipParameters()
                zp.compressionLevel = CompressionLevel.ULTRA

                zipper.addFolder(appDataDir, zp)
                zipper.renameFile(appDataDir.name + "/", APP_DATA_DIR)

                zipper.addFolder(userDataDir, zp)
                zipper.renameFile(userDataDir.name + "/", USER_DATA_DIR)
            }

            context.contentResolver.openOutputStream(backupUri).use { outputStream ->
                FileInputStream(tempZipFile).use { inputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while ((inputStream.read(buffer).also { length = it }) > 0) {
                        checkNotNull(outputStream)
                        outputStream.write(buffer, 0, length)
                    }
                }
            }

            FileUtils.deleteRecursive(tempZipFile)
            FileUtils.deleteRecursive(appDataDir)
            FileUtils.deleteRecursive(userDataDir)

            Result(true, null)
        } catch (e: Exception) {
            Result(false, e.message)
        }
    }
}