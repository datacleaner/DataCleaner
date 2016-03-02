package org.datacleaner.documentation.swagger;

import java.util.HashMap;

/**
 * Added by jakub on 2.3.16
 */
public class SwaggerSchema extends HashMap<String, Object> {
    public SwaggerSchema(String type) {
        this.put("type", type);
    }
}
