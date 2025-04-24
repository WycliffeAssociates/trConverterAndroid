package bible.translationtools.converter

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.fragment.app.Fragment
import bible.translationtools.converter.databinding.HomeFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject lateinit var directoryProvider: DirectoryProvider
    @Inject lateinit var extractBackup: ExtractBackup

    private var openDirectory: ActivityResultLauncher<Uri?>? = null
    private var openBackup: ActivityResultLauncher<String?>? = null

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var backPressedCallback: OnBackPressedCallback
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDirectory = registerForActivityResult(OpenDocumentTree()) { uri: Uri? ->
            uri?.let(::importDirectory)
        }
        openBackup = registerForActivityResult(GetContent()) { uri: Uri? ->
            uri?.let(::importBackup)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )

        binding.openFolder.setOnClickListener {
            openDirectory?.launch(null)
        }

        binding.openBackup.setOnClickListener {
            openBackup?.launch("application/zip")
        }

        binding.openWorkspace.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ConverterFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        backPressedCallback.remove()
    }

    private fun importDirectory(uri: Uri) {
        val handler = Handler(Looper.getMainLooper())
        uiScope.launch(Dispatchers.IO) {
            try {
                FileUtils.deleteRecursive(directoryProvider.workspaceDir)
                FileUtils.copyDirectory(requireContext(), uri, directoryProvider.workspaceDir)

                handler.post { loadWorkspace() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun importBackup(uri: Uri) {
        val handler = Handler(Looper.getMainLooper())
        uiScope.launch(Dispatchers.IO) {
            try {
                FileUtils.deleteRecursive(directoryProvider.workspaceDir)

                val result = extractBackup(uri)
                if (result.success) {
                    handler.post { loadWorkspace() }
                } else {
                    println(result.message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadWorkspace() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ConverterFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.exit_app)
            .setMessage(R.string.exit_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                backPressedCallback.isEnabled = false
                requireActivity().finishAffinity()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
