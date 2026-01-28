package cn.pupperclient.skimca.font;

import cn.pupperclient.skimca.utils.SkimcaUtils;
import io.github.humbleui.skija.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A helper class for loading and caching fonts using Skija.
 * Provides methods to load fonts from file paths and manage a cache of typefaces.
 */
public class FontHelper {

    /** Cache of loaded typefaces, keyed by font name. */
    private static final Map<String, Typeface> typefaceCache = new HashMap<>();

    /**
     * Retrieves a typeface from the cache or loads it if not present.
     *
     * @param font the name of the font file
     * @param type the font type (e.g., TTF, OTF)
     * @param path the file system path to the font directory
     * @return the loaded or cached {@link Typeface}
     */
    private static Typeface getTypeface(String font, FontType type, String path) {
        return typefaceCache.computeIfAbsent(font, k -> loadTypeface(k, type, path));
    }

    /**
     * Loads a typeface from the given font file.
     *
     * @param font the name of the font file
     * @param type the font type
     * @param path the file system path to the font directory
     * @return the loaded {@link Typeface}
     * @throws IllegalArgumentException if the font file cannot be found or read
     */
    private static Typeface loadTypeface(String font, FontType type, String path) {
        Optional<Data> fontDataOptional = SkimcaUtils.convertToData(path);
        return fontDataOptional.map(data -> FontMgr.getDefault().makeFromData(data))
                .orElseThrow(() -> new IllegalArgumentException("Font not found: " + font));
    }

    /**
     * Loads a font with the specified name, size, type, and path.
     *
     * @param font     the name of the font file (e.g., "arial.ttf")
     * @param size     the font size in points
     * @param fontType the type of the font (e.g., TTF, OTF)
     * @param path     the file system path to the font directory
     * @return a {@link Font} instance with the specified typeface and size
     */
    public static Font load(String font, float size, FontType fontType, String path) {
        Typeface typeface = getTypeface(font, fontType, path);
        return new Font(typeface, size);
    }

    /**
     * Loads a font with the specified name, size, and path.
     * The font type is inferred from the file extension.
     *
     * @param font the name of the font file (e.g., "arial.ttf")
     * @param size the font size in points
     * @param path the file system path to the font directory
     * @return a {@link Font} instance with the inferred typeface and size
     */
    public static Font load(String font, float size, String path) {
        return load(font, size, getFontType(font), path);
    }


    /**
     * Determines the font type from the file extension.
     *
     * @param font the font file name
     * @return the corresponding {@link FontType}
     * @throws IllegalArgumentException if the file extension is not supported
     */
    private static FontType getFontType(String font) {
        String fileExtension = font.substring(font.lastIndexOf('.') + 1).toLowerCase();
        return FontType.fromString(fileExtension);
    }

    /**
     * Clears all cached typefaces.
     */
    public static void clearCache() {
        typefaceCache.clear();
    }

    /**
     * Preloads the specified fonts into the cache.
     *
     * @param path  the file system path to the font directory
     * @param fonts one or more font file names to preload
     */
    public static void preloadFonts(String path, String... fonts) {
        for (String font : fonts) {
            getTypeface(font, getFontType(font), path);
        }
    }
}