package org.wycliffeassociates.converter;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.*;
import org.wycliffeassociates.trConverter.Converter;
import org.wycliffeassociates.trConverter.Mode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ConverterTask.ConverterResultCallback,
        AnalyserTask.AnaliserResultCallback {

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
        try {
            String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
            String[] params = new String[]{dir,""};

            converter = new Converter(params);

            button.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    if (modes.isEmpty())
                        analyze();
                    else
                        convert();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    protected void analyze() {
        AnalyserTask analyserTask = new AnalyserTask(MainActivity.this);
        analyserTask.execute();
    }

    @Override
    public Void analyzeStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(false);
                button.setText(R.string.analyzing);
                progress.setVisibility(View.VISIBLE);
                messageView.setText("");
            }
        });
        return null;
    }

    @Override
    public Void analyzeDone() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                modes = converter.getModes();
                button.setEnabled(true);
                button.setText(R.string.convert);
                listView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);

                if(modes.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.empty_modes, Toast.LENGTH_SHORT).show();
                    button.setText(R.string.analyze);
                } else {
                    Boolean hasEmptyModes = false;
                    for (Mode m: modes) {
                        if (m.mode.isEmpty())
                        {
                            hasEmptyModes = true;
                            break;
                        }
                    }

                    if(hasEmptyModes) {
                        messageView.setText(R.string.set_modes);
                        messageView.setTextColor(Color.RED);
                    }
                    listAdapter = new ModeListAdapter(MainActivity.this, modes);
                    listView.setAdapter(listAdapter);
                }
            }
        });
        return null;
    }

    protected void convert() {
        Boolean hasEmptyModes = false;
        for (Mode m: modes) {
            if (m.mode.isEmpty())
            {
                hasEmptyModes = true;
                break;
            }
        }

        if (!hasEmptyModes) {
            converter.setModes(modes);
            ConverterTask converterTask = new ConverterTask(MainActivity.this);
            converterTask.execute();
        } else {
            Toast.makeText(getApplicationContext(), R.string.set_modes, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Void conversionStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(false);
                button.setText(R.string.processing);
                listView.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                messageView.setText("");
            }
        });
        return null;
    }

    @Override
    public Void conversionDone(final Integer result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                modes.clear();
                converter.setModes(modes);
                converter.setDateTimeDir();
                button.setEnabled(true);
                button.setText(R.string.analyze);
                progress.setVisibility(View.GONE);

                if(result >= 0) {
                    messageView.setText(getString(R.string.conversion_complete, result));
                } else {
                    messageView.setText(R.string.error_occurred);
                }
                messageView.setTextColor(Color.BLACK);
            }
        });
        return null;
    }
}
