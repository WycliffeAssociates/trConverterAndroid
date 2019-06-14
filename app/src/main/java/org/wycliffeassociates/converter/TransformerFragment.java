package org.wycliffeassociates.converter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.wycliffeassociates.trConverter.ITransformer;
import org.wycliffeassociates.trConverter.Transformer;

public class TransformerFragment extends Fragment implements TransformerTask.TransformerResultCallback {

    private String project;
    private String language;
    private String version;
    private String book;
    private boolean isTransforming = false;
    private String messageText = "";
    private String buttonText = "";

    private String newLanguage;
    private String newVersion;

    private Button button;
    private ProgressBar progress;
    private TextView projectTitle;
    private TextView messageView;
    private EditText languageEdit;
    private EditText versionEdit;

    protected ITransformer transformer;

    public static TransformerFragment newInstance(String project, String language, String version, String book) {
        Bundle bundle = new Bundle();
        bundle.putString("project", project);
        bundle.putString("language", language);
        bundle.putString("version", version);
        bundle.putString("book", book);

        TransformerFragment fragment = new TransformerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            project = bundle.getString("project");
            language = bundle.getString("language");
            version = bundle.getString("version");
            book = bundle.getString("book");
        }
    }

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

        readBundle(getArguments());

        System.out.println(language);

        return inflater.inflate(R.layout.transformer_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        button = view.findViewById(R.id.transform);
        progress = view.findViewById(R.id.progressBar);
        projectTitle = view.findViewById(R.id.titleView);
        languageEdit = view.findViewById(R.id.languageEdit);
        versionEdit = view.findViewById(R.id.versionEdit);
        messageView = view.findViewById(R.id.messageView);

        buttonText = getString(R.string.transform); // Default value

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

    @Override
    public Integer startTransformation() {
        return transformer.execute();
    }

    protected void transform() {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TranslationRecorder";
        newLanguage = languageEdit.getText().toString().trim();
        newVersion = versionEdit.getText().toString().trim().toLowerCase();

        newLanguage = !newLanguage.isEmpty() && !newLanguage.equals(language) ? newLanguage : null;
        newVersion = !newVersion.isEmpty() && !newVersion.equals(version) ? newVersion : null;

        try {
            transformer = new Transformer(dir, language, newLanguage, null, newVersion);
            TransformerTask transformerTask = new TransformerTask(TransformerFragment.this);
            transformerTask.execute();
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public Void transformationStarted() {
        isTransforming = true;
        messageText = "";
        buttonText = getString(R.string.processing);

        button.setEnabled(false);
        button.setText(buttonText);
        languageEdit.setVisibility(View.GONE);
        versionEdit.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        messageView.setText(messageText);
        return null;
    }

    @Override
    public Void transformationDone(final Integer result) {
        isTransforming = false;
        buttonText = getString(R.string.transform);
        transformer.setDateTimeDir();
        button.setEnabled(true);
        button.setText(buttonText);
        progress.setVisibility(View.GONE);
        languageEdit.setVisibility(View.VISIBLE);
        versionEdit.setVisibility(View.VISIBLE);

        if(result >= 0) {
            messageText = getString(R.string.transformation_complete, result);
            messageView.setText(messageText);

            language = newLanguage != null ? newLanguage : language;
            version = newVersion != null ? newVersion : version;
            project = String.format("%s | %s | %s",
                    language, version, book);

            projectTitle.setText(project);
            languageEdit.setText(language);
            versionEdit.setText(version);

            getArguments().putString("project", project);
            getArguments().putString("language", language);
            getArguments().putString("version", version);
        } else {
            messageText = getString(R.string.error_occurred);
            messageView.setText(messageText);
        }
        messageView.setTextColor(Color.BLACK);
        System.out.println("Finished!");
        return null;
    }

    protected void init() {
        projectTitle.setText(project);
        languageEdit.setText(language);
        versionEdit.setText(version);

        if(isTransforming) {
            button.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            languageEdit.setVisibility(View.GONE);
            versionEdit.setVisibility(View.GONE);
        }

        button.setText(buttonText);
        messageView.setText(messageText);

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                transform();
            }
        });
    }
}
