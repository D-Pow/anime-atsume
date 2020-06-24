package org.animeatsume.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class KissanimeVideoHostRequest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CaptchaAnswerRequest {
        private String formId;
        private String imageId;
        private String promptText;
    }

    private String episodeUrl;
    private List<CaptchaAnswerRequest> captchaAnswers;
}
