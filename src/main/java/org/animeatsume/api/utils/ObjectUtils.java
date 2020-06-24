package org.animeatsume.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

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
        return getAllCompletableFutureResults(futures, (result, index) -> {});
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

        try (InputStream imageInputStream = resource.getInputStream()) {
            byte[] imageBytes = new byte[(int) resource.contentLength()];
            imageInputStream.read(imageBytes);

            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            byte[] imageHash = hash.digest(imageBytes);
            return new BigInteger(1, imageHash).toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Could not hash Resource ({}). Error cause ({}), message = {}", resource, e.getCause(), e.getMessage());
        }

        return null;
    }
}
