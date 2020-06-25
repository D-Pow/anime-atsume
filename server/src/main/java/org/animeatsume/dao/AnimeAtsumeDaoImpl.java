package org.animeatsume.dao;

import org.animeatsume.dao.model.CaptchaAnswer;
import org.animeatsume.dao.persistence.CaptchaAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("AnimeAtsumeDao")
public class AnimeAtsumeDaoImpl implements AnimeAtsumeDao {
    @Autowired
    CaptchaAnswerRepository captchaAnswerRepository;

    @Override
    public List<CaptchaAnswer> getAllCaptchaAnswersByPrompt(String prompt) {
        return captchaAnswerRepository.getAllCaptchaAnswersByPrompt(prompt);
    }

    public CaptchaAnswer saveNewCaptchaAnswer(CaptchaAnswer captchaAnswer) {
        return captchaAnswerRepository.save(captchaAnswer);
    }

    public List<CaptchaAnswer> saveNewCaptchaAnswers(List<CaptchaAnswer> captchaAnswers) {
        return captchaAnswerRepository.saveAll(captchaAnswers);
    }
}
