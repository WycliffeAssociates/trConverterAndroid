package bible.translationtools.converter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import bible.translationtools.converterlib.Converter;
import bible.translationtools.converterlib.IConverter;
import bible.translationtools.converterlib.Mode;

import java.util.ArrayList;
import java.util.List;

public class ConverterFragment extends Fragment implements ConverterTask.ConverterResultCallback,
        AnalyserTask.AnaliserResultCallback, MigrationTask.MigrationResultCallback,
        MigrationDialogFragment.MigrationCallback {

    private MainActivity activity;

    private boolean isAnalyzing = false;
    private boolean isConverting = false;
    private boolean isMigrating = false;
    private String messageText = "";
    private String buttonText = "";

    private Button button;
    private ProgressBar progress;
    private TextView messageView;
    private ImageView migrateButton;

    private ListView listView;
    private ModeListAdapter listAdapter;

    private List<Mode> modes = new ArrayList<>();
    protected IConverter converter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.converter_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (MainActivity)getActivity();

        button = view.findViewById(R.id.convert);
        progress = view.findViewById(R.id.progressBar);
        listView = view.findViewById(R.id.listView);
        messageView = view.findViewById(R.id.messageView);
        migrateButton = view.findViewById(R.id.migrate);

        buttonText = getString(R.string.analyze); // Default value

        init();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected void init() {
        if(!modes.isEmpty()) {
            buttonText = getString(R.string.convert);
            listAdapter = new ModeListAdapter(activity, modes);
            listView.setAdapter(listAdapter);
        }

        if(isAnalyzing || isConverting || isMigrating) {
            button.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }

        button.setText(buttonText);
        messageView.setText(messageText);

        String dir = activity.bttrDir().getAbsolutePath();
        if(activity.trDir().exists()) {
            migrateButton.setVisibility(View.VISIBLE);
            migrateButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    showMigrationDialog();
                }
            });
            showMigrationDialog();
        } else {
            migrateButton.setVisibility(View.GONE);
        }

        try {
            converter = new Converter(dir);
            button.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    if (modes.isEmpty())
                        analyze();
                    else
                        convert();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    protected void analyze() {
        AnalyserTask analyserTask = new AnalyserTask(ConverterFragment.this);
        analyserTask.execute();
    }

    @Override
    public Void startAnalyze() {
        converter.analyze();
        return null;
    }

    @Override
    public Void analyzeStarted() {
        isAnalyzing = true;
        messageText = "";
        buttonText = getString(R.string.analyzing);

        button.setEnabled(false);
        button.setText(buttonText);
        progress.setVisibility(View.VISIBLE);
        messageView.setText(messageText);
        migrateButton.setVisibility(View.GONE);
        return null;
    }

    @Override
    public Void analyzeDone() {
        isAnalyzing = false;
        buttonText = getString(R.string.convert);

        modes = converter.getModes();
        button.setEnabled(true);
        button.setText(buttonText);
        listView.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);

        if(activity.trDir().exists()) {
            migrateButton.setVisibility(View.VISIBLE);
        }

        if(modes.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.empty_modes, Toast.LENGTH_SHORT).show();
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
            listAdapter = new ModeListAdapter(activity, modes);
            listView.setAdapter(listAdapter);
        }
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
            ConverterTask converterTask = new ConverterTask(ConverterFragment.this);
            converterTask.execute();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), R.string.set_modes, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Integer startConversion() {
        return converter.execute();
    }

    @Override
    public Void conversionStarted() {
        isConverting = true;
        messageText = "";
        buttonText = getString(R.string.processing);

        button.setEnabled(false);
        button.setText(buttonText);
        listView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        messageView.setText(messageText);
        migrateButton.setVisibility(View.GONE);
        return null;
    }

    @Override
    public Void conversionDone(final Integer result) {
        isConverting = false;
        buttonText = getString(R.string.analyze);
        modes.clear();
        converter.setModes(modes);
        converter.setDateTimeDir();
        button.setEnabled(true);
        button.setText(buttonText);
        progress.setVisibility(View.GONE);

        if(activity.trDir().exists()) {
            migrateButton.setVisibility(View.VISIBLE);
        }

        if(result >= 0) {
            messageText = getString(R.string.conversion_complete, result);
            messageView.setText(messageText);
        } else {
            messageText = getString(R.string.error_occurred);
            messageView.setText(messageText);
        }
        messageView.setTextColor(Color.BLACK);
        System.out.println("Finished!");
        return null;
    }

    @Override
    public void onMigrate() {
        MigrationTask migrationTask = new MigrationTask(ConverterFragment.this);
        migrationTask.execute();
    }

    @Override
    public Void startMigration() {
        System.out.println("Migration started.....");
        if(activity.bttrDir().exists()) {
            messageText = getString(R.string.migration_error);
            messageView.setText(messageText);
            return null;
        }

        if(activity.trDir().renameTo(activity.bttrDir())) {
            messageText = getString(R.string.migration_success);
            messageView.setTextColor(getResources().getColor(R.color.colorAccent));
        }

        return null;
    }

    @Override
    public Void migrationStarted() {
        isMigrating = true;
        messageText = "";
        buttonText = getString(R.string.migrating);

        button.setEnabled(false);
        button.setText(buttonText);
        progress.setVisibility(View.VISIBLE);
        messageView.setText(messageText);
        migrateButton.setVisibility(View.GONE);
        return null;
    }

    @Override
    public Void migrationDone() {
        isMigrating = false;
        button.setEnabled(true);
        buttonText = getString(R.string.analyze);
        progress.setVisibility(View.GONE);
        init();
        return null;
    }

    private void showMigrationDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        MigrationDialogFragment dialog = new MigrationDialogFragment();
        dialog.setMigrationCallback(this);
        dialog.show(fm, "migrate");
    }

    @Override
    public void onResume() {
        super.onResume();
        analyze();
    }
}
