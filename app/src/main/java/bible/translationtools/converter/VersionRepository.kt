package bible.translationtools.converter

import javax.inject.Inject

class VersionRepository @Inject constructor() {
    val versionList: ArrayList<Version> = ArrayList<Version>()

    init {
        versionList.add(Version("ulb", "Unlocked Literal Bible"))
        versionList.add(Version("udb", "Unlocked Dynamic Bible"))
        versionList.add(Version("reg", "Regular"))
        versionList.add(Version("v4", "Version 4 (OBS)"))
    }

    fun getVersion(slug: String): Version? {
        for (v in versionList) {
            if (v.slug == slug) {
                return v
            }
        }

        return null
    }
}
