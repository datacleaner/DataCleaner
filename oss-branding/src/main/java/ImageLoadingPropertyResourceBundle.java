import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This loads images from a property file containing resource paths.
 */
public class ImageLoadingPropertyResourceBundle extends ResourceBundle {
    static final Logger logger = LoggerFactory.getLogger(ImageLoadingPropertyResourceBundle.class);

    final Map<String, String> paths;

    @SuppressWarnings("unchecked")
    public ImageLoadingPropertyResourceBundle(final URL url) throws IOException {
        final Properties properties = new Properties();
        properties.load(url.openStream());
        paths = new HashMap(properties);
    }

    @Override
    protected Object handleGetObject(final String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        final String path = paths.get(key);
        if(path == null) {
            logger.warn("No such key {}", key);
            return null;
        }

        try {
            return ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(path));
        } catch (final IOException e) {
            logger.warn("Could not load image with key {} and path {}", key, path, e);
            return null;
        }
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }

    @Override
    protected Set<String> handleKeySet() {
        return paths.keySet();
    }
}
