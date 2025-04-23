package bible.translationtools.converter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends FragmentActivity {

    private String trDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TranslationRecorder";
    private String bttrDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BttRecorder";

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            fragment = fm.findFragmentById(R.id.fragment_container);

            if(fragment == null) {
                fragment = new ConverterFragment();
                ft.replace(R.id.fragment_container, fragment);
                ft.commit();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.grant_permission, Toast.LENGTH_LONG).show();
        }
    }

    public File trDir() {
        return new File(trDirPath);
    }

    public File bttrDir() {
        return new File(bttrDirPath);
    }
}
