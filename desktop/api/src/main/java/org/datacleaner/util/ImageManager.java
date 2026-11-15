/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.util;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.common.cache.Cache;

/**
 * The {@link ImageManager} class serves as a utility for fetching images used in DataCleaner.
 */
public final class ImageManager {

    private static final Logger logger = LoggerFactory.getLogger(ImageManager.class);

    private static final ImageManager instance = new ImageManager();

    private final Cache<String, Image> _cachedImageIcons = CollectionUtils2.createCache(500, 5 * 60);
    private final ResourceManager resourceManager = ResourceManager.get();

    private ImageManager() {
    }

    /**
     * Gets the singleton instance of ImageManager.
     *
     * @return
     */
    public static ImageManager get() {
        return instance;
    }

    /**
     * Gets the singleton instance of ImageManager.
     *
     * @return
     *
     * @deprecated use {@link #get()} instead
     */
    @Deprecated
    public static ImageManager getInstance() {
        return get();
    }

    public ImageIcon getImageIcon(final String imagePath, final ClassLoader... classLoaders) {
        if (imagePath.endsWith(".gif")) {
            // animated gif's will loose their animations if loaded as images
            final URL url = resourceManager.getUrl(imagePath, classLoaders);
            return new ImageIcon(url);
        }
        return new ImageIcon(getImage(imagePath, classLoaders));
    }

    public ImageIcon getImageIcon(final String imagePath, final int newWidth, final ClassLoader... classLoaders) {
        return new ImageIcon(getImage(imagePath, newWidth, classLoaders));
    }

    /**
     * Gets an icon for the given image path, preferring a vector SVG sibling
     * of the referenced raster image if one exists (e.g.
     * {@code images/model/table.svg} for {@code images/model/table.png}). SVG
     * icons are rendered crisp at any size and scale factor, avoiding the
     * blur of scaled raster icons.
     *
     * @param imagePath image path (typically a .png resource)
     * @param size the icon size (width and height) in pixels
     * @param classLoaders optional class loaders to load the resource from
     * @return an icon, either vector-based (SVG) or raster-based (fallback)
     */
    public Icon getIcon(final String imagePath, final int size, final ClassLoader... classLoaders) {
        if (imagePath.endsWith(".png")) {
            final String svgPath = imagePath.substring(0, imagePath.length() - ".png".length()) + ".svg";
            final URL svgUrl = resourceManager.getUrl(svgPath, classLoaders);
            if (svgUrl != null) {
                try {
                    return new FlatSVGIcon(svgUrl).derive(size, size);
                } catch (final Exception | LinkageError e) {
                    logger.warn("Failed to load SVG icon {}, falling back to PNG: {}", svgPath, e.getMessage());
                }
            }
        }
        return getImageIcon(imagePath, size, classLoaders);
    }

    public Image getImage(final String imagePath, final ClassLoader... classLoaders) {
        Image image = _cachedImageIcons.getIfPresent(imagePath);
        if (image == null) {
            final URL url = resourceManager.getUrl(imagePath, classLoaders);

            if (url == null && classLoaders != null && classLoaders.length > 0) {
                return getImage(imagePath, new ClassLoader[0]);
            } else if (url == null) {
                logger.warn("Image path ({}) could not be resolved", imagePath);
                throw new IllegalArgumentException(
                        "Could not read image: '" + imagePath + "' (url could not be resolved)");
            } else {
                logger.debug("Image path ({}) resolved to url: {}", imagePath, url);
            }

            try {
                image = ImageIO.read(url);
            } catch (final IOException e) {
                throw new IllegalStateException("Could not read image from url:" + url);
            }
            if (image == null) {
                throw new IllegalArgumentException("Could not read image: " + imagePath + " (url: " + url + ")");
            }
        }
        _cachedImageIcons.put(imagePath, image);
        return image;
    }

    public Image getImage(final String imagePath, final int newWidth, final ClassLoader... classLoaders) {
        Image image = _cachedImageIcons.getIfPresent(imagePath + ",width=" + newWidth);
        if (image == null) {
            image = getImage(imagePath, classLoaders);
            final int width = image.getWidth(null);
            final int height = image.getHeight(null);
            final int newHeight = newWidth * height / width;
            image = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            _cachedImageIcons.put(imagePath + ",width=" + newWidth, image);
        }
        return image;
    }

    public Image getImageFromCache(final String key) {
        return _cachedImageIcons.getIfPresent(key);
    }

    public void storeImageIntoCache(final String key, final Image image) {
        _cachedImageIcons.put(key, image);
    }
}
