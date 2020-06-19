package org.animeatsume.api.controller;

import org.animeatsume.api.model.NovelPlanetSourceResponse;
import org.animeatsume.api.model.NovelPlanetUrlRequest;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.http.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.animeatsume.api.utils.http.Cookies.getCookieFromWebsite;

// TODO convert to service, autowire in KissanimeRuController,
//  and do all this novelplanet logic behind the scenes
@Component
public class NovelPlanetController {
    private static final Logger log = LoggerFactory.getLogger(NovelPlanetController.class);
    private static final String websiteIdentifier = "/v/";
    private static final String apiIdentifier = "/api/source/";
    private static final String protocolOriginSeparator = "://";

    public NovelPlanetSourceResponse getNovelPlanetSources(NovelPlanetUrlRequest novelPlanetRequest) {
        // TODO forward client request instead of making new one
        //  in order to preserve original IP address
        //
        // TODO see if there's an easier way than all these URL.get() methods
        URL websiteUrlObj = novelPlanetRequest.getNovelPlanetUrl();
        String protocol = websiteUrlObj.getProtocol();
        String hostAndPort = websiteUrlObj.getAuthority();
        String path = websiteUrlObj.getPath();
        String websiteUrl = websiteUrlObj.toString();
        String origin = protocol + protocolOriginSeparator + hostAndPort;
        String videoId = path.split(websiteIdentifier)[1];
        String novelPlanetApiUrl = origin + apiIdentifier + videoId;

        log.info("URL obj = {}, protocol = {}, hostAndPort = {}, path = {}, websiteUrl = {}, origin = {}, videoId = {}, novelApiUrl = {}", websiteUrlObj, protocol, hostAndPort, path, websiteUrl, origin, videoId, novelPlanetApiUrl);

        String cookie = getCookieFromWebsite(websiteUrl);
        NovelPlanetSourceResponse sourcesForVideo =
            getRedirectorSourcesForVideo(origin, websiteUrl, novelPlanetApiUrl, cookie);
        List<String> mp4Urls = getMp4UrlsFromRedirectorUrls(sourcesForVideo.getData(), origin, websiteUrl, cookie);
        setActualMp4UrlsFromNovelPlanetSources(sourcesForVideo, mp4Urls);

        mp4Urls.forEach(log::info);

        return sourcesForVideo;
    }

    private NovelPlanetSourceResponse getRedirectorSourcesForVideo(String origin, String websiteUrl, String apiUrl, String cookie) {
        // TODO headers to consider: "X-Forwarded-For", "X-Real-IP", "Host"
        HttpEntity<Void> request = CorsProxy.getCorsEntityWithCookie(null, origin, websiteUrl, cookie);

        ResponseEntity<NovelPlanetSourceResponse> response = new RestTemplate().exchange(
            apiUrl,
            HttpMethod.POST,
            request,
            NovelPlanetSourceResponse.class
        );

        return response.getBody();
    }

    // TODO make Async
    private List<String> getMp4UrlsFromRedirectorUrls(
        List<NovelPlanetSourceResponse.NovelPlanetSource> redirectorSources,
        String novelPlanetOrigin,
        String novelPlanetWebsiteUrl,
        String novelPlanetCookie
    ) {
        return redirectorSources.stream()
            .map(novelPlanetSource -> {
                String redirectorUrl = novelPlanetSource.getFile();

                HttpEntity<Void> mp4Request = CorsProxy.getCorsEntityWithCookie(null, novelPlanetOrigin, novelPlanetWebsiteUrl, novelPlanetCookie);

                // will give 302 (Found) with redirect. Don't follow it, instead get the redirect URL
                // since that holds the URL to the MP4
                RestTemplate redirectorRequest = Requests.getNoFollowRedirectsRestTemplate();

                ResponseEntity<Void> redirectorResponse = redirectorRequest.exchange(
                    redirectorUrl,
                    HttpMethod.GET,
                    mp4Request,
                    Void.class
                );

                return redirectorResponse.getHeaders().getFirst("Location");
            })
            .collect(Collectors.toList());
    }

    private void setActualMp4UrlsFromNovelPlanetSources(NovelPlanetSourceResponse novelPlanetSourceResponse, List<String> mp4Urls) {
        List<NovelPlanetSourceResponse.NovelPlanetSource> novelPlanetSources = novelPlanetSourceResponse.getData();

        for (int i = 0; i < novelPlanetSources.size(); i++) {
            NovelPlanetSourceResponse.NovelPlanetSource novelPlanetSource = novelPlanetSources.get(i);
            String mp4Url = mp4Urls.get(i);

            novelPlanetSource.setFile(mp4Url);
        }
    }

