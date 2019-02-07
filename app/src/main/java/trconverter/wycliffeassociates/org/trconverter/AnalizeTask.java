package trconverter.wycliffeassociates.org.trconverter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.wycliffeassociates.trConverter.Converter;

import java.util.Arrays;

/**
 * Created by mXaln on 11/11/17.
 */

public final class ConverterTask extends AsyncTask<Boolean, Integer, String> {

    Context context;
    ResultCallback mCallback;

    public ConverterTask(Context c) {
        this.context = c;
        this.mCallback = (ResultCallback) c;
    }

    @Override
    protected String doInBackground(Boolean... booleans) {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String[] params = new String[]{dir,"a"};
        Boolean shouldConvert = booleans[0];

        try {
            Converter converter = new Converter(params);

            if(shouldConvert) {
                // Set modes here
                converter.convert();
            } else {
                converter.analize();

                if (converter.getModes().isEmpty() ||
                        Arrays.stream(converter.getModes()).filter()) {
                    converter.convert();
                }
            }
            return "ejhjskejf";
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

    public interface ResultCallback {
        Void convertionStarted();

        Void convertionDone(String result);
    }
}
