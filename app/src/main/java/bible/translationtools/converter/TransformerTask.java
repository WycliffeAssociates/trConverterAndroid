package bible.translationtools.converter;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

/**
 * Created by mXaln on 11/11/17.
 */

public final class TransformerTask extends AsyncTask<Void, Integer, Integer> {

    TransformerResultCallback callback;

    public TransformerTask(Fragment c) {
        this.callback = (TransformerResultCallback) c;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        return callback.startTransformation();
    }

    @Override
    protected void onPreExecute() {
        callback.transformationStarted();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Integer result) {
        callback.transformationDone(result);
        super.onPostExecute(result);
    }

    public interface TransformerResultCallback {
        Integer startTransformation();
        Void transformationStarted();
        Void transformationDone(Integer result);
    }
}
