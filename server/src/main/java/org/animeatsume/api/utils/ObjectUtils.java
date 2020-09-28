package org.animeatsume.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ObjectUtils {
    private static final Logger log = LoggerFactory.getLogger(ObjectUtils.class);

    public static <T> T findObjectInList(List<T> list, Predicate<T> filterPredicate) {
        return list.stream().filter(filterPredicate).findFirst().orElse(null);
    }

    public static <T> List<T> getAllCompletableFutureResults(List<CompletableFuture<T>> futures) {
        return getAllCompletableFutureResults(futures, result -> {});
    }

    public static <T> List<T> getAllCompletableFutureResults(
        List<CompletableFuture<T>> futures,
        Consumer<T> sideEffect
    ) {
        return getAllCompletableFutureResults(futures, (result, index) -> sideEffect.accept(result));
    }

    public static <T> List<T> getAllCompletableFutureResults(
        List<CompletableFuture<T>> futures,
        BiConsumer<T, Integer> sideEffect
    ) {
        List<T> results = new ArrayList<>();

        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<T> future = futures.get(i);
            T result = null;

            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Could not get future. Cause = {}, Message = {}", e.getCause(), e.getMessage());
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
}
