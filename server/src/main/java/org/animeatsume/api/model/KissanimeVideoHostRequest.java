package org.animeatsume.api.model;

import lombok.Data;

import java.util.List;

@Data
public class KissanimeVideoHostRequest {
    private String episodeUrl;
    private List<CaptchaAttempt> captchaAnswers;
}
