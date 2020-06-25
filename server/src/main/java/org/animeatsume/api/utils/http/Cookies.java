package org.animeatsume.api.utils.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Cookies {
    public static String getCookieFromWebsite(String url) {
        RestTemplate websiteRequest = new RestTemplate();
        ResponseEntity<String> websiteHtml = websiteRequest.getForEntity(url, String.class);
        return websiteHtml.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    }
}
