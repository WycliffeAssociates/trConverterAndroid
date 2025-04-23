package bible.translationtools.converter;

import java.util.ArrayList;

public class VersionRepository {

    public static final ArrayList<Version> versionList = new ArrayList<>();

    static {
        versionList.add(new Version("ulb", "Unlocked Literal Bible"));
        versionList.add(new Version("udb", "Unlocked Dynamic Bible"));
        versionList.add(new Version("reg", "Regular"));
        versionList.add(new Version("v4", "Version 4 (OBS)"));
    }

    static Version getVersion(String slug) {
        for (Version v: versionList) {
            if(v.slug.equals(slug)) {
                return v;
            }
        }

        return null;
    }
}
