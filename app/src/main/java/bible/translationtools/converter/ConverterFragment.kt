package bible.translationtools.converter

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import bible.translationtools.converter.AnalyserTask.AnalyzerResultCallback
import bible.translationtools.converter.ConverterTask.ConverterResultCallback
import bible.translationtools.converter.databinding.ConverterFragmentBinding
import bible.translationtools.converter.di.DirectoryProvider
import bible.translationtools.converterlib.Converter
import bible.translationtools.converterlib.IConverter
import bible.translationtools.converterlib.Project
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConverterFragment : Fragment(), ConverterResultCallback, AnalyzerResultCallback,
    ModeListAdapter.OnEditProjectListener {

    @Inject lateinit var directoryProvider: DirectoryProvider

    private var isAnalyzing = false
    private var isConverting = false
    private var isMigrating = false
    private var messageText = ""
    private var buttonText = ""

    private val listAdapter = ModeListAdapter()
    private lateinit var converter: IConverter

    private var projects: MutableList<Project> = ArrayList<Project>()

    private var _binding: ConverterFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

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
        _binding = ConverterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonText = getString(R.string.analyze) // Default value
        init()
    }

    private fun init() {
        if (!projects.isEmpty()) {
            buttonText = getString(R.string.convert)
            listAdapter.setListener(this)
            listAdapter.setProjects(projects)
            binding.listView.adapter = listAdapter
        }

        if (isAnalyzing || isConverting || isMigrating) {
            binding.convert.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.listView.visibility = View.GONE
        }

        binding.convert.text = buttonText
        binding.messageView.text = messageText

        try {
            converter = Converter(directoryProvider.workspaceDir.absolutePath)
            binding.convert.setOnClickListener {
                if (projects.isEmpty()) analyze() else convert()
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun analyze() {
        val analyserTask = AnalyserTask(this@ConverterFragment)
        analyserTask.execute()
    }

    override fun startAnalyze() {
        converter.analyze()
    }

    override fun analyzeStarted() {
        isAnalyzing = true
        messageText = ""
        buttonText = getString(R.string.analyzing)

        binding.convert.isEnabled = false
        binding.convert.text = buttonText
        binding.progressBar.visibility = View.VISIBLE
        binding.messageView.text = messageText
    }

    override fun analyzeDone() {
        isAnalyzing = false
        buttonText = getString(R.string.convert)

        projects = converter.projects
        binding.convert.isEnabled = true
        binding.convert.text = buttonText
        binding.listView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE

        if (projects.isEmpty()) {
            Toast.makeText(context, R.string.empty_modes, Toast.LENGTH_SHORT).show()
            binding.convert.setText(R.string.analyze)
        } else {
            var hasEmptyModes = false
            for (m in projects) {
                if (m.mode.isEmpty()) {
                    hasEmptyModes = true
                    break
                }
            }

            if (hasEmptyModes) {
                binding.messageView.setText(R.string.set_modes)
                binding.messageView.setTextColor(Color.RED)
            }

            listAdapter.setListener(this)
            listAdapter.setProjects(projects)
            binding.listView.setAdapter(listAdapter)
        }
    }

    private fun convert() {
        var hasEmptyModes = false
        for (m in projects) {
            if (m.mode.isEmpty()) {
                hasEmptyModes = true
                break
            }
        }

        if (!hasEmptyModes) {
            converter.projects = projects
            val converterTask = ConverterTask(this@ConverterFragment)
            converterTask.execute()
        } else {
            Toast.makeText(
                context,
                R.string.set_modes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun startConversion(): Int {
        return converter.execute()
    }

    override fun conversionStarted() {
        isConverting = true
        messageText = ""
        buttonText = getString(R.string.processing)

        binding.convert.isEnabled = false
        binding.convert.text = buttonText
        binding.listView.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.messageView.text = messageText
    }

    override fun conversionDone(result: Int?) {
        isConverting = false
        buttonText = getString(R.string.analyze)
        projects.clear()
        converter.projects = projects
        converter.setDateTimeDir()
        binding.convert.isEnabled = true
        binding.convert.text = buttonText
        binding.progressBar.visibility = View.GONE

        if (result != null && result >= 0) {
            messageText = getString(R.string.conversion_complete, result)
            binding.messageView.text = messageText
        } else {
            messageText = getString(R.string.error_occurred)
            binding.messageView.text = messageText
        }
        binding.messageView.setTextColor(Color.BLACK)
        println("Finished!")
    }

    override fun onResume() {
        super.onResume()
        analyze()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEdit(project: Project) {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        ft.replace(
            R.id.fragment_container,
            TransformerFragment.newInstance(project)
        )
        ft.addToBackStack(null)
        ft.commit()
    }
}
