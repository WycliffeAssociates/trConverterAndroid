package trconverter.wycliffeassociates.org.trconverter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.converter.Converter;

/**
 * Created by max on 11/11/17.
 */

public final class ConverterTask extends AsyncTask {

    Context context;
    ResultCallback mCallback;

    public ConverterTask(Context c) {
        this.context = c;
        this.mCallback = (ResultCallback) c;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        mCallback.convertionStarted();

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String[] params = new String[]{dir};
        String result = Converter.Convert(params);

        mCallback.convertionDone(result);

        return null;
    }

    public interface ResultCallback {
        Void convertionStarted();

        Void convertionDone(String result);
    }
}
