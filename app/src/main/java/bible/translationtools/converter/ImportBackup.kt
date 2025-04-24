package bible.translationtools.converter

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class ImportBackup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val directoryProvider: DirectoryProvider
) {
    data class Result(
        val success: Boolean,
        val message: String?
    )

    operator fun invoke(backupUri: Uri): Result {
        val uuid = UUID.randomUUID().toString()
        val tempZipFile = File(directoryProvider.internalCacheDir, "$uuid.zip")

        context.contentResolver.openInputStream(backupUri).use { inputStream ->
            FileOutputStream(tempZipFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (true) {
                    checkNotNull(inputStream)
                    if (inputStream.read(buffer).also { length = it } <= 0) break
                    outputStream.write(buffer, 0, length)
                }
            }
        }

        val result = ZipFile(tempZipFile).use { zipFile ->
            if (Utils.isAppBackup(zipFile)) {
                extractDirectory(zipFile, directoryProvider.workspaceDir)
                Result(true, null)
            } else {
                Result(false, context.getString(R.string.invalid_backup))
            }
        }
        tempZipFile.delete()

        return result
    }

    @Throws(IOException::class)
    private fun extractDirectory(zipFile: ZipFile, destDir: File) {
        val dirToExtract = "$USER_DATA_DIR/$FILES_DIR/$TRANSLATIONS_DIR"
        for (fileHeader in zipFile.fileHeaders) {
            // Check if the file is a child of the specified folder within the zip file
            val filePathInZip = fileHeader.fileName
            if (filePathInZip.startsWith(dirToExtract) && filePathInZip != dirToExtract) {
                // Calculate the relative path of the file by removing the parent folder path
                val relativePath = filePathInZip.substring(dirToExtract.length)

                // Construct the destination file path
                val destFile = File(destDir, relativePath)

                // Ensure the parent directories exist
                if (fileHeader.isDirectory) {
                    destFile.mkdirs()
                } else {
                    destFile.parentFile?.mkdirs()
                    // Extract the file to the destination directory
                    zipFile.extractFile(fileHeader, destFile.parent, destFile.name)
                }
            }
        }
    }

    private companion object {
        const val FILES_DIR = "files"
        const val TRANSLATIONS_DIR = "translations"
    }
}