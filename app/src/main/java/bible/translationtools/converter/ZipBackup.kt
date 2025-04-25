package bible.translationtools.converter

import bible.translationtools.converter.ImportBackup.Companion.APP_DATA_DIR
import bible.translationtools.converter.ImportBackup.Companion.FILES_DIR
import bible.translationtools.converter.ImportBackup.Companion.TRANSLATIONS_DIR
import bible.translationtools.converter.ImportBackup.Companion.USER_DATA_DIR
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ZipBackup @Inject constructor(
    private val directoryProvider: DirectoryProvider
) {
    operator fun invoke(filename: String? = null): File {
        val name = filename ?: "${UUID.randomUUID()}.zip"
        val tempZipFile = File(directoryProvider.sharedDir, name)

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

        FileUtils.deleteRecursive(appDataDir)
        FileUtils.deleteRecursive(userDataDir)

        return tempZipFile
    }
}