package org.eobjects.datacleaner.util;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImageManager {

	private static final Logger logger = LoggerFactory.getLogger(ImageManager.class);

	private static ImageManager instance = new ImageManager();

	private final Map<String, Image> _cachedImageIcons = new WeakHashMap<String, Image>();

	public static ImageManager getInstance() {
		return instance;
	}

	private ImageManager() {
	}

	public ImageIcon getImageIcon(String imagePath) {
		return new ImageIcon(getImage(imagePath));
	}

	public Icon getImageIcon(String imagePath, int newWidth) {
		return new ImageIcon(getImage(imagePath, newWidth));
	}

	public Image getImage(String imagePath) {
		Image image = _cachedImageIcons.get(imagePath);
		if (image == null) {
			ResourceManager resourceManager = ResourceManager.getInstance();
			URL url = resourceManager.getUrl(imagePath);

			if (url == null) {
				logger.warn("Image path ({}) could not be resolved", imagePath);
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
		return image;
	}

	public Image getImage(String imagePath, int newWidth) {
		Image image = _cachedImageIcons.get(imagePath + ",width=" + newWidth);
		if (image == null) {
			image = getImage(imagePath);
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
