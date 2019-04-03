package org.wycliffeassociates.converter;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.wycliffeassociates.trConverter.Converter;
import org.wycliffeassociates.trConverter.IConverter;
import org.wycliffeassociates.trConverter.Mode;

import java.util.ArrayList;
import java.util.List;

public class ConverterFragment extends Fragment implements ConverterTask.ConverterResultCallback,
        AnalyserTask.AnaliserResultCallback {

    private boolean isAnalyzing = false;
    private boolean isConverting = false;
    private String messageText = "";
    private String buttonText = "";

    private Button button;
    private ProgressBar progress;
    private TextView messageView;

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

        button = view.findViewById(R.id.convert);
        progress = view.findViewById(R.id.progressBar);
        listView = view.findViewById(R.id.listView);
        messageView = view.findViewById(R.id.messageView);

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
            listAdapter = new ModeListAdapter(getActivity(), modes);
            listView.setAdapter(listAdapter);
        }
        return null;
    }

    @Override
    public Integer startConversion() {
        return converter.execute();
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
    public Void conversionStarted() {
        isConverting = true;
        messageText = "";
        buttonText = getString(R.string.processing);

        button.setEnabled(false);
        button.setText(buttonText);
        listView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        messageView.setText(messageText);
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

    protected void init() {
        if(!modes.isEmpty()) {
            buttonText = getString(R.string.convert);
            listAdapter = new ModeListAdapter(getActivity(), modes);
            listView.setAdapter(listAdapter);
        }

        if(isAnalyzing || isConverting) {
            button.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }

        button.setText(buttonText);
        messageView.setText(messageText);

        try {
            String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
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
}
