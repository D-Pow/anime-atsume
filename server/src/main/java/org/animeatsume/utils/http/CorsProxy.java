package org.animeatsume.utils.http;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Log4j2
public class CorsProxy {
    private static final boolean defaultFollowRedirects = true;

    public static <T> ResponseEntity<T> doCorsRequest(
        HttpMethod method,
        String url,
        @Nullable String origin,
        @Nullable Object body
    ) {
        return doCorsRequest(
            method,
            url,
            origin,
            body,
            null,
            defaultFollowRedirects
        );
    }

    public static <T> ResponseEntity<T> doCorsRequest(
        HttpMethod method,
        String url,
        @Nullable String origin,
        @Nullable Object body,
        @Nullable HttpHeaders headers
    ) {
        return doCorsRequest(
            method,
            url,
            origin,
            body,
            headers,
            defaultFollowRedirects
        );
    }

    public static <T> ResponseEntity<T> doCorsRequest(
        HttpMethod method,
        String url,
        @Nullable String origin,
        @Nullable Object body,
        @Nullable HttpHeaders headers,
        boolean noFollowRedirects
    ) {
        URI urlParsed;

        /**
         * @see <a href="https://stackoverflow.com/questions/5019210/java-url-encoding-leaving-allowed-character-intact">SO post about URL-encoding</a>
         */
        try {
            urlParsed = URI.create(url);
        } catch (Exception invalidUrlFormatException) {
            try {
                urlParsed = new URI(url);
            } catch (Exception e) {
                urlParsed = URI.create(URLEncoder.encode(url, StandardCharsets.ISO_8859_1));
            }
        }

        return doCorsRequest(
            method,
            urlParsed,
            origin == null ? null : URI.create(origin),
            body,
            headers,
            noFollowRedirects
        );
    }

    public static <T> ResponseEntity<T> doCorsRequest(
        HttpMethod method,
        URI url,
        @Nullable URI origin,
        @Nullable Object body,
        @Nullable HttpHeaders headers
    ) {
        return doCorsRequest(method, url, origin, body, headers, defaultFollowRedirects);
    }

    public static <T> ResponseEntity<T> doCorsRequest(
        HttpMethod method,
        URI url,
        @Nullable URI origin,
        @Nullable Object body,
        @Nullable HttpHeaders headers,
        boolean noFollowRedirects
    ) {
        String corsOrigin = origin != null ? origin.toString() : UriParser.getOrigin(url);
        HttpEntity<Object> corsEntity = getCorsEntity(body, corsOrigin, corsOrigin, null, headers, true);
        List<MediaType> requestAcceptHeaders = corsEntity.getHeaders().getAccept();

        if (requestAcceptHeaders.size() == 0) {
            return (ResponseEntity<T>) ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body("You must add a value for the 'Accept' header");
        }

        Class<?> responseClass = Requests.getClassFromContentTypeHeader(requestAcceptHeaders.toString());
        RestTemplate restTemplate = noFollowRedirects ? Requests.getNoFollowRedirectsRestTemplate() : new RestTemplate();

        // Add support for form-data requests and Map<String,String> responses
        Requests.addAcceptableMediaTypes(restTemplate, MediaType.APPLICATION_FORM_URLENCODED);
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        ResponseEntity<T> response = Requests.<T>doRequestWithFallback(restTemplate, url, method, corsEntity, responseClass);
        Object responseBody = response.getBody();
        HttpHeaders responseHeaders = Requests.copyHttpHeaders(response.getHeaders());

        // 'Content-Encoding' and 'Transfer-Encoding' mark that the response body is zipped/compressed.
        // Remove it from the response to the client since it is only relevant for the recipient of the original
        // content (which is this server).
        responseHeaders.remove(HttpHeaders.CONTENT_ENCODING);
        responseHeaders.remove(HttpHeaders.TRANSFER_ENCODING);

        return new ResponseEntity<T>((T) responseBody, responseHeaders, HttpStatus.OK);
    }

    public static <T> HttpEntity<T> getCorsEntity(T body, String origin, String referer) {
        return getCorsEntity(body, origin, referer, null);
    }

    public static <T> HttpEntity<T> getCorsEntity(T body, String origin, String referer, String cookie) {
        return getCorsEntity(body, origin, referer, cookie, null, false);
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

        return getCorsEntity(body, origin, referer, cookie, httpHeaders, false);
    }

    public static <T> HttpEntity<T> getCorsEntity(
        @Nullable T body,
        String origin,
        String referer,
        String cookie,
        HttpHeaders headers,
        boolean preventGzipResponses
    ) {
        HttpHeaders corsHeaders = new HttpHeaders();

        if (headers != null) {
            corsHeaders = Requests.copyHttpHeaders(headers);
        }

        corsHeaders.set(HttpHeaders.ORIGIN, origin);
        corsHeaders.set(HttpHeaders.REFERER, referer);

        if (cookie != null) {
            corsHeaders.set(HttpHeaders.COOKIE, cookie);
        }

        if (preventGzipResponses) {
            // Prevent getting gzip responses by removing the 'Accept-Encoding'
            // header which defines what algorithms are supported
            corsHeaders.remove(HttpHeaders.ACCEPT_ENCODING);
        }

        return new HttpEntity<>(body, corsHeaders);
    }
}
