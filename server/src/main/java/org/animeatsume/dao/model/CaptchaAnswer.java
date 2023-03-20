package org.animeatsume.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.io.Serializable;

@Entity
@Table(name = "captcha_answers")
@IdClass(CaptchaAnswer.CaptchaAnswerPk.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaAnswer {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CaptchaAnswerPk implements Serializable {
        private String prompt;
        private String imageHash;
    }

    @Id
    @Column(name = "prompt")
    private String prompt;

    @Id
    @Column(name = "image_hash")
    private String imageHash;
}
