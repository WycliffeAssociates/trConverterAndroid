package org.wycliffeassociates.trconverter;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by mXaln on 11/11/17.
 */

public final class ConverterTask extends AsyncTask<Void, Integer, Integer> {

    MainActivity activity;
    ConverterResultCallback mCallback;

    public ConverterTask(Context c) {
        this.activity = (MainActivity) c;
        this.mCallback = (ConverterResultCallback) c;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        return this.activity.converter.convert();
    }

    @Override
    protected void onPreExecute() {
        mCallback.conversionStarted();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Integer result) {
        mCallback.conversionDone(result);
        super.onPostExecute(result);
    }

    public interface ConverterResultCallback {
        Void conversionStarted();

        Void conversionDone(Integer result);
    }
}
