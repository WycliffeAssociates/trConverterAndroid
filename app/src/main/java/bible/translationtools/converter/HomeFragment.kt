package bible.translationtools.converter

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRetainInstance(true)

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

        binding.openFolder.setOnClickListener {
            openDirectory?.launch(null)
        }

        binding.openWorkspace.setOnClickListener {
            val fm = parentFragmentManager
            val ft = fm.beginTransaction()
            val fragment: Fragment = ConverterFragment()
            ft.replace(R.id.fragment_container, fragment)
            ft.addToBackStack(null)
            ft.commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadWorkspace(uri: Uri) {
        try {
            FileUtils.deleteRecursive(directoryProvider.workspaceDir)
            FileUtils.copyDirectory(requireContext(), uri, directoryProvider.workspaceDir)

            val fm = parentFragmentManager
            val ft = fm.beginTransaction()
            val fragment: Fragment = ConverterFragment()
            ft.replace(R.id.fragment_container, fragment)
            ft.addToBackStack(null)
            ft.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
