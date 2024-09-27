package org.animeatsume.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Log4j2
public class ObjectUtils {
    public static <T> T findObjectInList(List<T> list, Predicate<T> filterPredicate) {
        return list.stream().filter(filterPredicate).findFirst().orElse(null);
    }

    /**
     * Waits for all {@link CompletableFuture}s to complete and extracts
     * the result from each.
     *
     * @param futures List of futures to complete.
     * @return All future results in the same order as the {@code futures} parameter.
     */
    public static <T> List<T> getAllCompletableFutureResults(List<CompletableFuture<T>> futures) {
        return getAllCompletableFutureResults(futures, result -> {});
    }

    /**
     * Waits for all {@link CompletableFuture}s to complete and extracts
     * the result from each.
     *
     * @param futures List of futures to complete.
     * @param sideEffect Function to run on each {@link CompletableFuture#get()} call.
     *                   Accepts the future result as the argument.
     *                   Runs sequentially from {@code futures} start (index 0) to end,
     *                   regardless of individual future completion order.
     * @return All future results in the same order as the {@code futures} parameter.
     */
    public static <T> List<T> getAllCompletableFutureResults(
        List<CompletableFuture<T>> futures,
        Consumer<T> sideEffect
    ) {
        return getAllCompletableFutureResults(futures, (result, index) -> sideEffect.accept(result));
    }

    /**
     * Waits for all {@link CompletableFuture}s to complete and extracts
     * the result from each.
     *
     * @param futures List of futures to complete.
     * @param sideEffect Function to run on each {@link CompletableFuture#get()} call.
     *                   Accepts the future result and its index in the {@code futures} list as arguments.
     *                   Runs sequentially from {@code futures} start (index 0) to end,
     *                   regardless of individual future completion order.
     * @return All future results in the same order as the {@code futures} parameter.
     */
    public static <T> List<T> getAllCompletableFutureResults(
        List<CompletableFuture<T>> futures,
        BiConsumer<T, Integer> sideEffect
    ) {
        List<T> results = new ArrayList<>(futures.size());

        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<T> future = futures.get(i);
            T result = null;

            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Could not get future. Error = {}", e.getMessage());
            }

            sideEffect.accept(result, i);
            results.add(result);
        }

        return results;
    }

    public static String hashResource(Resource resource) {
        if (resource == null) {
            return null;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] imageBytes = new byte[(int) resource.contentLength()];
            inputStream.read(imageBytes);

            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            byte[] resourceHash = hash.digest(imageBytes);

            return new BigInteger(1, resourceHash).toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Could not hash Resource ({}). Error cause ({}), message = {}", resource, e.getCause(), e.getMessage());
        }

        return null;
    }

    /**
     * Parses a JSON string to the specified object.
     * Removes all non-ASCII, control, and non-printable characters, to ensure the JSON string is parsable.
     *
     * @param <T> Class type.
     * @param json JSON string to parse.
     * @param parseToClass Class that the JSON represents.
     * @return Instance of the specified class.
     *
     * @see <a href="https://www.baeldung.com/java-json">Overview of JSON (de)serialization libraries</a>
     */
    public static <T> T sanitizeAndParseJsonToClass(String json, Class<T> parseToClass) {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

        String nonAsciiCharactersRegex = "[^\\x00-\\x7F]";
        String asciiControlCharactersRegex = "[\\p{Cntrl}&&[^\r\n\t]]";
        String nonPrintableCharactersRegex = "\\p{C}";

        String sanitizeRegex = String.format("(%s)|(%s)|(%s)", nonAsciiCharactersRegex, asciiControlCharactersRegex, nonPrintableCharactersRegex);
        String sanitizedJson = json.replaceAll(sanitizeRegex, "");

        try {
            return objectMapper.readValue(sanitizedJson, parseToClass);
        } catch (JsonProcessingException e) {
            log.error("Could not parse json to class ({}). Error = {}", parseToClass, e.getMessage());
        }

        return null;
    }

    public static <T> T classFromJson(String json, Class<T> parseToClass) {
        return sanitizeAndParseJsonToClass(json, parseToClass);
    }

    public static String classToJson(Object obj) {
        try {
            return Jackson2ObjectMapperBuilder.json().build().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize Object ({}) to JSON string. Error = {}", obj, e.getMessage());
        }

        return null;
    }

    public static Map<String, ?> getObjectProperties(Object obj) {
        return (Map<String, ?>) getObjectProperties(obj, false, false);
    }
    public static Object getObjectProperties(Object obj, boolean asString) {
        return getObjectProperties(obj, asString, false);
    }
    /**
     * @see <a href="https://stackoverflow.com/questions/13400075/reflection-generic-get-field-value">Getting fields via reflection</a>
     * @see <a href="https://www.geeksforgeeks.org/reflection-in-java">Reflection overview</a>
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#getDeclaredField-java.lang.String-">Reflection - calling {@code getDeclaredField()} on objects</a>
     * @see <a href="https://stackoverflow.com/questions/60542367/how-exactly-does-the-java-reduce-function-with-3-parameters-work/60554907#60554907">{@code .reduce()} third argument for return-type conversion</a>
     *
     * Related:
     * @see <a href="https://stackoverflow.com/questions/15966726/how-to-change-method-behaviour-through-reflection/15967008#15967008">Object proxies for monitoring and modifying method calls</a>
     */
    private static Object getObjectProperties(Object obj, boolean asString, boolean recurseNestedObjects) {
        Class<?> objClass = obj.getClass();
        Field[] objFields = objClass.getDeclaredFields();

        StringBuilder sb = new StringBuilder(objClass.getName() + " {");

        Map<String, ?> objEntries = Arrays.asList(objFields).parallelStream()
            .reduce(new HashMap<>(), (map, field) -> {
                String fieldName = field.getName();

                sb.append(fieldName).append("=");

                Object fieldValue = "Unparsed";

                try {
                    field.setAccessible(true); // Make private fields readable
                    fieldValue = field.get(obj);

                    // Recurse if field is a dependency/non-primitive class
                    // i.e. when `Class::toString()` returns something akin to "com.company.SomeClass@a1b2c3d4"
                    if (recurseNestedObjects && fieldValue.toString().matches("^(org|com)[^@]*\\.\\w+@[a-z0-9]{8}\\$")) {
                        fieldValue = getObjectProperties(fieldValue, asString, recurseNestedObjects);
                    }
                } catch (IllegalAccessException ignored) {}

                sb.append("(")
                    .append(fieldValue)
                    .append(")");

                sb.append(", ");

                map.put(fieldName, fieldValue);

                return map;
            }, (mapIterationA, mapIterationB) -> {
                mapIterationA.putAll(mapIterationB);

                return mapIterationA;
            });

        sb.delete(sb.length() - 2, sb.length()); // Remove last comma+space
        sb.append("}");

        if (asString) {
            return sb.toString();
        }

        return objEntries;
    }

    public static String toString(Object obj) {
        return (String) getObjectProperties(obj, true);
    }
}
