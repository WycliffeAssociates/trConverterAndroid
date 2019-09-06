package bible.translationtools.converter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.Nullable;

import java.util.List;

public class LanguageSpinnerAdapter extends ArrayAdapter<Language> {
    private List<Language> values;

    public LanguageSpinnerAdapter(Context context, int textViewResourceId, List<Language> values) {
        super(context, textViewResourceId, values);
        this.values = values;
    }

    @Override
    public int getCount(){
        return values.size();
    }

    @Override
    public Language getItem(int position){
        return values.get(position);
    }

    @Override
    public int getPosition(@Nullable Language item) {
        return values.indexOf(item);
    }

    @Override
    public long getItemId(int position){
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setText(values.get(position).toString());
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setText(values.get(position).toString());
        return label;
    }
}
