package bible.translationtools.converter

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import bible.translationtools.converter.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        toolbar.title = getString(R.string.app_info)
        setSupportActionBar(toolbar)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()

        val fragment = fm.findFragmentById(R.id.fragment_container)

        if (fragment == null) {
            ft.replace(R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        return super.onCreateView(name, context, attrs)
    }
}
