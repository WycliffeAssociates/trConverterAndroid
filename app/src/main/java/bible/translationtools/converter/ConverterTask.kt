package bible.translationtools.converter

import android.os.AsyncTask
import androidx.fragment.app.Fragment

/**
 * Created by mXaln on 11/11/17.
 */
class ConverterTask(c: Fragment?) : AsyncTask<Void?, Int?, Int?>() {
    var callback: ConverterResultCallback

    init {
        this.callback = c as ConverterResultCallback
    }

    override fun doInBackground(vararg params: Void?): Int {
        return callback.startConversion()
    }

    override fun onPreExecute() {
        callback.conversionStarted()
        super.onPreExecute()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: Int?) {
        callback.conversionDone(result)
        super.onPostExecute(result)
    }

    interface ConverterResultCallback {
        fun startConversion(): Int
        fun conversionStarted()
        fun conversionDone(result: Int?)
    }
}
