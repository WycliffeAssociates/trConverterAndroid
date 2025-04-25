package bible.translationtools.converter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class LanguageSpinnerAdapter(
    context: Context,
    textViewResourceId: Int,
    private val values: MutableList<Language>
) : ArrayAdapter<Language>(context, textViewResourceId, values) {
    override fun getCount(): Int {
        return values.size
    }

    override fun getItem(position: Int): Language? {
        return values[position]
    }

    override fun getPosition(item: Language?): Int {
        return values.indexOf(item)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getView(position, convertView, parent) as TextView
        label.text = values[position].toString()
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getDropDownView(position, convertView, parent) as TextView
        label.text = values[position].toString()
        return label
    }
}
