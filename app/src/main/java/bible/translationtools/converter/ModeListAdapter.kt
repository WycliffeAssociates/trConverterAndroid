package bible.translationtools.converter

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import bible.translationtools.converter.databinding.ModeListCellBinding
import bible.translationtools.converterlib.Project

class ModeListAdapter : BaseAdapter() {
    interface OnEditProjectListener {
        fun onEdit(project: Project)
        fun onExport(project: Project)
    }

    private var listener: OnEditProjectListener? = null
    private val projects = mutableListOf<Project>()

    fun setProjects(projects: MutableList<Project>) {
        this.projects.clear()
        this.projects.addAll(projects)
        notifyDataSetChanged()
    }

    fun setListener(listener: OnEditProjectListener) {
        this.listener = listener
    }

    override fun getCount(): Int {
        return projects.size
    }

    override fun getItem(position: Int): Project? {
        return projects[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getViewTypeCount(): Int {
        return projects.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun isEnabled(position: Int): Boolean {
        return super.isEnabled(position)
    }

    private inner class ViewHolder(val binding: ModeListCellBinding) {
        private var isEmpty: Boolean? = null

        fun bind(position: Int) {
            val item = projects[position]
            isEmpty = item.mode.isEmpty()

            binding.projectTextView.text = item.toString()
            if (isEmpty == false) {
                binding.projectTextView.setTextColor(if (item.shouldUpdate) Color.RED else Color.GRAY)
                binding.editProject.setOnClickListener {
                    listener?.onEdit(item)
                }
                binding.exportProject.setOnClickListener {
                    listener?.onExport(item)
                }
            } else {
                binding.projectTextView.setTextColor(if (isEmpty == true) Color.RED else Color.BLACK)
                binding.projectTextView.setTypeface(null, Typeface.BOLD)
                binding.verseRadio.setTextColor(if (isEmpty == true) Color.RED else Color.BLACK)
                binding.verseRadio.setTypeface(null, Typeface.BOLD)
                binding.chunkRadio.setTextColor(if (isEmpty == true) Color.RED else Color.BLACK)
                binding.chunkRadio.setTypeface(null, Typeface.BOLD)
                binding.editProject.setEnabled(false)
            }

            binding.verseRadio.setChecked(item.mode == "verse")
            binding.verseRadio.setOnClickListener {
                item.mode = "verse"
                item.shouldUpdate = true
                binding.projectTextView.setTextColor(Color.BLACK)
                binding.verseRadio.setTextColor(Color.BLACK)
                binding.chunkRadio.setTextColor(Color.BLACK)
            }

            binding.chunkRadio.setChecked(item.mode == "chunk")
            binding.chunkRadio.setOnClickListener {
                item.mode = "chunk"
                item.shouldUpdate = true
                binding.projectTextView.setTextColor(Color.BLACK)
                binding.verseRadio.setTextColor(Color.BLACK)
                binding.chunkRadio.setTextColor(Color.BLACK)
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ModeListCellBinding
        val viewHolder: ViewHolder

        if (convertView == null) {
            binding = ModeListCellBinding.inflate(LayoutInflater.from(parent.context))
            viewHolder = ViewHolder(binding)
            binding.root.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        viewHolder.bind(position)

        return viewHolder.binding.root
    }
}
