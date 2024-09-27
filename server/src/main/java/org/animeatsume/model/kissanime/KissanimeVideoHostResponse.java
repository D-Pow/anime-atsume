package org.animeatsume.model.kissanime;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KissanimeVideoHostResponse {
    @Data
    @AllArgsConstructor
    public static class CaptchaContent {
        private List<String> promptTexts;
        private List<CaptchaAttempt> imgIdsAndSrcs;
    }

    private String videoHostUrl;
    private CaptchaContent captchaContent;
}
