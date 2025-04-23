package bible.translationtools.converter

data class Version(val slug: String, val name: String) {
    override fun equals(other: Any?): Boolean {
        return this.slug == (other as Version).slug
    }
    override fun hashCode(): Int {
        var result = slug.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}
