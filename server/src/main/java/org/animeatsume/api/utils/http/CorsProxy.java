package org.animeatsume.api.utils.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

public class CorsProxy {
    public static <T> HttpEntity<T> getCorsEntity(T body, String origin, String referer) {
        return CorsProxy.getCorsEntity(body, origin, referer, null);
    }

    public static <T> HttpEntity<T> getCorsEntity(T body, String origin, String referer, String cookie) {
        return getCorsEntity(body, origin, referer, cookie, (HttpHeaders) null);
    }

    public static <T> HttpEntity<T> getCorsEntity(
        T body,
        String origin,
        String referer,
        String cookie,
        Map<String, Object> headers
    ) {
        HttpHeaders httpHeaders = new HttpHeaders();

        if (headers != null) {
            headers.forEach((key, val) -> {
                if (val instanceof List) {
                    httpHeaders.put(key, (List<String>) val);
                } else if (val instanceof String) {
                    httpHeaders.set(key, (String) val);
                }
            });
        }

        return getCorsEntity(body, origin, referer, cookie, httpHeaders);
    }

    public static <T> HttpEntity<T> getCorsEntity(
        T body,
        String origin,
        String referer,
        String cookie,
        HttpHeaders headers
    ) {
        HttpHeaders corsHeaders = new HttpHeaders();

        if (headers != null) {
            corsHeaders = Requests.copyHttpHeaders(headers);
        }

        corsHeaders.set("Origin", origin);
        corsHeaders.set("Referer", referer);

        if (cookie != null) {
            corsHeaders.set("Cookie", cookie);
        }

        // Prevent getting gzip responses by removing the
        // header that defines what is supported
        corsHeaders.remove(HttpHeaders.ACCEPT_ENCODING);

        return new HttpEntity<>(body, corsHeaders);
    }
}
