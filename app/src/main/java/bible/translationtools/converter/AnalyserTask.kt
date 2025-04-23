package bible.translationtools.converter

import android.os.AsyncTask
import androidx.fragment.app.Fragment

/**
 * Created by mXaln on 11/11/17.
 */
class AnalyserTask(c: Fragment?) : AsyncTask<Void?, Int?, Void?>() {
    var callback: AnalyzerResultCallback

    init {
        this.callback = c as AnalyzerResultCallback
    }

    override fun doInBackground(vararg params: Void?): Void? {
        callback.startAnalyze()
        return null
    }

    override fun onPreExecute() {
        callback.analyzeStarted()
        super.onPreExecute()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(param: Void?) {
        callback.analyzeDone()
        super.onPostExecute(param)
    }

    interface AnalyzerResultCallback {
        fun startAnalyze()
        fun analyzeStarted()
        fun analyzeDone()
    }
}
