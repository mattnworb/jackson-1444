Attempts to reproduce https://github.com/spotify/helios/issues/1143 and tests
some potential fixes related to
https://github.com/FasterXML/jackson-databind/issues/1444.

See https://github.com/FasterXML/jackson-databind/issues/1736 for an
explanation of the "fix".

The same test is repeated for three cases:

- jackson-databind:2.8.9: should pass
- jackson-databind:2.9.0: should fail
- jackson-databind:2.9.0 with a different configuration: should pass

To run all tests in the repo, continuing after failures in some modules, run
`mvn test -Dmaven.test.failure.ignore=true` (but ignore the "Reactor Summary").

The test that we run is:

```java
public class NonEmptyTest {

  private static final TypeReference<Map<String, Object>> MAP_TYPE =
      new TypeReference<Map<String, Object>>() {
      };

  private final ObjectMapper objectMapper = new ObjectMapper()
      .configure(SORT_PROPERTIES_ALPHABETICALLY, true)
      .configure(ORDER_MAP_ENTRIES_BY_KEYS, true);

  private final ObjectWriter objectWriter = objectMapper
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
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
```
