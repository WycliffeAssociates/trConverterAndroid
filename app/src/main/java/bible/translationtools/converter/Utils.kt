package bible.translationtools.converter

import bible.translationtools.converter.ImportBackup.Companion.APP_DATA_DIR
import bible.translationtools.converter.ImportBackup.Companion.USER_DATA_DIR
import net.lingala.zip4j.ZipFile

object Utils {
    fun isAppBackup(zipFile: ZipFile): Boolean {
        var hasAppData = false
        var hasUserData = false

        for (fileHeader in zipFile.fileHeaders) {
            if (fileHeader.isDirectory) {
                if (fileHeader.fileName == "$APP_DATA_DIR/") {
                    hasAppData = true
                }
                if (fileHeader.fileName == "$USER_DATA_DIR/") {
                    hasUserData = true
                }
            }
        }
        return hasAppData && hasUserData
    }
}