package bible.translationtools.converter;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

/**
 * Created by mXaln on 11/11/17.
 */

public final class MigrationTask extends AsyncTask<Void, Integer, Void> {

    MigrationResultCallback callback;

    public MigrationTask(Fragment c) {
        this.callback = (MigrationResultCallback) c;
    }

    @Override
    protected Void doInBackground(Void... params) {
        callback.startMigration();
        return null;
    }

    @Override
    protected void onPreExecute() {
        callback.migrationStarted();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void param) {
        callback.migrationDone();
        super.onPostExecute(param);
    }

    public interface MigrationResultCallback {
        Void startMigration();
        Void migrationStarted();
        Void migrationDone();
    }
}
