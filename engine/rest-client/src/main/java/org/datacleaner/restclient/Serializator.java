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
package org.datacleaner.restclient;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class for input/output data types conversion from/into String.
 * @since 11. 09. 2015
 */
public class Serializator {
    private static final Logger logger = LoggerFactory.getLogger(Serializator.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static ComponentList componentList(String response) {
        return (ComponentList) Serializator.fromString(response, ComponentList.class);
    }

    public static ComponentList.ComponentInfo componentInfo(String response) {
        return (ComponentList.ComponentInfo) Serializator.fromString(response, ComponentList.ComponentInfo.class);
    }

    public static String stringProcessStatelessInput(ProcessStatelessInput processStatelessInput) {
        return Serializator.intoString(processStatelessInput);
    }

    public static OutputColumns outputColumnsOutput(String response) {
        return Serializator.fromString(response, OutputColumns.class);
    }

    public static ProcessStatelessOutput processStatelessOutput(String response) {
        return (ProcessStatelessOutput) Serializator.fromString(response, ProcessStatelessOutput.class);
    }

    public static String stringCreateInput(CreateInput createInput) {
        return Serializator.intoString(createInput);
    }

    public static String stringProcessInput(ProcessInput processInput) {
        return Serializator.intoString(processInput);
    }

    public static ProcessOutput processOutput(String response) {
        return (ProcessOutput) Serializator.fromString(response, ProcessOutput.class);
    }

    public static ProcessResult processResult(String response) {
        return (ProcessResult) Serializator.fromString(response, ProcessResult.class);
    }

    private static String intoString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromString(String value, Class<T> type) {
        try {
            if (value instanceof String && (value == null || value.equals(""))) {
                return null;
            }

            return objectMapper.readValue(value, type);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
