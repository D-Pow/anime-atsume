package org.animeatsume.controller;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.model.Anchor;
import org.animeatsume.model.TitleSearchRequest;
import org.animeatsume.model.TitlesAndEpisodes;
import org.animeatsume.model.kissanime.CaptchaAttempt;
import org.animeatsume.model.kissanime.KissanimeVideoHostRequest;
import org.animeatsume.model.kissanime.KissanimeVideoHostResponse;
import org.animeatsume.model.kissanime.NovelPlanetSourceResponse;
import org.animeatsume.service.KissanimeRuService;
import org.animeatsume.service.NovelPlanetService;
import org.animeatsume.service.VideoFileService;
import org.animeatsume.utils.ObjectUtils;
import org.animeatsume.utils.http.CorsProxy;
import org.animeatsume.utils.http.Requests;
import org.animeatsume.utils.regex.RegexUtils;
import org.animeatsume.dao.AnimeAtsumeDao;
import org.animeatsume.dao.model.CaptchaAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Controller
@Log4j2
public class KissanimeRuController implements ShowSearchController {
    @Value("${org.animeatsume.download-videos}")
    private Boolean downloadVideos;

    @Value("${org.animeatsume.extract-highest-resolution-video-only}")
    private Boolean extractHighestResVideoOnly;

    @Autowired
    KissanimeRuService kissanimeService;

    @Autowired
    NovelPlanetService novelPlanetService;

    @Autowired
    VideoFileService videoFileService;

    @Autowired
    AnimeAtsumeDao dao;

    public TitlesAndEpisodes searchShows(TitleSearchRequest titleSearchRequest) {
        TitlesAndEpisodes titlesAndEpisodes = kissanimeService.searchKissanimeTitles(titleSearchRequest);
        List<TitlesAndEpisodes.EpisodesForTitle> titleSearchResults = (List<TitlesAndEpisodes.EpisodesForTitle>) titlesAndEpisodes.getResults();

        if (titleSearchResults != null) {
            List<CompletableFuture<List<Anchor>>> episodeSearchResultsFutures = new ArrayList<>();

            titleSearchResults.forEach(titleAnchor -> {
                episodeSearchResultsFutures.add(kissanimeService.searchKissanimeEpisodes(titleAnchor.getUrl()));
            });

            ObjectUtils.getAllCompletableFutureResults(episodeSearchResultsFutures, (episodeAnchorList, index) -> {
                TitlesAndEpisodes.EpisodesForTitle episodeSearchResult = titleSearchResults.get(index);
                List<Anchor> episodeLinks = episodeAnchorList != null ? episodeAnchorList : new ArrayList<>();

                episodeSearchResult.setEpisodes(episodeLinks, false);
            });
        }

        return titlesAndEpisodes;
    }

    public TitlesAndEpisodes.EpisodesForTitle getVideosForEpisode(String url) {
        return getVideosForEpisode(new KissanimeVideoHostRequest(url, null));
    }

    public TitlesAndEpisodes.EpisodesForTitle getVideosForEpisode(KissanimeVideoHostRequest request) {
        KissanimeVideoHostResponse videoHost = getVideoHostUrlForKissanimeEpisode(request);
        String videoHostUrl = videoHost.getVideoHostUrl();
        Object body = videoHost;

        if (videoHostUrl != null && !videoHostUrl.isEmpty()) {
            if (videoHostUrl.contains(NovelPlanetService.DOMAIN)) {
                NovelPlanetSourceResponse videoSources = getVideoSourcesForNovelPlanetHost(videoHostUrl);

                if (extractHighestResVideoOnly) {
                    novelPlanetService.removeLowQualityVideos(videoSources);
                }

                if (downloadVideos) {
                    initiateEpisodeVideoDownloads(request.getEpisodeUrl(), videoSources);
                }

                body = normalizeNovelPlanetSources(videoSources);
            }
        }

        return (TitlesAndEpisodes.EpisodesForTitle) body;
    }

