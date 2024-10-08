package org.animeatsume.service;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.model.kissanime.NovelPlanetSourceResponse;
import org.animeatsume.utils.http.Cookies;
import org.animeatsume.utils.http.CorsProxy;
import org.animeatsume.utils.http.Requests;
import org.animeatsume.utils.http.UriParser;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class NovelPlanetService {
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
        HttpHeaders redirectorHeaders = Requests.copyHttpHeaders(requestEntity.getHeaders());
        redirectorHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        ResponseEntity<NovelPlanetSourceResponse> response = new RestTemplate().exchange(
            apiUrl,
            HttpMethod.POST,
            new HttpEntity<>(requestEntity.getBody(), redirectorHeaders),
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
        List<NovelPlanetSourceResponse.NovelPlanetSource> videoSources = sourceResponse.getData();
        log.info("Removing lower quality MP4 sources from response. Original length = {}", videoSources.size());

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
