package org.wycliffeassociates.converter;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

/**
 * Created by mXaln on 11/11/17.
 */

public final class AnalyserTask extends AsyncTask<Void, Integer, Void> {

    AnaliserResultCallback callback;

    public AnalyserTask(Fragment c) {
        this.callback = (AnaliserResultCallback) c;
    }

    @Override
    protected Void doInBackground(Void... params) {
        callback.startAnalyze();
        return null;
    }

    @Override
    protected void onPreExecute() {
        callback.analyzeStarted();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void param) {
        callback.analyzeDone();
        super.onPostExecute(param);
    }

    public interface AnaliserResultCallback {
        Void startAnalyze();
        Void analyzeStarted();
        Void analyzeDone();
    }
}
