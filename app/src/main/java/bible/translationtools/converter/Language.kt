package bible.translationtools.converter;

public class Language {
    String slug;
    String name;
    String angName;

    public Language(String slug, String name, String angName) {
        this.slug = slug;
        this.name = name;
        this.angName = angName;
    }

    @Override
    public boolean equals(Object obj) {
        return this.slug.equals(((Language) obj).slug);
    }

    @Override
    public String toString() {
        return String.format("[ %s ] %s", this.slug, this.name) +
                (!this.angName.equals(this.name) && !this.angName.equals("") ? " ( "+ this.angName +" )" : "");
    }
}
