package models;

import javafx.scene.image.Image;

/**
 * Created by daniel on 24.10.14.
 */
public enum Language {
    ENGLISH("English", "en", "i18n/i18n_en.properties", new Image(Language.class.getClassLoader().getResourceAsStream("icons/flags/us.png"))),
    GERMAN("Deutsch", "de", "i18n/i18n_de.properties", new Image(Language.class.getClassLoader().getResourceAsStream("icons/flags/de.png")));

    /**
     * Gibt ein Sprach-Enum zurück, das dem iso639-1 code <code>isoCode</code> entspricht.
     * @param isoCode Die gesuchte Sprache im iso639-1 format
     * @return Die Sprache oder <code>null</code>, wenn nicht verfügbar.
     */
    public static Language getLanguageByTag(String isoCode) {
        for(Language l : Language.values()) {
            if(l.getTag().toLowerCase().equals(isoCode))
                return l;
        }

        return null;
    }

    private String name;
    private String tag;
    private String resourceBundleFileName;
    private Image icon;

    Language(String name, String tag, String resourceBundleFileName, Image icon) {
        this.name = name;
        this.tag = tag;
        this.resourceBundleFileName = resourceBundleFileName;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public String getResourceBundleFileName() {
        return resourceBundleFileName;
    }

    public Image getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return getName();
    }
}
