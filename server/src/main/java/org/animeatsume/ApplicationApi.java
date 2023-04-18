package org.animeatsume;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.controller.FourAnimeController;
import org.animeatsume.api.controller.KissanimeRuController;
import org.animeatsume.api.controller.NineAnimeController;
import org.animeatsume.api.controller.ZoroToController;
import org.animeatsume.api.model.SearchAnimeResponse;
import org.animeatsume.api.model.TitleSearchRequest;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.kissanime.KissanimeVideoHostRequest;
import org.animeatsume.api.model.kissanime.NovelPlanetUrlRequest;
import org.animeatsume.api.service.NovelPlanetService;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.regex.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

@RestController
@CrossOrigin
@Log4j2
public class ApplicationApi {

    @Autowired
    KissanimeRuController kissanimeRuController;

    @Autowired
    FourAnimeController fourAnimeController;

    @Autowired
    NovelPlanetService novelPlanetService;

    @Autowired
    NineAnimeController nineAnimeController;

    @Autowired
    ZoroToController zoroToController;

    @Value("${org.animeatsume.activate-kissanime}")
    Boolean activateKissanime;

    @GetMapping(value = "/corsProxy", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getCorsRequest(
        @RequestParam("url") URI url,
        @RequestParam(value = "origin", required = false) URI origin,
        @RequestHeader HttpHeaders requestHeaders,
        HttpServletRequest request
    ) {
        return postCorsRequest(null, url, origin, requestHeaders, request);
    }

    @PostMapping(value = "/corsProxy", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> postCorsRequestWithFormData(
        @RequestParam(required = false) Map<String, String> body,
        @RequestParam("url") URI url,
        @RequestParam(value = "origin", required = false) URI origin,
        @RequestHeader HttpHeaders requestHeaders,
        HttpServletRequest request
    ) {
        body.remove("url");
        body.remove("origin");

        return postCorsRequest(body, url, origin, requestHeaders, request);
    }

    @PostMapping("/corsProxy")
    public ResponseEntity<?> postCorsRequest(
        @RequestBody(required = false) Object body,
        @RequestParam("url") URI url,
        @RequestParam(value = "origin", required = false) URI origin,
        @RequestHeader HttpHeaders requestHeaders,
        HttpServletRequest request
    ) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        log.info("Executing {} CORS request to URL ({}) with body ({})", method, url, body);

        return CorsProxy.doCorsRequest(method, url, origin, body, requestHeaders);
    }

    // Like most annotations, for `@Cacheable`, we can only use either a `static final` var (from another class)
    // or use the .properties var key directly (without the "proxy" of another class).
    // However, we can't use the .properties key to reduce duplicated code (so we don't have to repeat the .properties
    // var in source code), so we must duplicate the code here unfortunately.
    // See:
    //  - https://stackoverflow.com/questions/39013894/reading-from-application-properties-attribute-value-must-be-constant/39013994#39013994
    //@Cacheable(cacheNames = "${org.animeatsume.cache.anime-title-search}")
    @Cacheable(cacheNames = ApplicationConfig.ANIME_TITLE_SEARCH_CACHE_NAME)
    @PostMapping(value = "/searchAnime", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SearchAnimeResponse> searchAnime(@RequestBody TitleSearchRequest titleSearchRequest) {
        titleSearchRequest.setTitle(RegexUtils.removeNonAlphanumericChars(titleSearchRequest.getTitle()));

        if (activateKissanime) {
            return ResponseEntity
                .ok((SearchAnimeResponse) kissanimeRuController.searchKissanimeTitles(titleSearchRequest));
        }

        SearchAnimeResponse searchResults = null; // (SearchAnimeResponse) fourAnimeController.searchTitle(titleSearchRequest);

        if (searchResults == null || searchResults.getResults().size() == 0) {
            searchResults = new SearchAnimeResponse();
            searchResults.setResults(nineAnimeController.searchShows(titleSearchRequest).getResults());
        }

        if (searchResults == null || searchResults.getResults().size() == 0) {
            searchResults = new SearchAnimeResponse();
            searchResults.setResults(zoroToController.searchShows(titleSearchRequest).getResults());
        }

        if (searchResults == null || searchResults.getResults().size() == 0) {
            searchResults.setError("Anime servers are currently down :/");
        }

        if (searchResults == null || searchResults.getResults().size() == 0) {
            searchResults = nineAnimeController.searchShows(titleSearchRequest);
        }

        return ResponseEntity
            .ok(searchResults);
    }

    @PostMapping(value = "/getVideosForEpisode")
    public ResponseEntity<Object> getVideosForEpisode(@RequestBody KissanimeVideoHostRequest kissanimeEpisodeRequest) {
        if (activateKissanime) {
            return kissanimeRuController.getVideosForKissanimeEpisode(kissanimeEpisodeRequest);
        }

        TitlesAndEpisodes.EpisodesForTitle videosForEpisode = fourAnimeController.getVideoForEpisode(kissanimeEpisodeRequest.getEpisodeUrl());

        if (videosForEpisode == null || videosForEpisode.getEpisodes().size() == 0) {
            videosForEpisode = nineAnimeController.getVideoForEpisode(kissanimeEpisodeRequest.getEpisodeUrl());
        }

        if (videosForEpisode != null && videosForEpisode.getEpisodes().size() > 0) {
            return ResponseEntity.ok(videosForEpisode);
        }

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .build();
    }

    @PostMapping(value = "/getNovelPlanetSources", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TitlesAndEpisodes.EpisodesForTitle> getNovelPlanetSources(@RequestBody NovelPlanetUrlRequest novelPlanetRequest) {
        return ResponseEntity
            .ok(kissanimeRuController.normalizeNovelPlanetSources(
                kissanimeRuController.getVideoSourcesForNovelPlanetHost(novelPlanetRequest.getNovelPlanetUrl().toString())
            ));
    }

    @GetMapping(value = "/video/{show}/{episode}/{quality}", produces = { "video/mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<Resource> getProxiedVideoStream(
        @PathVariable("show") String showName,
        @PathVariable("episode") String episodeName,
        @PathVariable("quality") String videoQuality,
        @RequestParam("url") String videoUrl,
        @RequestHeader HttpHeaders requestHeaders
    ) {
        String validCharactersRegex = "^[a-zA-Z0-9-]+$"; // alphanumeric and '-'
        boolean paramsAreValid = (
            showName.matches(validCharactersRegex)
            && episodeName.matches(validCharactersRegex)
            && videoQuality.matches(validCharactersRegex)
        );

        if (!paramsAreValid || videoUrl == null || videoUrl.isEmpty()) {
            log.info("Invalid video stream parameters: showName ({}), episodeName ({}), videoQuality ({}), url ({})",
                showName,
                episodeName,
                videoQuality,
                videoUrl
            );
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .build();
        }

        try {
            return kissanimeRuController.getProxiedVideoStream(
                showName,
                episodeName,
                videoQuality,
                videoUrl,
                requestHeaders
            );
        } catch (Exception e) {
            log.error("Could not proxy video stream. Error: {}", e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        }
    }

    @GetMapping(value = "/image/{id}", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<Resource> getKissanimeCaptchaImage(@PathVariable("id") String imageId) {
        if (activateKissanime) {
            return kissanimeRuController.getProxiedKissanimeCaptchaImage(imageId);
        }

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .build();
    }
}
