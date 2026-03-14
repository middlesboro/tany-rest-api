# Development Rules for AI Agents

## JSON Processing (Jackson)
* **Rule**: When using `ObjectMapper` or `TypeReference`, do **NOT** use the legacy `com.fasterxml.jackson.*` packages.
* **Rule**: Use the new `tools.jackson.*` packages instead. This is required for Spring Boot 4.
  * Correct imports:
    ```java
    import tools.jackson.core.type.TypeReference;
    import tools.jackson.databind.ObjectMapper;
    ```
  * Incorrect imports:
    ```java
    import com.fasterxml.jackson.core.type.TypeReference;
    import com.fasterxml.jackson.databind.ObjectMapper;
    ```
