/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.documentation.swagger;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @since 23. 09. 2015
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SwaggerParameter {
    public static enum In {
        HEADER("header"),
        PATH("path"),
        QUERY("query"),
        BODY("body"),
        ;

        private String value = "";

        private In(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    };

    public static enum Type {
        STRING("string"),
        INTEGER("integer"),
        BOOLEAN("boolean"),
        OBJECT("object"),
        ;

        private String value = "";

        private Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    };

    private String in = "";
    private String name = "";
    private String description = "";
    private Boolean required = false;
    private String type = "";
    private Map<String, String> items = new HashMap<>();
    private Map<String, String> schema = new HashMap<>();

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getItems() {
        return items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }

    public Map<String, String> getSchema() {
        return schema;
    }

    public void setSchema(Map<String, String> schema) {
        this.schema = schema;
    }

    public void setTypeByClass(Class clazz) {
        if (clazz.getName().equals(String.class.getName())) {
            setType(Type.STRING.getValue());
        }
        else if (clazz.getName().equals(Integer.class.getName())) {
            setType(Type.INTEGER.getValue());
        }
        else if (clazz.getName().equals(Boolean.class.getName())) {
            setType(Type.BOOLEAN.getValue());
        }
        else {
            setType(Type.OBJECT.getValue());
            schema.put("$ref", "#/definitions/JSON object");
        }
    }
}
