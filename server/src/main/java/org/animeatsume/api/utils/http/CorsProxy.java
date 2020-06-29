package org.animeatsume.api.utils.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class CorsProxy {
    public static ResponseEntity<?> doCorsRequest(HttpMethod method, URI url, Object body, HttpHeaders headers) {
        String origin = UriParser.getOrigin(url);
        HttpEntity<Object> corsEntity = getCorsEntity(body, origin, origin, null, headers);
        List<String> acceptHeaders = corsEntity.getHeaders().get(HttpHeaders.ACCEPT);

        if (acceptHeaders == null || acceptHeaders.size() == 0) {
            return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body("You must add a value for the 'Accept' header");
        }

        boolean responseIsText = acceptHeaders.stream().reduce(
            false,
            (isText, headerEntry) -> headerEntry.contains("text"),
            (prevIsText, nextIsText) -> prevIsText || nextIsText
        );

        Class<?> responseClass = responseIsText ? String.class : Object.class;
        RestTemplate restTemplate = Requests.getNoFollowRedirectsRestTemplate();

        ResponseEntity<?> response = Requests.doRequestWithStringFallback(restTemplate, url, method, corsEntity, responseClass);
        Object responseBody = response.getBody();
        HttpHeaders responseHeaders = Requests.copyHttpHeaders(response.getHeaders());

        // 'Content-Encoding' and 'Transfer-Encoding' mark that the response body is zipped/compressed.
        // Remove it from the response to the client since it is only relevant for the recipient of the original
        // content (which is this server).
        responseHeaders.remove(HttpHeaders.CONTENT_ENCODING);
        responseHeaders.remove(HttpHeaders.TRANSFER_ENCODING);

        return new ResponseEntity<>(responseBody, responseHeaders, HttpStatus.OK);
    }

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
