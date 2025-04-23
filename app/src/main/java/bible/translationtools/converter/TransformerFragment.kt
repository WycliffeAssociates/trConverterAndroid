package bible.translationtools.converter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import bible.translationtools.converter.TransformerTask.TransformerResultCallback
import bible.translationtools.converter.databinding.TransformerFragmentBinding
import bible.translationtools.converter.di.DirectoryProvider
import bible.translationtools.converterlib.ITransformer
import bible.translationtools.converterlib.Project
import bible.translationtools.converterlib.Transformer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransformerFragment : Fragment(), TransformerResultCallback {

    @Inject lateinit var directoryProvider: DirectoryProvider
    @Inject lateinit var langRepo: LanguageRepository
    @Inject lateinit var versionRepo: VersionRepository

    private lateinit var projectName: String
    private lateinit var projectsName: String // language and version
    private lateinit var language: Language
    private lateinit var version: Version
    private lateinit var book: String

    private var isTransforming = false
    private var messageText = ""
    private var buttonText = ""

    private var newLanguage: Language? = null
    private var newVersion: Version? = null

    private lateinit var versionsAdapter: ArrayAdapter<Version>
    private lateinit var languageAdapter: ArrayAdapter<Language>

    private lateinit var transformer: ITransformer

    private var _binding: TransformerFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRetainInstance(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = TransformerFragmentBinding.inflate(inflater, container, false)

        langRepo = LanguageRepository(requireContext())

        if (arguments == null) {
            throw Exception("Arguments not found")
        }

        readBundle(requireArguments())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonText = getString(R.string.transform) // Default value
        binding.languages.setTitle(getString(R.string.select_language))

        init()
    }

    override fun startTransformation(): Int {
        return transformer.execute()
    }

    private fun transform() {
        newLanguage = binding.languages.selectedItem as Language?
        newVersion = binding.versions.selectedItem as Version?

        newLanguage = if (newLanguage != language) newLanguage else null
        newVersion = if (newVersion != version) newVersion else null

        val newLanguageSlug = newLanguage?.slug
        val newVersionSlug = newVersion?.slug

        val transformAll = binding.allBooksCheckbox.isChecked

        try {
            transformer = Transformer(
                directoryProvider.workspaceDir.absolutePath,
                language.slug,
                version.slug,
                (if (!transformAll) book else null),
                newLanguageSlug,
                null,
                newVersionSlug
            )
            val transformerTask = TransformerTask(this@TransformerFragment)
            transformerTask.execute()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun transformationStarted() {
        isTransforming = true
        messageText = ""
        buttonText = getString(R.string.processing)

        binding.transform.isEnabled = false
        binding.transform.text = buttonText
        binding.languages.visibility = View.GONE
        binding.versions.visibility = View.GONE
        binding.allBooksCheckbox.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.messageView.text = messageText
    }

    override fun transformationDone(result: Int?) {
        isTransforming = false
        buttonText = getString(R.string.transform)
        transformer.setDateTimeDir()
        binding.transform.isEnabled = true
        binding.transform.text = buttonText
        binding.progressBar.visibility = View.GONE
        binding.languages.visibility = View.VISIBLE
        binding.versions.visibility = View.VISIBLE

        if (result != null && result >= 0) {
            messageText = getString(R.string.transformation_complete, result)
            binding.messageView.text = messageText

            language = newLanguage ?: language
            version = newVersion ?: version
            projectName = String.format("%s | %s | %s", language.slug, version.slug, book)
            projectsName = String.format("%s | %s", language.slug, version.slug)

            binding.titleView.text = projectName
            binding.languages.setSelection(languageAdapter.getPosition(language))
            binding.versions.setSelection(versionsAdapter.getPosition(version))
            binding.allBooksCheckbox.text = getString(R.string.all_books_check, projectsName)

            requireArguments().putString("projectName", projectName)
            requireArguments().putString("language", language.slug)
            requireArguments().putString("version", version.slug)
        } else {
            messageText = getString(R.string.error_occurred)
            binding.messageView.text = messageText
        }
        binding.messageView.setTextColor(Color.BLACK)
        println("Finished!")
    }

    private fun init() {
        versionsAdapter = VersionSpinnerAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            versionRepo.versionList
        )
        versionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.versions.adapter = versionsAdapter

        languageAdapter = LanguageSpinnerAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            langRepo.languageList
        )
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languages.adapter = languageAdapter

        binding.titleView.text = projectName
        binding.languages.setSelection(languageAdapter.getPosition(language))
        binding.versions.setSelection(versionsAdapter.getPosition(version))

        if (isTransforming) {
            binding.transform.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.languages.visibility = View.GONE
            binding.versions.visibility = View.GONE
        }

        binding.transform.text = buttonText
        binding.messageView.text = messageText

        binding.allBooksCheckbox.text = getString(R.string.all_books_check, projectsName)

        binding.transform.setOnClickListener { transform() }
    }

    private fun readBundle(bundle: Bundle) {
        projectName = bundle.getString("projectName") ?: ""
        projectsName = String.format(
            "%s | %s",
            bundle.getString("language"),
            bundle.getString("version")
        )

        val lang = langRepo.getLang(bundle.getString("language") ?: "")

        if (lang == null) {
            throw Exception("Language not found")
        }

        language = Language(lang.slug, lang.name, lang.angName)

        val ver = versionRepo.getVersion(bundle.getString("version") ?: "")

        if (ver == null) {
            throw Exception("Version not found")
        }

        version = Version(ver.slug, ver.name)
        book = bundle.getString("book") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(project: Project): TransformerFragment {
            val bundle = Bundle()
            bundle.putString("projectName", project.toString())
            bundle.putString("language", project.language)
            bundle.putString("version", project.version)
            bundle.putString("book", project.book)

            val fragment = TransformerFragment()
            fragment.setArguments(bundle)
            return fragment
        }
    }
}
