package org.animeatsume.dao;

import org.animeatsume.dao.model.CaptchaAnswer;

import java.util.List;

public interface AnimeAtsumeDao {
    List<CaptchaAnswer> getAllCaptchaAnswersByPrompt(String prompt);
    CaptchaAnswer saveNewCaptchaAnswer(CaptchaAnswer captchaAnswer);
    List<CaptchaAnswer> saveNewCaptchaAnswers(List<CaptchaAnswer> captchaAnswers);
}
