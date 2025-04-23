package bible.translationtools.converter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import bible.translationtools.converterlib.Project;

import java.util.List;

public class ModeListAdapter extends BaseAdapter {
    List<Project> projects;
    LayoutInflater layoutInflater;
    FragmentManager fragmentManager;

    public ModeListAdapter(MainActivity activity, List<Project> projects) {
        this.projects = projects;
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        fragmentManager = activity.getSupportFragmentManager();
    }

    @Override
    public int getCount() {
        return projects.size();
    }

    @Override
    public Object getItem(int position) {
        return projects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return projects.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(position);
    }

    private static class ViewHolder {
        TextView projectText;
        RadioGroup radioGroup;
        RadioButton verseButton;
        RadioButton chunkButton;
        Button editButton;

        Boolean isEmpty = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final Project item = projects.get(position);
        Boolean isEmpty = item.mode.isEmpty();

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.mode_list_cell, null);
            viewHolder.projectText = convertView.findViewById(R.id.projectTextView);
            viewHolder.radioGroup = convertView.findViewById(R.id.radioGroup);
            viewHolder.verseButton = convertView.findViewById(R.id.verseRadio);
            viewHolder.chunkButton = convertView.findViewById(R.id.chunkRadio);
            viewHolder.editButton = convertView.findViewById(R.id.editProject);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (viewHolder.isEmpty == null) {
            viewHolder.isEmpty = isEmpty;
        }

        viewHolder.projectText.setText(item.toString());
        if (!viewHolder.isEmpty) {
            viewHolder.projectText.setTextColor(item.pending ? Color.RED : Color.GRAY);

            viewHolder.editButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    ft.replace(
                            R.id.fragment_container,
                            TransformerFragment.newInstance(item)
                    );
                    ft.addToBackStack(null);
                    ft.commit();
                }

            });
        } else {
            viewHolder.projectText.setTextColor(isEmpty ? Color.RED : Color.BLACK);
            viewHolder.projectText.setTypeface(null, Typeface.BOLD);
            viewHolder.verseButton.setTextColor(isEmpty ? Color.RED : Color.BLACK);
            viewHolder.verseButton.setTypeface(null, Typeface.BOLD);
            viewHolder.chunkButton.setTextColor(isEmpty ? Color.RED : Color.BLACK);
            viewHolder.chunkButton.setTypeface(null, Typeface.BOLD);
            viewHolder.editButton.setEnabled(false);
        }

        viewHolder.verseButton.setChecked(item.mode.equals("verse"));
        viewHolder.verseButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                item.mode = "verse";
                item.pending = true;
                viewHolder.projectText.setTextColor(Color.BLACK);
                viewHolder.verseButton.setTextColor(Color.BLACK);
                viewHolder.chunkButton.setTextColor(Color.BLACK);
            }

        });

        viewHolder.chunkButton.setChecked(item.mode.equals("chunk"));
        viewHolder.chunkButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                item.mode = "chunk";
                item.pending = true;
                viewHolder.projectText.setTextColor(Color.BLACK);
                viewHolder.verseButton.setTextColor(Color.BLACK);
                viewHolder.chunkButton.setTextColor(Color.BLACK);
            }

        });

        return convertView;
    }
}
