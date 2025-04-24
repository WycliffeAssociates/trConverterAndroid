package bible.translationtools.converter

import android.content.Context
import bible.translationtools.converterlib.Project
import java.io.File

interface DirectoryProvider {

    /**
     * Returns the path to the internal files directory accessible by the app only.
     * This directory is not accessible by other applications and file managers.
     * It's good for storing private data, such as ssh keys.
     * Files saved in this directory will be removed when the application is uninstalled
     */
    val internalAppDir: File

    /**
     * Returns the path to the external files directory accessible by the app only.
     * This directory can be accessed by file managers.
     * It's good for storing user-created data, such as translations and backups.
     * Files saved in this directory will be removed when the application is uninstalled
     */
    val externalAppDir: File

    /**
     * Returns the absolute path to the application specific cache directory on the filesystem.
     */
    val internalCacheDir: File

    /**
     * Returns the absolute path to the workspace directory on the filesystem.
     */
    val workspaceDir: File

    /**
     * Returns the absolute path to the project directory on the filesystem.
     */
    fun getProjectDir(project: Project): File?

    /**
     * Creates a temporary directory in the cache directory.
     * @param name The optional name of the directory.
     */
    fun createTempDir(name: String?): File

    /**
     * Creates a temporary file in the cache directory.
     * @param prefix The optional prefix of the file.
     * @param suffix The optional suffix of the file.
     * @param dir The optional directory to create the file in.
     */
    fun createTempFile(prefix: String, suffix: String?, dir: File? = null): File

    /**
     * Clear the cache directory
     */
    fun clearCache()
}

class DirectoryProviderImpl (private val context: Context) : DirectoryProvider {
    companion object {
        const val TAG = "DirectoryProvider"
    }

    override val internalAppDir: File
        get() = context.filesDir

    override val externalAppDir: File
        get() = context.getExternalFilesDir(null)
            ?: throw NullPointerException("External storage is currently unavailable.")

    override val internalCacheDir: File
        get() = context.cacheDir

    override val workspaceDir: File
        get() {
            val workspace = File(externalAppDir, "workspace")
            if (!workspace.exists()) {
                workspace.mkdirs()
            }
            return workspace
        }

    override fun getProjectDir(project: Project): File? {
        val projectPath = "${project.language}/${project.version}/${project.book}"
        val projectDir = File(workspaceDir, projectPath)

        return if (projectDir.exists()) {
            return projectDir
        } else null
    }

    override fun createTempDir(name: String?): File {
        val tempName = name ?: System.currentTimeMillis().toString()
        val tempDir = File(internalCacheDir, tempName)
        tempDir.mkdirs()
        return tempDir
    }

    override fun createTempFile(prefix: String, suffix: String?, dir: File?): File {
        return File.createTempFile(prefix, suffix, dir ?: internalCacheDir)
    }

    override fun clearCache() {
        internalCacheDir.listFiles()?.forEach {
            FileUtils.deleteRecursive(it)
        }
    }
}