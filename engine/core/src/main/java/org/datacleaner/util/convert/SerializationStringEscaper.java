package org.datacleaner.util.convert;

import org.apache.commons.lang.ArrayUtils;

public class SerializationStringEscaper {
    private static final String[][] ESCAPE_MAPPING = { { "&amp;", "&" }, { "&#91;", "[" }, { "&#93;", "]" },
            { "&#44;", "," }, { "&lt;", "<" }, { "&gt;", ">" }, { "&quot;", "\"" }, { "&copy;", "\u00a9" },
            { "&reg;", "\u00ae" }, { "&euro;", "\u20a0" } };
    private static final String[][] ESCAPE_MAPPING_REVERSED;
    
    // Ughhh.
    static {
        ESCAPE_MAPPING_REVERSED = (String[][]) ArrayUtils.clone(ESCAPE_MAPPING);
        ArrayUtils.reverse(ESCAPE_MAPPING_REVERSED);
    }
    
    public static final String escape(String str) {
        for (String[] mapping : ESCAPE_MAPPING) {
            String escapedValue = mapping[1];
            if (str.contains(escapedValue)) {
                str = str.replace(escapedValue, mapping[0]);
            }
        }
        return str;
    }

    public static final String unescape(String str) {
        for (String[] mapping : ESCAPE_MAPPING_REVERSED) {
            String unescapedValue = mapping[0];
            if (str.contains(unescapedValue)) {
                str = str.replaceAll(unescapedValue, mapping[1]);
            }
        }
        return str;
    }
}
