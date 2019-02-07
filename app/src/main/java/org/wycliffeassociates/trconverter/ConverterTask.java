package org.wycliffeassociates.trconverter;

import android.content.Context;
import android.os.AsyncTask;

import org.wycliffeassociates.trConverter.Mode;

import java.util.List;

/**
 * Created by mXaln on 11/11/17.
 */

public final class ConverterTask extends AsyncTask<List<Mode>, Integer, String> {

    MainActivity activity;
    ConverterResultCallback mCallback;

    public ConverterTask(Context c) {
        this.activity = (MainActivity) c;
        this.mCallback = (ConverterResultCallback) c;
    }

    @Override
    protected String doInBackground(List<Mode>... lists) {
        List<Mode> modes = lists[0];

        try {
            this.activity.converter.setModes(modes);
            return this.activity.converter.convert();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    protected void onPreExecute() {
        mCallback.convertionStarted();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        mCallback.convertionDone(result);
        super.onPostExecute(result);
    }

    public interface ConverterResultCallback {
        Void convertionStarted();

        Void convertionDone(String result);
    }
}
