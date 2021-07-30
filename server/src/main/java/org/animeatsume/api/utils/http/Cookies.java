package org.animeatsume.api.utils.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
public class Cookies {
    public static String getCookieFromWebsite(String url) {
        RestTemplate websiteRequest = new RestTemplate();
        ResponseEntity<String> websiteHtml = websiteRequest.getForEntity(url, String.class);
        return websiteHtml.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    }

    public static List<String> getCookiesFromWebsite(String url) {
        RestTemplate websiteRequest = new RestTemplate();
        ResponseEntity<String> websiteHtml = websiteRequest.getForEntity(url, String.class);

        return websiteHtml.getHeaders().get(HttpHeaders.SET_COOKIE);
    }
}
