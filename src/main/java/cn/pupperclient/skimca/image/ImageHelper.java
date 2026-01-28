package cn.pupperclient.skimca.image;

import cn.pupperclient.skimca.SkimcaLogger;
import cn.pupperclient.skimca.context.SkiaContext;
import cn.pupperclient.skimca.utils.SkimcaUtils;
import io.github.humbleui.skija.ColorType;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.SurfaceOrigin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A helper class for loading and caching images from various sources.
 * Supports loading from OpenGL textures, Minecraft resources, file paths, and File objects.
 */
public class ImageHelper {

    /** Cache of images loaded from files or resources, keyed by their path or identifier. */
    private final Map<String, Image> images = new HashMap<>();

    /** Cache of images created from OpenGL textures, keyed by texture ID. */
    private final Map<Integer, Image> textures = new HashMap<>();

    /**
     * Loads an image from an OpenGL texture and caches it.
     *
     * @param texture the OpenGL texture ID
     * @param width   the width of the texture
     * @param height  the height of the texture
     * @param origin  the surface origin (e.g., TOP_LEFT, BOTTOM_LEFT)
     * @return {@code true} if the texture was successfully loaded or already cached
     */
    public boolean load(int texture, float width, float height, SurfaceOrigin origin) {
        if (!textures.containsKey(texture)) {
            Image image = Image.adoptGLTextureFrom(SkiaContext.getContext(), texture, GL11.GL_TEXTURE_2D,
                    (int) width, (int) height, GL11.GL_RGBA8, origin, ColorType.RGBA_8888);
            textures.put(texture, image);
        }
        return true;
    }

    /**
     * Loads an image from a Minecraft resource identifier and caches it.
     *
     * @param identifier the Minecraft resource identifier
     * @return {@code true} if the image was successfully loaded, already cached, or if a non-critical error occurred
     */
    public boolean load(Identifier identifier) {
        if (!images.containsKey(identifier.getPath())) {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            Resource resource;
            try {
                resource = resourceManager.getResourceOrThrow(identifier);
                try (InputStream inputStream = resource.getInputStream()) {
                    byte[] imageData = inputStream.readAllBytes();
                    Image image = Image.makeDeferredFromEncodedBytes(imageData);
                    images.put(identifier.getPath(), image);
                    return true;
                } catch (IOException e) {
                    SkimcaLogger.error("ImageHelper", "Failed to read resource: " + identifier, e);
                }
            } catch (FileNotFoundException e) {
                SkimcaLogger.warn("ImageHelper", "Resource not found: " + identifier);
            }
        }
        return true;
    }

    /**
     * Loads an image from a file path and caches it.
     *
     * @param filePath the path to the image file
     * @return {@code true} if the image was successfully loaded or already cached;
     *         {@code false} if the file could not be read or decoded
     */
    public boolean load(String filePath) {
        if (!images.containsKey(filePath)) {
            Optional<byte[]> encodedBytes = SkimcaUtils.convertToBytes(filePath);
            if (encodedBytes.isPresent()) {
                images.put(filePath, Image.makeDeferredFromEncodedBytes(encodedBytes.get()));
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Loads an image from a File object and caches it.
     *
     * @param file the image file
     * @return {@code true} if the image was successfully loaded or already cached;
     *         {@code false} if the file could not be read or decoded
     */
    public boolean load(File file) {
        if (!images.containsKey(file.getName())) {
            try {
                byte[] encoded = org.apache.commons.io.IOUtils.toByteArray(new FileInputStream(file));
                images.put(file.getName(), Image.makeDeferredFromEncodedBytes(encoded));
                return true;
            } catch (IOException e) {
                SkimcaLogger.error("ImageHelper", "Failed to load image from file: " + file.getAbsolutePath(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves a cached image by its path or identifier.
     *
     * @param path the path or identifier used when loading the image
     * @return the cached {@link Image}, or {@code null} if not found
     */
    public Image get(String path) {
        return images.get(path);
    }

    /**
     * Retrieves a cached image by its OpenGL texture ID.
     *
     * @param texture the OpenGL texture ID
     * @return the cached {@link Image}, or {@code null} if not found
     */
    public Image get(int texture) {
        return textures.get(texture);
    }
}