    /*
     * TODO Find a way to send video response immediately even if not fully loaded.
     *  Options:
     *  1. If file doesn't exist, spawn thread to save the file, and return UrlResource. Subsequent requests will
     *     return the file. Hopefully, even if the file isn't finished downloading, what has finished downloading can
     *     still be returned. (This is just a theory and needs validating.)
     *  2. Return a stream and use "Range" header parsing: Even with the "Range" header, if the range is "bytes=X-"
     *     without an ending, a large chunk of file will still need to be loaded and returned. A stream could
     *     allow for streaming parts of the file as it's loaded instead of waiting for the whole thing to be
     *     loaded before sending a response.
     *
     * For the byte-range header:
     *  - https://stackoverflow.com/questions/28427339/how-to-implement-http-byte-range-requests-in-spring-mvc
     *
     * For streams:
     *  Some tutorial suggestions:
     *  - https://melgenek.github.io/spring-video-service
     *  - https://www.baeldung.com/spring-resttemplate-download-large-file
     *  - https://github.com/ItamarBenjamin/stream-rest-template/blob/master/src/main/java/com/ibinyami/spring/resttemplate/StreamRestTemplate.java
     *  - https://docs.spring.io/spring/docs/3.2.x/spring-framework-reference/html/resources.html
     *  Other tutorials, but these use StreamingResponseBody, which is spring-web, not spring-webflux
     *  - https://dzone.com/articles/streaming-data-with-spring-boot-restful-web-servic
     *  - https://stackoverflow.com/questions/47277640/how-to-proxy-a-http-video-stream-to-any-amount-of-clients-through-a-spring-webse
     *  Some solutions for (non-)reactive implementations
     *  Non-reactive solution:
     *  - https://stackoverflow.com/questions/20333394/return-a-stream-with-spring-mvcs-responseentity
     *    (not helpful b/c uses InputStreamResource which throws "Cannot convert an InputStreamResource to a ResourceRegion")
     *  - https://stackoverflow.com/questions/15951439/resteasy-client-3-0-handling-large-content
     *  - https://stackoverflow.com/questions/32812645/spring-boot-writing-media-image-mp3-mp4-file-to-response-output-stream
     *  Reactive solution:
     *  - Requires using WebFlux client instead of RestTemplate
     *    - WebFlux tutorial: https://spring.io/guides/gs/async-method/
     *    - Overview: https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-client
     *  - Consider a multi-part response
     *    - MultipartBodyBuilder (non-reactive)
     *    - Multipart (reactive): https://docs.spring.io/spring/docs/current/spring-framework-reference/integration.html#rest-template-multipart
     */
    public ResponseEntity<Resource> getVideoSrcStreamFromMp4Url(String url, String rangeHeader) {
        return Requests.getUrlResourceStreamResponse(url);
//        return getVideoSrcStreamFromMp4UrlFile(url, rangeHeader);
    }

