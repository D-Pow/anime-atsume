package org.animeatsume.dao.persistence;

import org.animeatsume.dao.model.CaptchaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CaptchaAnswerRepository extends JpaRepository<CaptchaAnswer, CaptchaAnswer.CaptchaAnswerPk> {
    @Query("SELECT captcha FROM CaptchaAnswer captcha WHERE captcha.prompt LIKE %:prompt%")
    List<CaptchaAnswer> getAllCaptchaAnswersByPrompt(@Param("prompt") String prompt);
}
