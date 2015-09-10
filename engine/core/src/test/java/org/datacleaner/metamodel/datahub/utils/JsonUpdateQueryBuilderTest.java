package org.datacleaner.metamodel.datahub.utils;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonUpdateQueryBuilderTest {

    private static final String QUERY1 = "UPDATE tablename SET column1 = value1, column2 = value2 WHERE column3 = value3";
    private static final String QUERY2 = "INSERT INTO tablename VALUES (value1, value2, value3)";

    private static final ObjectMapper mapper = new ObjectMapper();
    
    @Test
    public void shouldConvertQueriesToJson() throws IOException {
        List<String> queries = Arrays.asList(new String[] { QUERY1, QUERY2 });
        String jsonString = JsonUpdateQueryBuilder.buildJsonArray(queries);
        List<String> jsonArrayValues = jsonArrayToList(jsonString);
        assertThat(jsonArrayValues, containsInAnyOrder(QUERY1, QUERY2));
    }
    
    @Test
    public void shouldConvertEmptyList() throws IOException {
        List<String> emptyList = Collections.emptyList();
        String jsonString = JsonUpdateQueryBuilder.buildJsonArray(emptyList);
        List<String> jsonArrayValues = jsonArrayToList(jsonString);
        assertThat(jsonArrayValues.isEmpty(), is(true));
    }
    
    @Test
    public void shouldHandleNullValue() {
        String jsonString = JsonUpdateQueryBuilder.buildJsonArray(null);
        assertThat(jsonString, is("null"));
    }

    private List<String> jsonArrayToList(String jsonString)
            throws IOException, JsonParseException, JsonMappingException {
        CollectionType stringListCollectionType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        List<String> jsonArrayValues = mapper.readValue(jsonString, stringListCollectionType);
        return jsonArrayValues;
    }

}
