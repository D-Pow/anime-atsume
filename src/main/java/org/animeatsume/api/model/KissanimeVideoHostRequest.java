package org.animeatsume.api.model;

import lombok.Data;

import java.util.List;

@Data
public class KissanimeVideoHostRequest {
    @Data
    public static class CaptchaAnswerRequest {
        private String formId;
        private String imageId;
        private String promptText;
    }

    private String episodeUrl;
    private List<CaptchaAnswerRequest> captchaAnswers;
}
