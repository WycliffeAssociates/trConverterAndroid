package bible.translationtools.converter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public class MigrationDialogFragment extends DialogFragment {
    private MigrationCallback callback;

    interface MigrationCallback {
        void onMigrate();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.migrate_message)
                .setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(callback != null) {
                            callback.onMigrate();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("Canceled. Do nothing");
                    }
                });
        return builder.create();
    }

    public void setMigrationCallback(MigrationCallback callback) {
        this.callback = callback;
    }
}
