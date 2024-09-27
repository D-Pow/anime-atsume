package org.animeatsume.api.model.kissanime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KissanimeVideoHostRequest {
    private String episodeUrl;
    private List<CaptchaAttempt> captchaAnswers;
}
