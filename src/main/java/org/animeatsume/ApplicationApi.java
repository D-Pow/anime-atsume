package org.animeatsume;

import org.animeatsume.api.controller.NovelPlanetController;
import org.animeatsume.api.model.NovelPlanetSourceResponse;
import org.animeatsume.api.model.NovelPlanetUrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApplicationApi {
    private static final Logger log = LoggerFactory.getLogger(ApplicationApi.class);

    @Autowired
    NovelPlanetController novelPlanetController;

    @CrossOrigin
    @PostMapping(value = "/getNovelPlanetSources", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NovelPlanetSourceResponse getNovelPlanetSources(@RequestBody NovelPlanetUrlRequest novelPlanetRequest, ServerHttpRequest request) {
        log.info("Address is {}", request.getRemoteAddress().getAddress().toString());
        log.info("Port is {}", request.getRemoteAddress().getPort());

        return novelPlanetController.getNovelPlanetSources(novelPlanetRequest, request);
    }
}
