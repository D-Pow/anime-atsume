package org.animeatsume.api.model.kissanime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaAttempt {
    private String formId;
    private String imageId;
    private String imageHash;
    private String promptText;
}
