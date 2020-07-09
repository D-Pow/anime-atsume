package org.animeatsume;

import org.animeatsume.api.controller.KissanimeRuController;
import org.animeatsume.api.service.NovelPlanetService;
import org.animeatsume.api.model.*;
import org.animeatsume.api.utils.http.CorsProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

@RestController
public class ApplicationApi {
    private static final Logger log = LoggerFactory.getLogger(ApplicationApi.class);

    @Autowired
    KissanimeRuController kissanimeRuController;

    @Autowired
    NovelPlanetService novelPlanetService;

    @CrossOrigin
    @GetMapping(value = "/corsProxy", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getCorsRequest(
        @RequestParam("url") URI url,
        @RequestHeader HttpHeaders requestHeaders,
        HttpServletRequest request
    ) {
        return postCorsRequest(null, url, requestHeaders, request);
    }

    @CrossOrigin
    @PostMapping(value = "/corsProxy", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> postCorsRequestWithFormData(
        @RequestParam Map<String, String> body,
        @RequestParam("url") URI url,
        @RequestHeader HttpHeaders requestHeaders,
        HttpServletRequest request
    ) {
        body.remove("url");

        return postCorsRequest(body, url, requestHeaders, request);
    }

    @CrossOrigin
    @PostMapping("/corsProxy")
    public ResponseEntity<?> postCorsRequest(
        @RequestBody(required = false) Object body,
        @RequestParam("url") URI url,
        @RequestHeader HttpHeaders requestHeaders,
        HttpServletRequest request
    ) {
        HttpMethod method = HttpMethod.resolve(request.getMethod());

        log.info("Executing {} CORS request to URL ({}) with body ({})", method, url, body);

        return CorsProxy.doCorsRequest(method, url, body, requestHeaders);
    }

    @CrossOrigin
    @PostMapping(value = "/searchKissanime", consumes = MediaType.APPLICATION_JSON_VALUE)
    public KissanimeSearchResponse searchKissanime(@RequestBody KissanimeSearchRequest kissanimeSearchRequest) {
        return kissanimeRuController.searchKissanimeTitles(kissanimeSearchRequest);
    }

    @CrossOrigin
    @PostMapping(value = "/getVideosForEpisode")
    public ResponseEntity<Object> getVideoHostUrlForKissanimeEpisode(@RequestBody KissanimeVideoHostRequest kissanimeEpisodeRequest) {
        return kissanimeRuController.getVideosForKissanimeEpisode(kissanimeEpisodeRequest);
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
        @RequestHeader HttpHeaders requestHeaders
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
            requestHeaders
        );
    }

    @CrossOrigin
    @GetMapping(value = "/image/{id}", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<Resource> getKissanimeCaptchaImage(@PathVariable("id") String imageId) {
        return kissanimeRuController.getProxiedKissanimeCaptchaImage(imageId);
    }
}
