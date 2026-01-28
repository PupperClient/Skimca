package cn.pupperclient.skimca.font;

// Represents the type of font file.
// Supports TrueType (TTF) and OpenType (OTF) formats.
public enum FontType {
    /** TrueType Font format. */
    TTF("ttf"),

    /** OpenType Font format. */
    OTF("otf");

    /** The file extension string associated with this font type. */
    private final String string;

    /**
     * Constructs a FontType enum constant.
     *
     * @param string the file extension (e.g., "ttf", "otf")
     */
    private FontType(String string) {
        this.string = string;
    }

    /**
     * Converts a file extension string to the corresponding FontType.
     *
     * @param extension the file extension (case-insensitive)
     * @return the matching {@link FontType}
     * @throws IllegalArgumentException if the extension is not supported
     */
    public static FontType fromString(String extension) {
        String normalizedExtension = extension.toLowerCase();
        return switch (normalizedExtension) {
            case "ttf" -> TTF;
            case "otf" -> OTF;
            default -> throw new IllegalArgumentException("Unsupported font type: " + extension);
        };
    }

    /**
     * Returns the file extension string for this font type.
     *
     * @return the extension as a string (e.g., "ttf", "otf")
     */
    @Override
    public String toString() {
        return string;
    }
}