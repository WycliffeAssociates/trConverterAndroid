package bible.translationtools.converter

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class LanguageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val languageList: ArrayList<Language> = ArrayList<Language>()

    init {
        parseJson()
    }

    fun getLang(slug: String): Language? {
        for (l in languageList) {
            if (l.slug == slug) {
                return l
            }
        }
        return null
    }

    fun loadJson(): String? {
        return try {
            context.assets.open("langnames.json").use { input ->
                val size = input.available()
                val buffer = ByteArray(size)
                input.read(buffer)
                String(buffer, charset("UTF-8"))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun parseJson() {
        try {
            val jsonArr = JSONArray(loadJson())
            for (i in 0..<jsonArr.length()) {
                val obj = jsonArr.get(i) as JSONObject
                languageList.add(
                    Language(
                        obj.getString("lc"),
                        obj.getString("ln"),
                        obj.getString("ang")
                    )
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
