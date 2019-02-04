package trconverter.wycliffeassociates.org.trconverter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.wycliffeassociates.trConverter.Converter;
import org.wycliffeassociates.trConverter.Mode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mXaln on 11/11/17.
 */

public final class AnaliserTask extends AsyncTask<String, Integer, List<Mode>> {

    Context context;
    AnaliserResultCallback mCallback;

    public AnaliserTask(Context c) {
        this.context = c;
        this.mCallback = (AnaliserResultCallback) c;
    }

    @Override
    protected List<Mode> doInBackground(String... values) {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String[] params = new String[]{dir,""};

        try {
            Converter converter = new Converter(params);
            converter.analize();
            return converter.getModes();
        } catch (Exception e) {
            Log.e("TRC", e.getMessage());
            return new ArrayList<>();
        }
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
    protected void onPostExecute(List<Mode> result) {
        mCallback.analizeDone(result);
        super.onPostExecute(result);
    }

    public interface AnaliserResultCallback {
        Void analizeStarted();

        Void analizeDone(List<Mode> result);
    }
}
