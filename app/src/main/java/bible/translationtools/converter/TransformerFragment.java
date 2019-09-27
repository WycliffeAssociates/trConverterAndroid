package bible.translationtools.converter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import bible.translationtools.converterlib.ITransformer;
import bible.translationtools.converterlib.Project;
import bible.translationtools.converterlib.Transformer;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

public class TransformerFragment extends Fragment implements TransformerTask.TransformerResultCallback {

    private MainActivity activity;

    private String projectName;
    private String projectsName; // language and version
    private Language language;
    private Version version;
    private String book;
    private boolean isTransforming = false;
    private String messageText = "";
    private String buttonText = "";

    private Language newLanguage;
    private Version newVersion;

    private Button button;
    private ProgressBar progress;
    private TextView projectTitle;
    private TextView messageView;
    private Spinner versionSpinner;
    private SearchableSpinner languageSpinner;
    private CheckBox allBooksCheckbox;

    private LanguageRepository langRepo;
    private ArrayAdapter<Version> versionsAdapter;
    private ArrayAdapter<Language> languageAdapter;

    protected ITransformer transformer;

    public static TransformerFragment newInstance(Project project) {
        Bundle bundle = new Bundle();
        bundle.putString("projectName", project.toString());
        bundle.putString("language", project.language);
        bundle.putString("version", project.version);
        bundle.putString("book", project.book);

        TransformerFragment fragment = new TransformerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            projectName = bundle.getString("projectName");
            projectsName = String.format("%s | %s", bundle.getString("language"), bundle.getString("version"));

            Language lang = langRepo.getLang(bundle.getString("language"));
            language = new Language(
                    lang.slug,
                    lang.name,
                    lang.angName
            );

            Version ver = VersionRepository.getVersion(bundle.getString("version"));
            version = new Version(
                    ver.slug,
                    ver.name
            );
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

        langRepo = new LanguageRepository(getContext());

        readBundle(getArguments());

        return inflater.inflate(R.layout.transformer_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (MainActivity)getActivity();

        button = view.findViewById(R.id.transform);
        progress = view.findViewById(R.id.progressBar);
        projectTitle = view.findViewById(R.id.titleView);
        languageSpinner = view.findViewById(R.id.languages);
        versionSpinner = view.findViewById(R.id.versions);
        messageView = view.findViewById(R.id.messageView);
        allBooksCheckbox = view.findViewById(R.id.allBooksCheckbox);

        buttonText = getString(R.string.transform); // Default value
        languageSpinner.setTitle(getString(R.string.select_language));

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
        String dir = activity.bttrDir().getAbsolutePath();
        newLanguage = (Language) languageSpinner.getSelectedItem();
        newVersion = (Version) versionSpinner.getSelectedItem();

        newLanguage = !newLanguage.equals(language) ? newLanguage : null;
        newVersion = !newVersion.equals(version) ? newVersion : null;

        String newLanguageSlug = newLanguage != null ? newLanguage.slug : null;
        String newVersionSlug = newVersion != null ? newVersion.slug : null;

        boolean transformAll = allBooksCheckbox.isChecked();

        try {
            transformer = new Transformer(
                    dir,
                    language.slug,
                    version.slug,
                    (!transformAll ? book : null),
                    newLanguageSlug,
                    null,
                    newVersionSlug
            );
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
        languageSpinner.setVisibility(View.GONE);
        versionSpinner.setVisibility(View.GONE);
        allBooksCheckbox.setVisibility(View.GONE);
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
        languageSpinner.setVisibility(View.VISIBLE);
        versionSpinner.setVisibility(View.VISIBLE);

        if(result >= 0) {
            messageText = getString(R.string.transformation_complete, result);
            messageView.setText(messageText);

            language = newLanguage != null ? newLanguage : language;
            version = newVersion != null ? newVersion : version;
            projectName = String.format("%s | %s | %s", language.slug, version.slug, book);
            projectsName = String.format("%s | %s", language.slug, version.slug);

            projectTitle.setText(projectName);
            languageSpinner.setSelection(languageAdapter.getPosition(language));
            versionSpinner.setSelection(versionsAdapter.getPosition(version));
            allBooksCheckbox.setText(getString(R.string.all_books_check, projectsName));

            getArguments().putString("projectName", projectName);
            getArguments().putString("language", language.slug);
            getArguments().putString("version", version.slug);
        } else {
            messageText = getString(R.string.error_occurred);
            messageView.setText(messageText);
        }
        messageView.setTextColor(Color.BLACK);
        System.out.println("Finished!");
        return null;
    }

    protected void init() {
        versionsAdapter = new VersionSpinnerAdapter(
                getContext(),
                android.R.layout.simple_spinner_item,
                VersionRepository.versionList);
        versionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        versionSpinner.setAdapter(versionsAdapter);

        LanguageRepository languageRepository = new LanguageRepository(getContext());
        languageAdapter = new LanguageSpinnerAdapter(
                getContext(),
                android.R.layout.simple_spinner_item,
                languageRepository.languageList
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);

        projectTitle.setText(projectName);
        languageSpinner.setSelection(languageAdapter.getPosition(language));
        versionSpinner.setSelection(versionsAdapter.getPosition(version));

        if(isTransforming) {
            button.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            languageSpinner.setVisibility(View.GONE);
            versionSpinner.setVisibility(View.GONE);
        }

        button.setText(buttonText);
        messageView.setText(messageText);

        allBooksCheckbox.setText(getString(R.string.all_books_check, projectsName));

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                transform();
            }
        });
    }
}
