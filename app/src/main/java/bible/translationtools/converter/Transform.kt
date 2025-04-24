package bible.translationtools.converter

import bible.translationtools.converterlib.ITransformer
import javax.inject.Inject

class Transform @Inject constructor() {

    data class Result(val count: Int, val error: String?)

    fun execute(transformer: ITransformer): Result {
        return try {
            val result = transformer.execute()
            Result(result, null)
        } catch (e: Exception) {
            Result(-1, e.message)
        }
    }
}