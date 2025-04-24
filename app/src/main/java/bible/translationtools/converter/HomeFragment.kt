package bible.translationtools.converter

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.fragment.app.Fragment
import bible.translationtools.converter.databinding.HomeFragmentBinding
import bible.translationtools.converter.di.DirectoryProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject lateinit var directoryProvider: DirectoryProvider

    private var openDirectory: ActivityResultLauncher<Uri?>? = null

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDirectory = registerForActivityResult(OpenDocumentTree()) { uri: Uri? ->
            uri?.let(::loadWorkspace)
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

    private fun loadWorkspace(uri: Uri) {
        try {
            FileUtils.deleteRecursive(directoryProvider.workspaceDir)
            FileUtils.copyDirectory(requireContext(), uri, directoryProvider.workspaceDir)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ConverterFragment())
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
