package bible.translationtools.converter

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import bible.translationtools.converter.databinding.ConverterFragmentBinding
import bible.translationtools.converterlib.Converter
import bible.translationtools.converterlib.IConverter
import bible.translationtools.converterlib.Project
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ConverterFragment : Fragment(), ModeListAdapter.OnEditProjectListener {

    @Inject lateinit var directoryProvider: DirectoryProvider
    @Inject lateinit var analyze: Analyze
    @Inject lateinit var convert: Convert
    @Inject lateinit var exportProject: ExportProject
    @Inject lateinit var exportBackup: ExportBackup

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

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var projectToExport: Project? = null
    private var selectExportProject: ActivityResultLauncher<String>? = null
    private var selectExportBackup: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectExportProject = registerForActivityResult(CreateDocument("application/zip")) { uri ->
            uri?.let { out ->
                projectToExport?.let { project ->
                    exportProject(project, out)
                }
            }
        }
        selectExportBackup = registerForActivityResult(CreateDocument("application/zip")) { uri ->
            uri?.let { out ->
                doExportBackup(out)
            }
        }
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

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.convert_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    val id = menuItem.itemId
                    return when (id) {
                        R.id.export_backup -> {
                            exportBackup()
                            true
                        }
                        R.id.share_backup -> {
                            shareBackup()
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.STARTED
        )

        init()
    }

    private fun init() {
        buttonText = getString(R.string.analyze) // Default value

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
            converter = Converter(directoryProvider.workspaceDir.absolutePath, false)
            binding.convert.setOnClickListener {
                if (projects.isEmpty()) analyze() else convert()
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        parentFragmentManager.setFragmentResultListener(
            TransformerFragment.TRANSFORMER_ID,
            viewLifecycleOwner
        ) { requestKey, bundle ->
            bundle.getString(TransformerFragment.TRANSFORMER_ERROR_KEY)?.let {
                showMessageDialog(R.string.error_occurred, it)
            }
        }
    }

    private fun analyze() {
        analyzeStarted()
        val handler = Handler(Looper.getMainLooper())
        uiScope.launch(Dispatchers.IO) {
            val result = analyze(converter)
            if (!result.success) {
                result.error?.let {
                    showMessageDialog(R.string.error_occurred, it)
                }
            }
            handler.post { analyzeDone() }
        }
    }

    private fun analyzeStarted() {
        isAnalyzing = true
        messageText = ""
        buttonText = getString(R.string.analyzing)

        binding.convert.isEnabled = false
        binding.convert.text = buttonText
        binding.progressBar.visibility = View.VISIBLE
        binding.messageView.text = messageText
    }

    private fun analyzeDone() {
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

            val handler = Handler(Looper.getMainLooper())
            conversionStarted()

            uiScope.launch(Dispatchers.IO) {
                val result = convert(converter)
                handler.post { conversionDone(result.count) }
            }
        } else {
            Toast.makeText(
                context,
                R.string.set_modes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun conversionStarted() {
        isConverting = true
        messageText = ""
        buttonText = getString(R.string.processing)

        binding.convert.isEnabled = false
        binding.convert.text = buttonText
        binding.listView.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.messageView.text = messageText
    }

    private fun conversionDone(result: Int) {
        isConverting = false
        buttonText = getString(R.string.analyze)
        projects.clear()
        converter.projects = projects
        converter.setDateTimeDir()
        binding.convert.isEnabled = true
        binding.convert.text = buttonText
        binding.progressBar.visibility = View.GONE

        if (result >= 0) {
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
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            .replace(R.id.fragment_container, TransformerFragment.newInstance(project))
            .addToBackStack(null)
            .commit()
    }

    override fun onExport(project: Project) {
        val filename = "${project.language}_${project.version}_${project.book}.zip"
        projectToExport = project
        selectExportProject?.launch(filename)
    }

    private fun exportProject(project: Project, uri: Uri) {
        val handler = Handler(Looper.getMainLooper())
        uiScope.launch(Dispatchers.IO) {
            val result = exportProject.invoke(project, uri)
            if (result.success) {
                handler.post {
                    showMessageDialog(R.string.success, getString(R.string.project_exported))
                }
            } else {
                handler.post {
                    showMessageDialog(
                        R.string.error_occurred,
                        result.error ?: getString(R.string.unknown_error)
                    )
                }
            }
        }
    }

    private fun exportBackup() {
        val date = Date()
        val dateString = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.US).format(date)
        selectExportBackup?.launch("$dateString.zip")
    }

    private fun doExportBackup(uri: Uri) {
        val handler = Handler(Looper.getMainLooper())
        uiScope.launch(Dispatchers.IO) {
            val result = exportBackup.invoke(uri)
            if (result.success) {
                handler.post {
                    showMessageDialog(R.string.success, getString(R.string.backup_exported))
                }
            } else {
                handler.post {
                    showMessageDialog(
                        R.string.error_occurred,
                        result.error ?: getString(R.string.unknown_error)
                    )
                }
            }
        }
    }

    private fun shareBackup() {
        println("share backup")
    }

    private fun showMessageDialog(@StringRes title: Int, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}
