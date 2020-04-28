package org.animeatsume.api.controller;

import org.animeatsume.api.model.NovelPlanetSourceResponse;
import org.animeatsume.api.model.NovelPlanetUrlRequest;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.http.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NovelPlanetController {
    private static final Logger log = LoggerFactory.getLogger(NovelPlanetController.class);
    private static final String websiteIdentifier = "/v/";
    private static final String apiIdentifier = "/api/source/";
    private static final String protocolOriginSeparator = "://";

    public void getNovelPlanetMp4Urls(NovelPlanetUrlRequest novelPlanetRequest, ServerHttpRequest request) {
        // TODO forward client request instead of making new one
        //  in order to preserve original IP address
        URL websiteUrlObj = novelPlanetRequest.getNovelPlanetUrl();
        String protocol = websiteUrlObj.getProtocol();
        String hostAndPort = websiteUrlObj.getAuthority();
        String path = websiteUrlObj.getPath();
        String websiteUrl = websiteUrlObj.toString();
        String origin = protocol + protocolOriginSeparator + hostAndPort;
        String videoId = path.split(websiteIdentifier)[1];
        String novelPlanetApiUrl = origin + apiIdentifier + videoId;

        log.info("URL obj = {}, protocol = {}, hostAndPort = {}, path = {}, websiteUrl = {}, origin = {}, videoId = {}, novelApiUrl = {}", websiteUrlObj, protocol, hostAndPort, path, websiteUrl, origin, videoId, novelPlanetApiUrl);

        String cookie = getCookieFromWebsite(websiteUrl);
        List<NovelPlanetSourceResponse.NovelPlanetSource> sourcesForVideo =
            getRedirectorSourcesForVideo(origin, websiteUrl, novelPlanetApiUrl, cookie);
        List<String> mp4Urls = getMp4UrlsFromRedirectorUrls(sourcesForVideo, origin, websiteUrl, cookie);
        mp4Urls.forEach(log::info);
    }

    private String getCookieFromWebsite(String url) {
        RestTemplate websiteRequest = new RestTemplate();
        ResponseEntity<String> websiteHtml = websiteRequest.getForEntity(url, String.class);
        return websiteHtml.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    }

    private List<NovelPlanetSourceResponse.NovelPlanetSource> getRedirectorSourcesForVideo(String origin, String websiteUrl, String apiUrl, String cookie) {
        // TODO headers to consider: "X-Forwarded-For", "X-Real-IP", "Host"
        HttpEntity<Void> request = CorsProxy.getCorsEntityWithCookie(null, origin, websiteUrl, cookie);

        ResponseEntity<NovelPlanetSourceResponse> response = new RestTemplate().exchange(
            apiUrl,
            HttpMethod.POST,
            request,
            NovelPlanetSourceResponse.class
        );

        return response.getBody().getData();
    }

    private List<String> getMp4UrlsFromRedirectorUrls(List<NovelPlanetSourceResponse.NovelPlanetSource> redirectorSources, String novelPlanetOrigin, String novelPlanetWebsiteUrl, String novelPlanetCookie) {
        return redirectorSources.stream()
            .map(novelPlanetSource -> {
                String redirectorUrl = novelPlanetSource.getFile();

                HttpEntity<Void> mp4Request = CorsProxy.getCorsEntityWithCookie(null, novelPlanetOrigin, novelPlanetWebsiteUrl, novelPlanetCookie);

                // will give 302 (Found) with redirect. Don't follow it, instead get the redirect URL
                // since that holds the URL to the MP4
                RestTemplate redirectorRequest = Requests.getNoFollowRedirectsRestTemplate();

                ResponseEntity<Void> redirectorResponse = redirectorRequest.exchange(
                    redirectorUrl,
                    HttpMethod.GET,
                    mp4Request,
                    Void.class
                );

                return redirectorResponse.getHeaders().getFirst("Location");
            })
            .collect(Collectors.toList());
    }
}
