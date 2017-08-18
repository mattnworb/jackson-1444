package com.spotify.jackson1444;

import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Map;
import org.junit.Test;

public class NonEmptyTest {

  private static final TypeReference<Map<String, Object>> MAP_TYPE =
      new TypeReference<Map<String, Object>>() {
      };

  private final ObjectMapper objectMapper = new ObjectMapper()
      .configure(SORT_PROPERTIES_ALPHABETICALLY, true)
      .configure(ORDER_MAP_ENTRIES_BY_KEYS, true);

  private final ObjectWriter objectWriter = objectMapper
      // !!!! difference from version-2.9.x module:
      .setDefaultPropertyInclusion(
          //parameters are valueIncl=NON_EMPTY, contentIncl=ALWAYS
          JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.ALWAYS)
      )
      .writer();

  @Test
  public void testStringToMapToString() throws Exception {
    // A JSON object with two fields, where "foo" has a series of nested fields with the leaf entry
    // having a field with a value that is an empty object
    final String json = "{\"a\":\"b\",\"foo\":{\"bar\":{\"baz\":{\"bat\":{}}}}}";

    final Map<String, Object> map = objectMapper.readValue(json, MAP_TYPE);
    assertTrue(map.containsKey("foo"));

    // helios expects that writing this map back to a string (or bytes) will give the original input
    // this passes in jackson-databind:2.8.x but in 2.9.x, the "foo" field is omitted.
    assertEquals(json, objectWriter.writeValueAsString(map));
  }
}
