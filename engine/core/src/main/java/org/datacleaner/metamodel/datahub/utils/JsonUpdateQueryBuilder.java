package org.datacleaner.metamodel.datahub.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUpdateQueryBuilder {

    public static String buildJsonArray(List<String> queries) {

        final OutputStream os = new ByteArrayOutputStream();
        final ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            mapper.writeValue(os, queries);
            final byte[] data = ((ByteArrayOutputStream) os).toByteArray();
            json = new String(data, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;

    }

}
