package org.animeatsume;

import org.animeatsume.api.controller.KissanimeRuController;
import org.animeatsume.api.service.NovelPlanetService;
import org.animeatsume.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApplicationApi {
    private static final Logger log = LoggerFactory.getLogger(ApplicationApi.class);

    @Autowired
    KissanimeRuController kissanimeRuController;

    @Autowired
    NovelPlanetService novelPlanetService;

    @CrossOrigin
    @PostMapping(value = "/searchKissanime", consumes = MediaType.APPLICATION_JSON_VALUE)
    public KissanimeSearchResponse searchKissanime(@RequestBody KissanimeSearchRequest kissanimeSearchRequest) {
        return kissanimeRuController.searchKissanimeTitles(kissanimeSearchRequest);
    }

    @CrossOrigin
    @PostMapping(value = "/getVideosForEpisode")
    public ResponseEntity<Object> getVideoHostUrlForKissanimeEpisode(@RequestBody KissanimeVideoHostRequest kissanimeEpisodeRequest, ServerHttpResponse response) {
        return kissanimeRuController.getVideosForKissanimeEpisode(kissanimeEpisodeRequest, response);
    }

    @CrossOrigin
    @PostMapping(value = "/getNovelPlanetSources", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NovelPlanetSourceResponse getNovelPlanetSources(@RequestBody NovelPlanetUrlRequest novelPlanetRequest) {
        return kissanimeRuController.getVideoSourcesForNovelPlanetHost(novelPlanetRequest.getNovelPlanetUrl().toString());
    }

    @CrossOrigin
    @GetMapping(value = "/novelPlanetVideo", produces = { "video/mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<Resource> getNovelPlanetVideoStream(
        @RequestParam("url") String novelPlanetUrl,
        @RequestHeader("Range") String rangeHeader
    ) {
        return novelPlanetService.getVideoSrcStreamFromMp4Url(novelPlanetUrl, rangeHeader);
    }
}
