package org.animeatsume;

import org.animeatsume.api.controller.KissanimeRuController;
import org.animeatsume.api.controller.NovelPlanetController;
import org.animeatsume.api.model.KissanimeSearchRequest;
import org.animeatsume.api.model.KissanimeSearchResponse;
import org.animeatsume.api.model.NovelPlanetSourceResponse;
import org.animeatsume.api.model.NovelPlanetUrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApplicationApi {
    private static final Logger log = LoggerFactory.getLogger(ApplicationApi.class);

    @Autowired
    KissanimeRuController kissanimeRuController;

    @Autowired
    NovelPlanetController novelPlanetController;

    @CrossOrigin
    @PostMapping(value = "/searchKissanime", consumes = MediaType.APPLICATION_JSON_VALUE)
    public KissanimeSearchResponse searchKissanime(@RequestBody KissanimeSearchRequest kissanimeSearchRequest) {
        return kissanimeRuController.searchKissanimeTitles(kissanimeSearchRequest);
    }

    @CrossOrigin
    @PostMapping(value = "/getNovelPlanetSources", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NovelPlanetSourceResponse getNovelPlanetSources(@RequestBody NovelPlanetUrlRequest novelPlanetRequest, ServerHttpRequest request) {
        log.info("Address is {}", request.getRemoteAddress().getAddress().toString());
        log.info("Port is {}", request.getRemoteAddress().getPort());

        return novelPlanetController.getNovelPlanetSources(novelPlanetRequest, request);
    }

    @CrossOrigin
    @GetMapping(value = "/novelPlanetVideo", produces = { "video/mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<Resource> getNovelPlanetVideoStream(
        @RequestParam("url") String novelPlanetUrl,
        @RequestHeader("Range") String rangeHeader
    ) {
        return novelPlanetController.getVideoSrcStreamFromMp4Url(novelPlanetUrl, rangeHeader);
    }
}
