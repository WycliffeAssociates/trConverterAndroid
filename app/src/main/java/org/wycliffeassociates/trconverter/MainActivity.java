package org.wycliffeassociates.trconverter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.wycliffeassociates.trConverter.Converter;
import org.wycliffeassociates.trConverter.Mode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ConverterTask.ConverterResultCallback,
        AnaliserTask.AnaliserResultCallback {

    private Button button;
    private ProgressBar progress;
    private TextView messageView;

    private ListView listView;
    private ModeListAdapter listAdapter;

    private List<Mode> modes = new ArrayList<>();

    protected Converter converter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        button = findViewById(R.id.convert);
        progress = findViewById(R.id.progressBar);
        listView = findViewById(R.id.listView);
        messageView = findViewById(R.id.messageView);

        init();
    }

    protected void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (modes.isEmpty())
                    analize();
                else
                    convert();
            }
        });


    }

    protected void analize() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            AnaliserTask analiser = new AnaliserTask(MainActivity.this);
            analiser.execute();
        } else {
            Toast.makeText(getApplicationContext(), R.string.grant_permission, Toast.LENGTH_LONG).show();
        }
    }

    protected void convert() {
        Boolean shouldConvert = true;
        for (Mode m: modes) {
            if (m.mode.isEmpty())
            {
                shouldConvert = false;
                break;
            }
        }

        if (shouldConvert) {
            ConverterTask converter = new ConverterTask(MainActivity.this);
            converter.execute(modes);
        } else {
            Toast.makeText(getApplicationContext(), R.string.set_modes, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Void convertionStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(false);
                button.setText(R.string.processing);
                progress.setVisibility(View.VISIBLE);
                messageView.setText("");
            }
        });
        return null;
    }

    @Override
    public Void convertionDone(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                modes.clear();
                button.setEnabled(true);
                button.setText(R.string.convert);
                progress.setVisibility(View.GONE);

                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }
        });
        return null;
    }

    @Override
    public Void analizeStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(false);
                button.setText(R.string.analizing);
                progress.setVisibility(View.VISIBLE);
            }
        });
        return null;
    }

    @Override
    public Void analizeDone(final List<Mode> result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                modes = result;
                button.setEnabled(true);
                button.setText(R.string.convert_continue);
                progress.setVisibility(View.GONE);

                if(modes.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.empty_modes, Toast.LENGTH_SHORT).show();
                    button.setText(R.string.convert);
                } else {
                    Boolean shouldConvert = true;
                    for (Mode m: modes) {
                        if (m.mode.isEmpty())
                        {
                            shouldConvert = false;
                            break;
                        }
                    }

                    if(shouldConvert) {
                        convert();
                    } else {
                        messageView.setText(R.string.set_modes);
                        listAdapter = new ModeListAdapter(MainActivity.this, modes);
                        listView.setAdapter(listAdapter);
                    }
                }
            }
        });
        return null;
    }
}
