package org.animeatsume.api.controller;

import org.animeatsume.api.model.NovelPlanetSourceResponse;
import org.animeatsume.api.model.NovelPlanetUrlRequest;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.http.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.animeatsume.api.utils.http.Cookies.getCookieFromWebsite;

@Component
public class NovelPlanetController {
    private static final Logger log = LoggerFactory.getLogger(NovelPlanetController.class);
    private static final String websiteIdentifier = "/v/";
    private static final String apiIdentifier = "/api/source/";
    private static final String protocolOriginSeparator = "://";

    public NovelPlanetSourceResponse getNovelPlanetSources(NovelPlanetUrlRequest novelPlanetRequest, ServerHttpRequest request) {
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
        NovelPlanetSourceResponse sourcesForVideo =
            getRedirectorSourcesForVideo(origin, websiteUrl, novelPlanetApiUrl, cookie);
        List<String> mp4Urls = getMp4UrlsFromRedirectorUrls(sourcesForVideo.getData(), origin, websiteUrl, cookie);
        setActualMp4UrlsFromNovelPlanetSources(sourcesForVideo, mp4Urls);

        mp4Urls.forEach(log::info);

        return sourcesForVideo;
    }

    private NovelPlanetSourceResponse getRedirectorSourcesForVideo(String origin, String websiteUrl, String apiUrl, String cookie) {
        // TODO headers to consider: "X-Forwarded-For", "X-Real-IP", "Host"
        HttpEntity<Void> request = CorsProxy.getCorsEntityWithCookie(null, origin, websiteUrl, cookie);

        ResponseEntity<NovelPlanetSourceResponse> response = new RestTemplate().exchange(
            apiUrl,
            HttpMethod.POST,
            request,
            NovelPlanetSourceResponse.class
        );

        return response.getBody();
    }

    private List<String> getMp4UrlsFromRedirectorUrls(
        List<NovelPlanetSourceResponse.NovelPlanetSource> redirectorSources,
        String novelPlanetOrigin,
        String novelPlanetWebsiteUrl,
        String novelPlanetCookie
    ) {
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

                String mp4UrlFromRedirector = redirectorResponse.getHeaders().getFirst("Location");

                boolean getMp4Headers = false;
                if (getMp4Headers) {
                    HttpHeaders mp4Headers = new RestTemplate().headForHeaders(mp4UrlFromRedirector);
                    // TODO get "Content-Length" from header for front-end
                    //  Might not be necessary depending on Range header for
                    //  front-end's video-buffer requests.
                    //  See for front-end buffering scheme:
                    //  - https://developers.google.com/web/fundamentals/media/fast-playback-with-video-preload
                    log.info("Headers for MP4 redirect request = {}", mp4Headers);
                }

                return mp4UrlFromRedirector;
            })
            .collect(Collectors.toList());
    }

    private void setActualMp4UrlsFromNovelPlanetSources(NovelPlanetSourceResponse novelPlanetSourceResponse, List<String> mp4Urls) {
        List<NovelPlanetSourceResponse.NovelPlanetSource> novelPlanetSources = novelPlanetSourceResponse.getData();

        for (int i = 0; i < novelPlanetSources.size(); i++) {
            NovelPlanetSourceResponse.NovelPlanetSource novelPlanetSource = novelPlanetSources.get(i);
            String mp4Url = mp4Urls.get(i);

            novelPlanetSource.setFile(mp4Url);
        }
    }

    public ResponseEntity<Resource> getVideoSrcStreamFromMp4Url(String url) throws MalformedURLException {
        // TODO this is a "dumb" video forwarding system that doesn't
        //  keep track of client's location, meaning that they can't
        //  seek through the video; when they click on a certain
        //  spot in the seek feed, the back-end reloads the whole video
        //  and trashes whatever was cached.
        //  Suggested tutorials:
        //  - https://melgenek.github.io/spring-video-service
        //  - https://www.baeldung.com/spring-resttemplate-download-large-file
        //  These use StreamingResponseBody, which is spring-web, not spring-webflux
        //  - https://dzone.com/articles/streaming-data-with-spring-boot-restful-web-servic
        //  - https://stackoverflow.com/questions/47277640/how-to-proxy-a-http-video-stream-to-any-amount-of-clients-through-a-spring-webse
        UrlResource videoResource = new UrlResource(url);

        return ResponseEntity
            .status(HttpStatus.PARTIAL_CONTENT)
            .contentType(
                MediaTypeFactory
                    .getMediaType(videoResource)
                    .orElse(MediaType.valueOf(
                        MediaType.APPLICATION_OCTET_STREAM_VALUE
                    ))
            )
            .body(videoResource);
    }
}
