package bible.translationtools.converter

import bible.translationtools.converterlib.IConverter
import javax.inject.Inject

class Analyze @Inject constructor() {
    data class Result(val success: Boolean, val error: String?)

    fun execute(converter: IConverter): Result {
        return try {
            converter.analyze()
            Result(true, null)
        } catch (e: Exception) {
            Result(false, e.message)
        }
    }
}