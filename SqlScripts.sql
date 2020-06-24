CREATE TABLE IF NOT EXISTS captcha_answers (
    prompt VARCHAR(32) NOT NULL,
    image_id VARCHAR(200) NOT NULL,
    PRIMARY KEY (prompt, image_id)
);
