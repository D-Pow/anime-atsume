package org.animeatsume.api.utils.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class Requests {
    private static final Logger log = LoggerFactory.getLogger(Requests.class);

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
                        .orElse(MediaType.valueOf(
                            MediaType.APPLICATION_OCTET_STREAM_VALUE
                        ))
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