    public ResponseEntity<Resource> getVideoSrcStreamFromMp4UrlFile(String url, String rangeHeader) {
        String baseDirectoryName = ".";
        String videoDirectoryName = "videos";
        String showDirectoryName = "Your Lie In April";
        String showDirectoryPath = baseDirectoryName + File.separator + videoDirectoryName + File.separator + showDirectoryName;
        String fileName = "Episode-1.mp4";
        String filePath = showDirectoryPath + File.separator + fileName;
        File showDirectory = new File(showDirectoryPath);
        File videoFileOnDisk = new File(filePath);

        if (!showDirectory.exists()) {
            boolean success = showDirectory.mkdirs();

            if (success) {
                log.info("New directory for show ({}) was created at path ({})",
                    showDirectoryName,
                    showDirectory.getAbsolutePath()
                );
            } else {
                log.error("Failed to make directory for show ({}). Current process' working directory = ({})",
                    showDirectoryName,
                    new File(baseDirectoryName).getAbsolutePath()
                );

                return ResponseEntity.noContent().build();
            }
        }

        if (!videoFileOnDisk.exists()) {
            boolean success = saveNewVideoFromUrl(url, rangeHeader, videoFileOnDisk);

            if (!success) {
                return ResponseEntity.noContent().build();
            }
        }

        FileSystemResource resourceForResponseBody = new FileSystemResource(videoFileOnDisk);

        return ResponseEntity
            .status(HttpStatus.PARTIAL_CONTENT)
            .contentType(
                MediaTypeFactory
                    .getMediaType(resourceForResponseBody)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM) // alt: MediaType.asMediaType(MimeType.valueOf("video/mp4"))
            )
            .body(resourceForResponseBody);
    }

    public boolean saveNewVideoFromUrl(String url, String rangeHeader, File videoFileOnDisk) {
        RestTemplate mp4Request = new RestTemplate();
        Requests.addAcceptableMediaTypes(mp4Request, MediaType.parseMediaType("video/mp4"));

        Boolean success = mp4Request.execute(
            url,
            HttpMethod.GET,
            clientHttpRequest -> {
                HttpHeaders headers = clientHttpRequest.getHeaders();

                if (rangeHeader != null && !rangeHeader.isEmpty()) {
                    headers.set("Range", rangeHeader);
                }
            },
            clientHttpResponse -> {
                log.info(
                    "Response callback: status({}), headers({})",
                    clientHttpResponse.getStatusCode(),
                    clientHttpResponse.getHeaders()
                );

                try (
                    InputStream responseBodyInputStream = clientHttpResponse.getBody();
                    FileOutputStream fileOutputStream = new FileOutputStream(videoFileOnDisk)
                ) {
                    StreamUtils.copy(responseBodyInputStream, fileOutputStream);
                } catch (IOException e) {
                    log.error("Failed to download video from URL({}) to file ({}). Error:", url, videoFileOnDisk.getAbsolutePath());
                    e.printStackTrace();

                    return false;
                }

                log.info(
                    "Response callback: video from ({}} downloaded to ({})",
                    url,
                    videoFileOnDisk.getAbsolutePath()
                );

                return true;
            }
        );

        return success != null && success;
    }

    // Unsuccessful attempt from non-reactive solution from bullet-points above
    // at URL: https://stackoverflow.com/questions/15951439/resteasy-client-3-0-handling-large-content
//    public ResponseEntity<Resource> getVideoSrcStreamFromMp4UrlStreamingHttp(String url, String rangeHeader) throws IOException {
//        log.info("Getting MP4 sream with range header=[{}] for url=[{}]", rangeHeader, url);
//
//        UrlResource mp4UrlResource = new UrlResource(url);
//        InputStream mp4InputStream = mp4UrlResource.getInputStream();
//        HttpHeaders mp4Headers = new RestTemplate().headForHeaders(url);
//        List<String> contentLengthHeader = mp4Headers.get("Content-Length");
//
//        log.info("Mime type = {}", MediaTypeFactory.getMediaType(mp4UrlResource));
//
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.setContentType(
//            MediaTypeFactory
//                .getMediaType(mp4UrlResource)
//                .orElse(MediaType.valueOf(
//                    MediaType.APPLICATION_OCTET_STREAM_VALUE
//                ))
//        );
//
//        RestTemplate mp4Request = new RestTemplate();
//        Requests.addAcceptableMediaTypes(mp4Request, MediaType.parseMediaType("video/mp4"));
//
//        log.info("Before execute");
//        ClientHttpResponse mp4Response = mp4Request.execute(
//            url,
//            HttpMethod.GET,
//            clientHttpRequest -> {
//                HttpHeaders headers = clientHttpRequest.getHeaders();
//                headers.set("Range", rangeHeader);
//
//                try (OutputStream requestOutputStream = clientHttpRequest.getBody()) {
//                    StreamUtils.copy(mp4InputStream, requestOutputStream);
//                }
//            },
//            clientHttpResponse -> clientHttpResponse
//        );
//
//        log.info("After execute, before StreamingOutput");
//
//        StreamingHttpOutputMessage streamingResponseBody = new StreamingHttpOutputMessage() {
//            Body body = outputStream -> StreamUtils.copy(mp4InputStream, outputStream);
//
//            @Override
//            public void setBody(Body body) {
//                this.body = body;
//            }
//
//            @Override
//            public OutputStream getBody() throws IOException {
//                return null;
//            }
//
//            @Override
//            public HttpHeaders getHeaders() {
//                return responseHeaders;
//            }
//        };
//
//        log.info("Trying to return ResponseEntity");
//
//        try {
//            return new ResponseEntity(streamingResponseBody, responseHeaders, HttpStatus.PARTIAL_CONTENT);
//        } catch(Exception e) {
//            log.error("Error returning ResponseEntity<StreamingHttpOutputMessage>:");
//            e.printStackTrace();
//        }
//
//        return ResponseEntity.noContent().build();
//    }
}