    private KissanimeVideoHostResponse getVideoHostUrlForKissanimeEpisode(KissanimeVideoHostRequest request) {
        log.info("KissanimeVideoHostRequest = {}", request);

        String kissanimeEpisodeUrl = request.getEpisodeUrl();
        List<CaptchaAttempt> captchaAnswers = request.getCaptchaAnswers();

        if (captchaAnswers == null || captchaAnswers.size() == 0) {
            if (kissanimeService.requestIsRedirected(kissanimeEpisodeUrl)) {
                // Request is redirected because AreYouHuman verification needs to be completed
                KissanimeVideoHostResponse responseWithCaptcha = getCaptchaContentWithImageHash(kissanimeEpisodeUrl);

                List<String> captchaAnswersFoundInDb = attemptGettingCaptchaAnswerWithPreviousAnswers(responseWithCaptcha.getCaptchaContent());

                if (captchaAnswersFoundInDb != null) {
                    log.info("Captcha answers found in database. Trying to bypass captcha using them.");
                    boolean bypassSuccess = kissanimeService.executeBypassAreYouHumanCheckWithDbEntries(kissanimeEpisodeUrl, captchaAnswersFoundInDb);

                    if (!bypassSuccess) {
                        log.info("Failed to bypass captcha with database entries.");
                        return getCaptchaContentWithImageHash(kissanimeEpisodeUrl);
                    }

                    log.info("Succeeded in bypassing captcha with database entries.");
                    return kissanimeService.getVideoHostUrlFromEpisodePage(kissanimeEpisodeUrl);
                }

                return responseWithCaptcha;
            }

            return kissanimeService.getVideoHostUrlFromEpisodePage(kissanimeEpisodeUrl);
        }

        boolean bypassSuccess = kissanimeService.executeBypassAreYouHumanCheck(kissanimeEpisodeUrl, captchaAnswers);

        if (!bypassSuccess) {
            return getCaptchaContentWithImageHash(kissanimeEpisodeUrl);
        }

        saveCorrectCaptchaAnswers(request.getCaptchaAnswers());

        return kissanimeService.getVideoHostUrlFromEpisodePage(kissanimeEpisodeUrl);
    }

    private KissanimeVideoHostResponse getCaptchaContentWithImageHash(String kissanimeEpisodeUrl) {
        KissanimeVideoHostResponse response = kissanimeService.getBypassAreYouHumanPromptContent(kissanimeEpisodeUrl);
        List<CompletableFuture<String>> imageHashes = response.getCaptchaContent().getImgIdsAndSrcs().stream()
            .map(kissanimeService::setCaptchaImageHash)
            .collect(Collectors.toList());

        ObjectUtils.getAllCompletableFutureResults(imageHashes);

        return response;
    }

    private List<String> attemptGettingCaptchaAnswerWithPreviousAnswers(KissanimeVideoHostResponse.CaptchaContent captchaContent) {
        List<CaptchaAttempt> captchaImgIdsAndSrcs = captchaContent.getImgIdsAndSrcs();
        List<String> formIdsForImagesFoundInDb = new ArrayList<>();

        captchaContent.getPromptTexts().forEach(promptText -> {
            List<CaptchaAnswer> captchaAnswersForPrompt = dao.getAllCaptchaAnswersByPrompt(promptText);

            if (captchaAnswersForPrompt.size() > 0) {
                List<String> savedImageHashesForPrompt = captchaAnswersForPrompt.stream()
                    .map(CaptchaAnswer::getImageHash)
                    .collect(Collectors.toList());
                CaptchaAttempt captchaAttemptThatExistsInDb = ObjectUtils.findObjectInList(
                    captchaImgIdsAndSrcs,
                    captchaAttempt -> savedImageHashesForPrompt.contains(captchaAttempt.getImageHash())
                );

                if (captchaAttemptThatExistsInDb != null) {
                    log.info("Found captcha answer in DB for prompt text ({}) and prompt image ({})",
                        promptText,
                        captchaAttemptThatExistsInDb.getImageHash()
                    );
                    formIdsForImagesFoundInDb.add(captchaAttemptThatExistsInDb.getFormId());
                }
            }
        });

        if (formIdsForImagesFoundInDb.size() == captchaContent.getPromptTexts().size()) {
            return formIdsForImagesFoundInDb;
        }

        return null;
    }

    private void saveCorrectCaptchaAnswers(List<CaptchaAttempt> captchaAnswers) {
        log.info("Saving correct captcha answers to the DB: {}", captchaAnswers);

        if (captchaAnswers != null && captchaAnswers.size() > 0) {
            List<CaptchaAnswer> answers = captchaAnswers.stream()
                .map(captchaAnswerRequest -> new CaptchaAnswer(
                    captchaAnswerRequest.getPromptText(),
                    captchaAnswerRequest.getImageHash())
                )
                .collect(Collectors.toList());

            dao.saveNewCaptchaAnswers(answers);
        }
    }

    public ResponseEntity<Resource> getProxiedKissanimeCaptchaImage(String imageId) {
        return kissanimeService.getKissanimeCaptchaImage(imageId);
    }

    public NovelPlanetSourceResponse getVideoSourcesForNovelPlanetHost(String videoHostUrl) {
        log.info("Getting NovelPlanet MP4 sources for ({})", videoHostUrl);
        URI videoHostUri = URI.create(videoHostUrl);

        HttpEntity<Void> corsEntity = novelPlanetService.getCorsEntityForNovelPlanet(null, videoHostUri);
        String novelPlanetApiUrl = novelPlanetService.getApiUrlForHost(videoHostUri);
        NovelPlanetSourceResponse sourcesForVideo = novelPlanetService.getRedirectorSourcesForVideo(novelPlanetApiUrl, corsEntity);
        sourcesForVideo.setWebsiteUrl(videoHostUrl);

        List<CompletableFuture<Void>> mp4UrlCompletableFutures = sourcesForVideo.getData().stream()
            .map(novelPlanetSource -> novelPlanetService.getMp4UrlFromRedirectorUrl(novelPlanetSource, corsEntity))
            .collect(Collectors.toList());

        ObjectUtils.getAllCompletableFutureResults(mp4UrlCompletableFutures);

        log.info("Obtained {} MP4 sources for NovelPlanet URL ({})", sourcesForVideo.getData().size(), novelPlanetApiUrl);

        return sourcesForVideo;
    }

