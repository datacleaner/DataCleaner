package org.eobjects.datacleaner.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceManager {

	private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

	private static ResourceManager instance = new ResourceManager();

	public static ResourceManager getInstance() {
		return instance;
	}

	private ResourceManager() {
	}

	public List<URL> getUrls(String path) {
		List<URL> result = new LinkedList<URL>();
		URL url = getClass().getResource(path);
		if (url != null) {
			result.add(url);
		}

		try {
			Enumeration<URL> resources = ClassLoader.getSystemResources(path);
			while (resources.hasMoreElements()) {
				result.add(resources.nextElement());
			}
		} catch (IOException e) {
			logger.error("IOException when investigating system resources", e);
		}

		// in Java Web Start mode the getSystemResource will return null
		try {
			Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
			while (resources.hasMoreElements()) {
				result.add(resources.nextElement());
			}
		} catch (IOException e) {
			logger.error("IOException when investigating context classloader resources", e);
		}

		// when running in eclipse this file-based hack is nescesary
		File file = new File("src/main/resources/" + path);
		if (file.exists()) {
			try {
				result.add(file.toURI().toURL());
			} catch (IOException e) {
				logger.error("IOException when adding File-based resource to URLs", e);
			}
		}

		return result;
	}

	public URL getUrl(String path) {
		List<URL> urls = getUrls(path);
		if (urls.isEmpty()) {
			return null;
		}
		return urls.get(0);
	}
}
