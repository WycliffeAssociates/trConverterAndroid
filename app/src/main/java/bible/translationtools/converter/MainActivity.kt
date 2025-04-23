package bible.translationtools.converter

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.FragmentActivity
import bible.translationtools.converter.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()

        var fragment = fm.findFragmentById(R.id.fragment_container)

        if (fragment == null) {
            fragment = HomeFragment()
            ft.add(R.id.fragment_container, fragment)
            ft.commit()
        }

        return super.onCreateView(name, context, attrs)
    }
}