    private void initiateEpisodeVideoDownloads(String kissanimeEpisodeUrl, NovelPlanetSourceResponse novelPlanetSources) {
        String[] showAndEpisodeNames = kissanimeService.getShowAndEpisodeNamesFromEpisodeUrl(kissanimeEpisodeUrl);

        try {
            String showName = showAndEpisodeNames[0];
            String episodeName = showAndEpisodeNames[1];

            novelPlanetSources.getData().forEach(novelPlanetSource -> {
                String videoQuality = novelPlanetSource.getLabel();
                String videoUrl = novelPlanetSource.getFile();

                videoFileService.saveNewVideoFile(videoUrl, showName, episodeName, videoQuality);
            });
        } catch (Exception e) {
            log.error("Error initiating episode downloads for kissanime episode URL ({}) and NovelPlanetSourceResponse ({}). Error cause ({}), message = {}",
                kissanimeEpisodeUrl,
                novelPlanetSources,
                e.getCause(),
                e.getMessage()
            );
        }
    }

    public TitlesAndEpisodes.EpisodesForTitle normalizeNovelPlanetSources(NovelPlanetSourceResponse sources) {
        List<Anchor> episodeAnchors = sources.getData().stream()
            .map(novelPlanetSource -> new Anchor(
                novelPlanetSource.getFile(),
                novelPlanetSource.getLabel()
            )).collect(Collectors.toList());

        return new TitlesAndEpisodes.EpisodesForTitle(
            sources.getWebsiteUrl(),
            null,
            episodeAnchors,
            false
        );
    }

    public ResponseEntity<Resource> getProxiedVideoStream(
        String showName,
        String episodeName,
        String videoQuality,
        String videoUrl,
        HttpHeaders requestHeaders
    ) {
        log.info("Serving video stream: show name ({}), episode name ({}), video quality ({}), source URL ({})",
            showName,
            episodeName,
            videoQuality,
            videoUrl
        );

        if (!downloadVideos) {
            // Proxy video bytes from URL since the videos aren't being downloaded.
            // Buffer videos with default buffer defined in {@code getContentRangeStartAndEndAndLength()}.
            List<Long> ranges = Requests.getContentRangeStartAndEndAndLength(videoUrl, requestHeaders, false);

            HttpHeaders proxyHeaders = new HttpHeaders();
            proxyHeaders.setAccept(requestHeaders.getAccept());
            proxyHeaders.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            proxyHeaders.set(HttpHeaders.RANGE, String.format("bytes=%d-%d", ranges.get(0), ranges.get(1)));

            return CorsProxy.doCorsRequest(
                HttpMethod.GET,
                URI.create(videoUrl),
                null,
                null,
                proxyHeaders
            );
        }

        File videoFile = videoFileService.getVideoFile(showName, episodeName, videoQuality);

        if (videoFile == null) {
            log.info("Video file not found. Serving content from UrlResource at URL ({})", videoUrl);
            return Requests.getUrlResourceStreamResponse(videoUrl);
        }

        String rangeHeader = requestHeaders.getFirst("Range");

        if (rangeHeader == null) {
            rangeHeader = "";
        }

        List<String> matchesForRangeFirstByte = RegexUtils.getFirstMatchGroups("(?<=bytes=)(\\d+)(?=-)", rangeHeader);
        long firstBytePositionInRange = Long.parseLong(matchesForRangeFirstByte.size() > 0 ? matchesForRangeFirstByte.get(0) : "0");
        long minimumFileLength = 1000; // video files will definitely be larger than 1 kb

        if (videoFile.length() <= minimumFileLength || videoFile.length() <= firstBytePositionInRange) {
            log.info("Video file not finished downloading. File length ({}), requested first byte in 'Range' ({})",
                videoFile.length(),
                firstBytePositionInRange
            );
            return Requests.getUrlResourceStreamResponse(videoUrl);
        }

        FileSystemResource fileResource = new FileSystemResource(videoFile);
        log.info("Video file found. Serving FileSystemResource");

        return ResponseEntity
            .status(HttpStatus.PARTIAL_CONTENT)
            .contentType(
                MediaTypeFactory
                    .getMediaType(fileResource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM)
            )
            .body(fileResource);
    }
}
