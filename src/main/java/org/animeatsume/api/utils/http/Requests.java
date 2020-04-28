package org.animeatsume.api.utils.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class Requests {
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
}
