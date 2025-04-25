package bible.translationtools.converter

import android.content.Context
import android.net.Uri
import bible.translationtools.converterlib.Project
import dagger.hilt.android.qualifiers.ApplicationContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import java.io.File
import javax.inject.Inject

class ExportProject @Inject constructor(
    @ApplicationContext private val context: Context,
    private val directoryProvider: DirectoryProvider
) {
    data class Result(val success: Boolean, val error: String?)

    operator fun invoke(project: Project, outputUri: Uri): Result {
        return try {
            val projectDir = directoryProvider.getProjectDir(project)
            if (projectDir == null) throw IllegalArgumentException("Project directory not found")

            val zipFile = zipProject(projectDir)
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                zipFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            FileUtils.deleteRecursive(zipFile)
            Result(true, null)
        } catch (e: Exception) {
            Result(false, e.message)
        }
    }

    private fun zipProject(projectDir: File): File {
        val tempZip = directoryProvider.createTempFile("temp_export", ".zip")
        val zipper = ZipFile(tempZip)
        val zp = ZipParameters()
        zp.compressionLevel = CompressionLevel.ULTRA
        zipper.addFolder(projectDir, zp)
        return tempZip
    }
}