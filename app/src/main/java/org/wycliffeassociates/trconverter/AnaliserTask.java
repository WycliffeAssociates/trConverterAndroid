package org.wycliffeassociates.trconverter;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by mXaln on 11/11/17.
 */

public final class AnaliserTask extends AsyncTask<Void, Integer, Void> {

    MainActivity activity;
    AnaliserResultCallback mCallback;

    public AnaliserTask(Context c) {
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
        mCallback.analizeStarted();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void param) {
        mCallback.analizeDone();
        super.onPostExecute(param);
    }

    public interface AnaliserResultCallback {
        Void analizeStarted();

        Void analizeDone();
    }
}
