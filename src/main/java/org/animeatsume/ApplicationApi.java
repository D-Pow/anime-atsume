package org.animeatsume;

import org.animeatsume.api.controller.KissanimeRuController;
import org.animeatsume.api.controller.NovelPlanetController;
import org.animeatsume.api.model.KissanimeSearchRequest;
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
    public void searchKissanime(@RequestBody KissanimeSearchRequest kissanimeSearchRequest) {
        kissanimeRuController.searchKissanimeTitles(kissanimeSearchRequest);
    }

    @CrossOrigin
    @PostMapping(value = "/getNovelPlanetSources", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NovelPlanetSourceResponse getNovelPlanetSources(@RequestBody NovelPlanetUrlRequest novelPlanetRequest, ServerHttpRequest request) {
        log.info("Address is {}", request.getRemoteAddress().getAddress().toString());
        log.info("Port is {}", request.getRemoteAddress().getPort());

        return novelPlanetController.getNovelPlanetSources(novelPlanetRequest, request);
    }

    /*
(async () => {
    const res = await fetch('http://localhost:8080/getNovelPlanetSources', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            "novelPlanetUrl": "https://www.novelplanet.me/v/4dvj42p-yv1"
        })
    }).then(res => res.json());

    const { file } = res.data[0];
    const urlPrefix = 'http://localhost:8080/novelPlanetVideo?url=';

    try {
        document.body.removeChild(document.querySelector('video'));
    } catch(e) {}

    const video = document.createElement('video');
    const source = document.createElement('source')

    video.controls = true
    source.type = 'video/mp4'
    source.src = urlPrefix + file;

    video.appendChild(source);
    document.body.appendChild(video);

    console.log(file);
})()
     */
    @CrossOrigin
    @GetMapping(value = "/novelPlanetVideo", produces = { "video/mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<Resource> getNovelPlanetVideoStream(
        @RequestParam("url") String novelPlanetUrl,
        @RequestHeader("Range") String rangeHeader
    ) {
        return novelPlanetController.getVideoSrcStreamFromMp4Url(novelPlanetUrl, rangeHeader);
    }
}
