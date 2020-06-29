package org.animeatsume.api.service;

import org.animeatsume.api.model.NovelPlanetSourceResponse;
import org.animeatsume.api.utils.http.Cookies;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.http.Requests;
import org.animeatsume.api.utils.http.UriParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.*;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class NovelPlanetService {
    private static final Logger log = LoggerFactory.getLogger(NovelPlanetService.class);
    private static final String websiteIdentifier = "/v/";
    private static final String apiIdentifier = "/api/source/";
    public static final String DOMAIN = "novelplanet";

    public <T> HttpEntity<T> getCorsEntityForNovelPlanet(T body, URI uri) {
        String url = uri.toString();
        String origin = UriParser.getOrigin(uri);
        String cookie = Cookies.getCookieFromWebsite(url);

        return CorsProxy.getCorsEntity(body, origin, url, cookie);
    }

    public String getApiUrlForHost(URI websiteHost) {
        return websiteHost.toString().replace(websiteIdentifier, apiIdentifier);
    }

    public NovelPlanetSourceResponse getRedirectorSourcesForVideo(String apiUrl, HttpEntity<Void> requestEntity) {
        ResponseEntity<NovelPlanetSourceResponse> response = new RestTemplate().exchange(
            apiUrl,
            HttpMethod.POST,
            requestEntity,
            NovelPlanetSourceResponse.class
        );

        return response.getBody();
    }

    @Async
    public CompletableFuture<Void> getMp4UrlFromRedirectorUrl(
        NovelPlanetSourceResponse.NovelPlanetSource novelPlanetSource,
        HttpEntity<Void> mp4RequestEntity
    ) {
        String redirectorUrl = novelPlanetSource.getFile();

        // will give 302 (Found) with redirect. Don't follow it, instead get the redirect URL
        // since that holds the URL to the MP4
        RestTemplate redirectorRequest = Requests.getNoFollowRedirectsRestTemplate();

        ResponseEntity<Void> redirectorResponse = redirectorRequest.exchange(
            redirectorUrl,
            HttpMethod.GET,
            mp4RequestEntity,
            Void.class
        );

        String mp4Url = redirectorResponse.getHeaders().getFirst("Location");

        novelPlanetSource.setFile(mp4Url);

        return CompletableFuture.completedFuture(null);
    }

    public void removeLowQualityVideos(NovelPlanetSourceResponse sourceResponse) {
        log.info("Removing lower quality MP4 sources from response");
        List<NovelPlanetSourceResponse.NovelPlanetSource> videoSources = sourceResponse.getData();

        NovelPlanetSourceResponse.NovelPlanetSource largestResSource = videoSources.stream()
            .reduce(null, (previous, next) -> {
                if (previous == null) {
                    return next;
                }

                String prevResLabel = previous.getLabel();
                int prevRes = Integer.parseInt(prevResLabel.replaceAll("[^\\d]", ""));
                String nextResLabel = next.getLabel();
                int nextRes = Integer.parseInt(nextResLabel.replaceAll("[^\\d]", ""));

                if (nextRes > prevRes) {
                    return next;
                }

                return previous;
            });

        log.info("Largest quality video extracted = {}", largestResSource);

        sourceResponse.setData(Collections.singletonList(largestResSource));
    }
}
