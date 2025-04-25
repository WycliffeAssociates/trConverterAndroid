package bible.translationtools.converter

class Language(val slug: String, val name: String, val angName: String) {
    override fun equals(other: Any?): Boolean {
        return this.slug == (other as Language).slug
    }

    override fun toString(): String {
        val name = String.format("[ %s ] %s", this.slug, this.name)
        val angName = if (this.angName != this.name && this.angName != "") {
            " ( ${this.angName} )"
        } else ""
        return name + angName
    }

    override fun hashCode(): Int {
        var result = slug.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + angName.hashCode()
        return result
    }
}
