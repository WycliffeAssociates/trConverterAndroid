package bible.translationtools.converter

import android.os.AsyncTask
import androidx.fragment.app.Fragment

/**
 * Created by mXaln on 11/11/17.
 */
class TransformerTask(c: Fragment?) : AsyncTask<Void?, Int?, Int?>() {
    var callback: TransformerResultCallback

    init {
        this.callback = c as TransformerResultCallback
    }

    override fun doInBackground(vararg params: Void?): Int? {
        return callback.startTransformation()
    }

    override fun onPreExecute() {
        callback.transformationStarted()
        super.onPreExecute()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: Int?) {
        callback.transformationDone(result)
        super.onPostExecute(result)
    }

    interface TransformerResultCallback {
        fun startTransformation(): Int
        fun transformationStarted()
        fun transformationDone(result: Int?)
    }
}
