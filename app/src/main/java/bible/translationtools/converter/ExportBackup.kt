package bible.translationtools.converter

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileInputStream
import javax.inject.Inject

class ExportBackup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val zipBackup: ZipBackup
) {
    data class Result(val success: Boolean, val error: String?)

    operator fun invoke(backupUri: Uri): Result {
        return try {
            val tempZipFile = zipBackup()

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

            Result(true, null)
        } catch (e: Exception) {
            Result(false, e.message)
        }
    }
}