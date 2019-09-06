package bible.translationtools.converter;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class LanguageRepository {
    private Context context;
    public final ArrayList<Language> languageList = new ArrayList<>();

    public  LanguageRepository(Context context) {
        this.context = context;
        parseJson();
    }

    Language getLang(String slug) {
        for (Language l: languageList) {
            if(l.slug.equals(slug)) {
                return l;
            }
        }

        return null;
    }

    String loadJson() {
        String json;
        try {
            InputStream is = context.getAssets().open("langnames.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    void parseJson() {
        try {
            JSONArray jsonArr = new JSONArray(loadJson());
            for(int i=0; i<jsonArr.length(); i++) {
                JSONObject obj = (JSONObject) jsonArr.get(i);
                languageList.add(new Language(
                        obj.getString("lc"),
                        obj.getString("ln"),
                        obj.getString("ang")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
