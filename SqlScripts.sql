CREATE TABLE IF NOT EXISTS captcha_answers (
    prompt VARCHAR(32) NOT NULL,
    image_hash VARCHAR(200) NOT NULL,
    PRIMARY KEY (prompt, image_hash)
);
