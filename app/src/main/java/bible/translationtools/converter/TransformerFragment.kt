package bible.translationtools.converter

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import bible.translationtools.converter.databinding.TransformerFragmentBinding
import bible.translationtools.converterlib.ITransformer
import bible.translationtools.converterlib.Project
import bible.translationtools.converterlib.Transformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransformerFragment : Fragment() {

    @Inject lateinit var directoryProvider: DirectoryProvider
    @Inject lateinit var langRepo: LanguageRepository
    @Inject lateinit var versionRepo: VersionRepository
    @Inject lateinit var transform: Transform

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

    private var transformer: ITransformer? = null

    private var _binding: TransformerFragmentBinding? = null
    private val binding get() = _binding!!

    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = TransformerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonText = getString(R.string.transform) // Default value
        binding.languages.setTitle(getString(R.string.select_language))

        try {
            readBundle(requireArguments())
            init()
        } catch (e: Exception) {
            finishWithError(e.message ?: "Invalid parameters")
        }
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
            transformationStarted()

            transformer = Transformer(
                directoryProvider.workspaceDir.absolutePath,
                language.slug,
                version.slug,
                (if (!transformAll) book else null),
                newLanguageSlug,
                null,
                newVersionSlug,
                false
            )

            val handler = Handler(Looper.getMainLooper())
            uiScope.launch(Dispatchers.IO) {
                val result = transform(transformer!!)
                handler.post { transformationDone(result.count) }
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun transformationStarted() {
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

    private fun transformationDone(result: Int) {
        isTransforming = false
        buttonText = getString(R.string.transform)
        transformer?.setDateTimeDir()
        binding.transform.isEnabled = true
        binding.transform.text = buttonText
        binding.progressBar.visibility = View.GONE
        binding.languages.visibility = View.VISIBLE
        binding.versions.visibility = View.VISIBLE

        if (result >= 0) {
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
        projectName = bundle.getString("projectName") ?: throw Exception("Project name not defined")

        val langArg = bundle.getString("language") ?: throw Exception("Language not defined")
        val lang = langRepo.getLang(langArg)

        if (lang == null) {
            throw Exception("Language not found")
        }

        language = Language(lang.slug, lang.name, lang.angName)

        val verArg = bundle.getString("version") ?: throw Exception("Version not defined")
        val ver = versionRepo.getVersion(verArg)

        if (ver == null) {
            throw Exception("Version not found")
        }

        version = Version(ver.slug, ver.name)
        book = bundle.getString("book") ?: throw Exception("Book not defined")

        projectsName = String.format("%s | %s", langArg, verArg)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun finishWithError(error: String) {
        val bundle = Bundle()
        bundle.putString(TRANSFORMER_ERROR_KEY, error)
        parentFragmentManager.setFragmentResult(TRANSFORMER_ID, bundle)
        parentFragmentManager.popBackStack()
    }

    companion object {
        const val TRANSFORMER_ID = "transformer"
        const val TRANSFORMER_ERROR_KEY = "error"

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
