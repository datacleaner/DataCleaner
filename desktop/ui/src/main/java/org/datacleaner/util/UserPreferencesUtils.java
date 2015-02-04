package org.datacleaner.util;

import java.awt.Dimension;
import java.util.Map;

import org.datacleaner.user.UserPreferences;

public class UserPreferencesUtils {

  //  private static Logger logger = LoggerFactory.getLogger(UserPreferencesUtils.class);

    private final UserPreferences _userPreferences;
    private final String _identifier;
    private final int _defaultWidth;
    private final int _defaultHeight;

    public UserPreferencesUtils( final UserPreferences userPreferences, final String identifier, final int defaultWidth,
            int defaultHeight) {
        _identifier = identifier;
        _defaultWidth= defaultWidth; 
        _defaultHeight= defaultHeight; 
        _userPreferences = userPreferences;
    }

    public UserPreferences getUserPreferences() {
        return _userPreferences;
    }

    public Dimension getPreferredSizeFromUserPreferences() {
        Map<String, String> properties = _userPreferences.getAdditionalProperties();
        String widthStr = properties.get(getWidthPropertyKey());
        if (widthStr == null) {
            widthStr = "" + _defaultWidth;
        }
        String heightStr = properties.get(getHeightPropertyKey());
        if (heightStr == null) {
            heightStr = "" + _defaultHeight;
        }

        return new Dimension(Integer.parseInt(widthStr), Integer.parseInt(heightStr));
    }
    
    public void setUserPreferredMaximizedSize(Dimension size) {
        Map<String, String> properties = _userPreferences.getAdditionalProperties();
        properties.put(getWidthPropertyKey(), "" + size.width);
        properties.put(getHeightPropertyKey(), "" + size.height);
        _userPreferences.save();
    }

    private String getHeightPropertyKey() {
        return getClass().getName() + "." + _identifier + ".height";
    }

    private String getWidthPropertyKey() {
        return getClass().getName() + "." + _identifier + ".width";
    }
}
