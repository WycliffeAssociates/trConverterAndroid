package org.wycliffeassociates.trconverter;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by mXaln on 11/11/17.
 */

public final class AnalyserTask extends AsyncTask<Void, Integer, Void> {

    MainActivity activity;
    AnaliserResultCallback mCallback;

    public AnalyserTask(Context c) {
        this.activity = (MainActivity) c;
        this.mCallback = (AnaliserResultCallback) c;
    }

    @Override
    protected Void doInBackground(Void... params) {
        this.activity.converter.analize();
        return null;
    }

    @Override
    protected void onPreExecute() {
        mCallback.analyzeStarted();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void param) {
        mCallback.analyzeDone();
        super.onPostExecute(param);
    }

    public interface AnaliserResultCallback {
        Void analyzeStarted();

        Void analyzeDone();
    }
}
