package bible.translationtools.converter;

public class Version {
    String slug;
    String name;

    public Version(String slug, String name) {
        this.slug = slug;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return this.slug.equals(((Version) obj).slug);
    }
}
