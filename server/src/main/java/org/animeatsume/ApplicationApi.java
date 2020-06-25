package org.animeatsume;

import org.animeatsume.api.controller.KissanimeRuController;
import org.animeatsume.api.service.NovelPlanetService;
import org.animeatsume.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
    @GetMapping(value = "/video/{show}/{episode}/{quality}", produces = { "video/mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<Resource> getNovelPlanetVideoStream(
        @PathVariable("show") String showName,
        @PathVariable("episode") String episodeName,
        @PathVariable("quality") String videoQuality,
        @RequestParam("url") String novelPlanetUrl,
        ServerHttpRequest request
    ) {
        String validCharactersRegex = "^[a-zA-Z0-9-]+$"; // alphanumeric and '-'
        boolean paramsAreValid = (
            showName.matches(validCharactersRegex)
            && episodeName.matches(validCharactersRegex)
            && videoQuality.matches(validCharactersRegex)
        );

        if (!paramsAreValid || novelPlanetUrl == null || novelPlanetUrl.isEmpty()) {
            log.info("Invalid video stream parameters: showName ({}), episodeName ({}), videoQuality ({}), url ({})",
                showName,
                episodeName,
                videoQuality,
                novelPlanetUrl
            );
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .build();
        }

        return kissanimeRuController.getNovelPlanetVideoStream(
            showName,
            episodeName,
            videoQuality,
            novelPlanetUrl,
            request
        );
    }

    @CrossOrigin
    @GetMapping(value = "/image/{id}", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<Resource> getKissanimeCaptchaImage(@PathVariable("id") String imageId) {
        return kissanimeRuController.getProxiedKissanimeCaptchaImage(imageId);
    }
}
