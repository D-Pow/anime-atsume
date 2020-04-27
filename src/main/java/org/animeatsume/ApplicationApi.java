package org.animeatsume;

import org.animeatsume.api.controller.NovelPlanetController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApplicationApi {
    private static final Logger log = LoggerFactory.getLogger(ApplicationApi.class);

    @Autowired
    NovelPlanetController novelPlanetController;

    @CrossOrigin
    @PostMapping("/novelPlanet")
    public void getNovelPlanetMp4Urls(ServerHttpRequest request) {
        log.info("Address is {}", request.getRemoteAddress().getAddress().toString());
        log.info("Port is {}", String.valueOf(request.getRemoteAddress().getPort()));
    }
}
