package org.animeatsume.api.utils.http;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.utils.ObjectUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Log4j2
public class Requests {
    public static HttpHeaders copyHttpHeaders(HttpHeaders headers) {
        HttpHeaders copiedHeaders = new HttpHeaders();

        if (headers != null) {
            headers.forEach(copiedHeaders::addAll);
        }

        return copiedHeaders;
    }

    public static <T> HttpEntity<T> getHttpEntityWithHeaders(T body, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();

        headers.forEach(httpHeaders::add);

        return new HttpEntity<T>(body, httpHeaders);
    }

    public static <T> HttpEntity<T> getHttpEntityWithHeaders(T body, String[][] headers) {
        HttpHeaders httpHeaders = new HttpHeaders();

        Stream.of(headers).forEach(entry -> {
            httpHeaders.add(entry[0], entry[1]);
        });

        return new HttpEntity<T>(body, httpHeaders);
    }

    public static RestTemplate getNoFollowRedirectsRestTemplate() {
        ClientHttpRequestFactory noFollowFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };

        return new RestTemplate(noFollowFactory);
    }

    public static void addAcceptableMediaTypes(RestTemplate restTemplate, MediaType... mediaTypes) {
        MappingJackson2HttpMessageConverter httpMediaTypeConverter = new MappingJackson2HttpMessageConverter();
        httpMediaTypeConverter.setSupportedMediaTypes(Arrays.asList(mediaTypes));
        restTemplate.getMessageConverters().add(httpMediaTypeConverter);
    }

    public static HttpEntity<MultiValueMap<String, String>> getFormDataHttpEntity(@Nullable HttpHeaders requestHeaders, String[][] bodyEntries) {
        HttpHeaders headers = requestHeaders != null ? requestHeaders : new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formDataBody = new LinkedMultiValueMap<>();

        if (bodyEntries != null && bodyEntries.length > 0) {
            Stream.of(bodyEntries).forEach(keyValPair -> {
                formDataBody.add(keyValPair[0], keyValPair[1]);
            });
        }

        return new HttpEntity<>(formDataBody, headers);
    }

    public static HttpHeaders headForHeadersWithAcceptAllFallback(
        URI url,
        RestTemplate restTemplate,
        HttpEntity<?> requestEntity
    ) {
        HttpHeaders headersForHeadRequest = new HttpHeaders();
        Object requestBody = null;
        RestTemplate headRequest = new RestTemplate();

        if (requestEntity != null) {
            headersForHeadRequest = copyHttpHeaders(requestEntity.getHeaders());
            requestBody = requestEntity.getBody();
            headRequest = restTemplate;
        }

        HttpHeaders headResponseHeaders;

        try {
            headResponseHeaders = headRequest.headForHeaders(url);
        } catch (HttpClientErrorException e) {
            log.error("Error executing HEAD request: status code ({}), response body ({}), error = {}",
                e.getStatusCode(),
                e.getResponseBodyAsString(),
                e.getMessage()
            );
            log.info("Attempting HEAD request with 'Accept: */*'...");

            headersForHeadRequest.add(HttpHeaders.ACCEPT, "*/*");

            /*
             * Use RestTemplate.exchange() instead of .headForHeaders() in
             * order to add the "Accept: (all)" header to the request and let
             * it, as well as any other headers in requestEntity, to be passed
             * along in the HEAD request.
             *
             * Since HttpHeaders cannot be parsed by RestTemplate's responseExtractor,
             * cast it to a MultiValueMap, the superclass of HttpMethod.
             */
            ResponseEntity<MultiValueMap> headResponse = headRequest.exchange(
                url,
                HttpMethod.HEAD,
                new HttpEntity<>(requestBody, headersForHeadRequest),
                MultiValueMap.class
            );

            headResponseHeaders = headResponse.getHeaders();
        }

        return headResponseHeaders;
    }

    public static Class<?> getClassFromContentTypeHeader(String contentTypeHeader) {
        List<String> objectType = Arrays.asList("json", "xml");
        List<String> textType = Arrays.asList("text");

        Predicate<List<String>> headerMatchesTypeListEntry = (List<String> typeList) ->
            typeList.stream().anyMatch(type -> (
                contentTypeHeader != null
                && !contentTypeHeader.isEmpty()
                && contentTypeHeader.toLowerCase().contains(type)
            ));

        if (headerMatchesTypeListEntry.test(objectType)) {
            return Object.class;
        }

        if (headerMatchesTypeListEntry.test(textType)) {
            return String.class;
        }

        return Resource.class;
    }

    public static <T> ResponseEntity<T> doRequestWithFallback(
        RestTemplate restTemplate,
        URI url,
        HttpMethod method,
        HttpEntity<?> requestEntity,
        Class<?> responseType
    ) {
        ResponseEntity<?> response;
        Object body;

        try {
            try {
                response = restTemplate.exchange(
                    url,
                    method,
                    requestEntity,
                    responseType
                );
                body = response.getBody();
            } catch (Exception e) {
                log.info("Failed to parse response to type ({}), proceeding with getting type from header. Error cause = {}", responseType, e.getMessage());

                if (responseType == String.class && e instanceof HttpStatusCodeException) {
                    throw e;
                }

                HttpHeaders responseHeaders = headForHeadersWithAcceptAllFallback(url, restTemplate, requestEntity);
                List<MediaType> headersAccept = responseHeaders.getAccept();
                String contentTypeHeader = headersAccept.isEmpty()
                    ? MediaType.TEXT_PLAIN_VALUE
                    : headersAccept
                        .get(headersAccept.size() - 1)
                        .toString();
                Class<?> actualResponseTypeClass = getClassFromContentTypeHeader(contentTypeHeader);

                response = restTemplate.exchange(
                    url,
                    method,
                    requestEntity,
                    actualResponseTypeClass
                );
                body = response.getBody();

                if (actualResponseTypeClass == String.class && responseType != String.class) {
                    // If expecting an object but received a string, it's probably because
                    // there are invalid characters.
                    // Thus, attempt to parse the object after removing invalid characters.
                    Object parsedObject = ObjectUtils.sanitizeAndParseJsonToClass((String) body, responseType);

                    if (parsedObject != null) {
                        log.info("String from response body successfully parsed to ({})", responseType);
                        body = parsedObject;
                    }
                }
            }
        } catch (HttpStatusCodeException e) {
            log.error("Error executing request: status code ({}), response body ({}), error = {}",
                e.getStatusCode(),
                e.getResponseBodyAsString(),
                e.getMessage()
            );

            return new ResponseEntity<>((T) e.getResponseBodyAsString(), e.getResponseHeaders(), e.getStatusCode());
        }

        return new ResponseEntity<>((T) body, response.getHeaders(), response.getStatusCode());
    }

    /**
     * Proxies/forwards a {@link Resource} from a given URL back to the client.
     *
     * Serves as a "dumb" proxy system in that it doesn't track the client's
     * progress in receiving the external resource.
     * This means that e.g. if a video were loaded initially but then the client
     * seeked to a future part of the video that hadn't loaded yet, the previous
     * loaded content would be thrown out and the video would be re-requested from
     * the URL; this server would then have to download all the video up until the
     * point the user seeked to (as told by the {@code Range} header), resulting in
     * duplicated/uncached download effort.
     *
     * @param url The URL of the desired {@link Resource} to proxy back to the client.
     * @return A {@link ResponseEntity} containing the given {@link Resource} in the body
     *         and {@code Content-Type} header set.
     */
    public static ResponseEntity<Resource> getUrlResourceStreamResponse(String url) {
        try {
            UrlResource urlResource = new UrlResource(url);

            return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .contentType(
                    MediaTypeFactory
                        .getMediaType(urlResource)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM)
                )
                .body(urlResource);
        } catch (MalformedURLException e) {
            log.error("Could not get UrlResource of URL ({}). Error:", url);
            e.printStackTrace();

            return ResponseEntity.noContent().build();
        }
    }

    private static final long BYTE_LENGTH = 1024;
    private static final long CHUNK_SIZE_VERY_LOW = BYTE_LENGTH * 256;
    private static final long CHUNK_SIZE_LOW = BYTE_LENGTH * 512;
    private static final long CHUNK_SIZE_MED = BYTE_LENGTH * 1024;
    private static final long CHUNK_SIZE_HIGH = BYTE_LENGTH * 2048;
    private static final long CHUNK_SIZE_VERY_HIGH = CHUNK_SIZE_HIGH * 2;

    public static List<Long> getContentRangeStartAndEndAndLength(String url, HttpHeaders headers, boolean endRangeIsContentLengthIfStartIsZero) {
        long chunkSize = CHUNK_SIZE_VERY_HIGH; // TODO choose which chunk size to use from above

        try {
            UrlResource urlResource = new UrlResource(url);
            long contentLength = urlResource.contentLength();
            HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().get(0);

            long headerRangeStart = 0;

            if (range != null) {
                headerRangeStart = range.getRangeStart(contentLength);
            }

            if (headerRangeStart == 0L) {
                long rangeEnd = endRangeIsContentLengthIfStartIsZero
                    ? contentLength-1
                    : chunkSize;

                return Arrays.asList(0L, rangeEnd, contentLength);
            }

            long resourceLengthLeftToServe = contentLength - headerRangeStart + 1;
            long rangeLength = Math.min(chunkSize, resourceLengthLeftToServe);
            long rangeEnd = headerRangeStart + rangeLength;

            return Arrays.asList(headerRangeStart, rangeEnd, contentLength);
        } catch (IOException e) {
            log.error("Could not get UrlResource or ResourceRegion for URL ({})", url);
            e.printStackTrace();
        }

        return Arrays.asList(0L, chunkSize, 0L);
    }

    public static ResourceRegion getUrlResourceRegion(String url, HttpHeaders headers) {
        long chunkSize = CHUNK_SIZE_MED; // TODO choose which chunk size to use from above

        try {
            UrlResource urlResource = new UrlResource(url);
            long contentLength = urlResource.contentLength();
            HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().get(0);

            if (range != null) {
                long rangeStart = range.getRangeStart(contentLength);
                long rangeEnd = range.getRangeEnd(contentLength);
                long resourceLength = rangeEnd - rangeStart + 1;
                long rangeLength = Math.min(chunkSize, resourceLength);

                return new ResourceRegion(urlResource, rangeStart, rangeLength);
            } else {
                long rangeLength = Math.min(chunkSize, contentLength);

                return new ResourceRegion(urlResource, 0, rangeLength);
            }
        } catch (IOException e) {
            log.error("Could not get UrlResource or ResourceRegion for URL ({})", url);
            e.printStackTrace();
        }

        return null;
    }
}
