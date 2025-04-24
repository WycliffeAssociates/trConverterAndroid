package bible.translationtools.converter

import bible.translationtools.converterlib.IConverter
import javax.inject.Inject

class Convert @Inject constructor() {
    data class Result(val count: Int, val error: String?)

    fun execute(converter: IConverter): Result {
        return try {
            val result = converter.execute()
            Result(result, null)
        } catch (e: Exception) {
            Result(-1, e.message)
        }
    }
}