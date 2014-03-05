/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.util;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.eobjects.analyzer.util.CollectionUtils2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;

/**
 * The {@link ImageManager} class serves as a utility for fetching images used
 * in DataCleaner.
 */
public final class ImageManager {

    private static final Logger logger = LoggerFactory.getLogger(ImageManager.class);

    private static ImageManager instance = new ImageManager();

    private final Cache<String, Image> _cachedImageIcons = CollectionUtils2.createCache(500, 5 * 60);
    private final ResourceManager resourceManager = ResourceManager.getInstance();

    public static ImageManager getInstance() {
        return instance;
    }

    private ImageManager() {
    }

    public ImageIcon getImageIcon(String imagePath, ClassLoader... classLoaders) {
        if (imagePath.endsWith(".gif")) {
            // animated gif's will loose their animations if loaded as images
            URL url = resourceManager.getUrl(imagePath, classLoaders);
            return new ImageIcon(url);
        }
        return new ImageIcon(getImage(imagePath, classLoaders));
    }

    public ImageIcon getImageIcon(String imagePath, int newWidth, ClassLoader... classLoaders) {
        return new ImageIcon(getImage(imagePath, newWidth, classLoaders));
    }

    public Image getImage(String imagePath, ClassLoader... classLoaders) {
        Image image = _cachedImageIcons.getIfPresent(imagePath);
        if (image == null) {
            URL url = resourceManager.getUrl(imagePath, classLoaders);

            if (url == null) {
                logger.warn("Image path ({}) could not be resolved", imagePath);
                throw new IllegalArgumentException("Could not read image: '" + imagePath
                        + "' (url could not be resolved)");
            } else {
                logger.debug("Image path ({}) resolved to url: {}", imagePath, url);
            }

            try {
                image = ImageIO.read(url);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read image from url:" + url);
            }
            if (image == null) {
                throw new IllegalArgumentException("Could not read image: " + imagePath + " (url: " + url + ")");
            }
        }
        _cachedImageIcons.put(imagePath, image);
        return image;
    }

    public Image getImage(String imagePath, int newWidth, ClassLoader... classLoaders) {
        Image image = _cachedImageIcons.getIfPresent(imagePath + ",width=" + newWidth);
        if (image == null) {
            image = getImage(imagePath, classLoaders);
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            if (width > newWidth) {
                int newHeight = newWidth * height / width;
                image = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                _cachedImageIcons.put(imagePath + ",width=" + newWidth, image);
            }
        }
        return image;
    }
}
