package org.animeatsume.api.service;

import org.animeatsume.api.utils.http.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Service
public class VideoFileService {
    private static final Logger log = LoggerFactory.getLogger(VideoFileService.class);
    private static final String BASE_DIRECTORY = ".";
    private static final String VIDEO_DIRECTORY = "videos";
    private static final String NAME_SEPARATOR = "-";
    private static final String MP4_EXTENSION = ".mp4";

    private String getVideoDirectory() {
        return BASE_DIRECTORY + File.separator + VIDEO_DIRECTORY;
    }

    private String getShowPath(String showName) {
        return getVideoDirectory() + File.separator + showName;
    }

    private String getEpisodeFilePath(String showName, String episodeName, String quality) {
        return getShowPath(showName) + File.separator + episodeName + NAME_SEPARATOR + quality + MP4_EXTENSION;
    }

    private synchronized boolean createFileIfNotExists(String showName, String episodeName, String quality) {
        String showPath = getShowPath(showName);
        File showDirectory = new File(showPath);
        String episodeFilePath = getEpisodeFilePath(showName, episodeName, quality);
        File episodeFile = new File(episodeFilePath);

        boolean success = true;

        if (!showDirectory.exists()) {
            success = showDirectory.mkdirs();

            if (success) {
                log.info("New directory for show ({}) was created at path ({})",
                    showName,
                    showDirectory.getAbsolutePath()
                );
            } else {
                log.error("Failed to make directory for show ({}). Current process' working directory = ({})",
                    showName,
                    showDirectory.getAbsolutePath()
                );
            }
        }

        if (success && !episodeFile.exists()) {
            try {
                success = episodeFile.createNewFile();
            } catch (IOException e) {
                log.error("Failed to make new episode file ({}). Error cause ({}), message = {}",
                    episodeFilePath,
                    e.getCause(),
                    e.getMessage()
                );

                success = false;
            }

            if (!success) {
                log.error("Failed to make new episode file ({})", episodeFilePath);
            }
        }

        return success;
    }

    public File getVideoFile(String showName, String episodeName, String quality) {
        if (createFileIfNotExists(showName, episodeName, quality)) {
            return new File(getEpisodeFilePath(showName, episodeName, quality));
        }

        return null;
    }

    @Async
    public CompletableFuture<Boolean> saveNewVideoFile(String url, String showName, String episodeName, String quality) {
        File videoFile = getVideoFile(showName, episodeName, quality);

        if (videoFile != null) {
            RestTemplate mp4Request = new RestTemplate();
            Requests.addAcceptableMediaTypes(mp4Request, MediaType.parseMediaType("video/mp4"));

            Boolean success = mp4Request.execute(
                url,
                HttpMethod.GET,
                null,
                clientHttpResponse -> {
                    log.info("Video request at ({}) returned with status ({})",
                        url,
                        clientHttpResponse.getStatusCode()
                    );

                    try (
                        InputStream responseBodyInputStream = clientHttpResponse.getBody();
                        FileOutputStream fileOutputStream = new FileOutputStream(videoFile)
                    ) {
                        StreamUtils.copy(responseBodyInputStream, fileOutputStream);
                    } catch (IOException e) {
                        log.error("Failed to download video from URL ({}) to file ({}). Error cause ({}), message = {}",
                            url,
                            videoFile.getAbsolutePath(),
                            e.getCause(),
                            e.getMessage()
                        );

                        return false;
                    }

                    log.info(
                        "Video from URL ({}} downloaded to ({})",
                        url,
                        videoFile.getAbsolutePath()
                    );

                    return true;
                }
            );

            return CompletableFuture.completedFuture(success != null && success);
        }

        return CompletableFuture.completedFuture(false);
    }
}
