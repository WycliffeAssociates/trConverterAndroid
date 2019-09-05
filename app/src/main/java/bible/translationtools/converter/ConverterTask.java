package bible.translationtools.converter;

import android.os.AsyncTask;
import androidx.fragment.app.Fragment;

/**
 * Created by mXaln on 11/11/17.
 */

public final class ConverterTask extends AsyncTask<Void, Integer, Integer> {

    ConverterResultCallback callback;

    public ConverterTask(Fragment c) {
        this.callback = (ConverterResultCallback) c;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        return callback.startConversion();
    }

    @Override
    protected void onPreExecute() {
        callback.conversionStarted();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Integer result) {
        callback.conversionDone(result);
        super.onPostExecute(result);
    }

    public interface ConverterResultCallback {
        Integer startConversion();
        Void conversionStarted();
        Void conversionDone(Integer result);
    }
}
