Front-end
    * Add link to GitHub repo (via icon, like in personal website)
    * Scroll down to episodes on show click on mobile

Back-end
    * Try UrlResource with client's range headers (I don't think I forwarded the range header previously, so it'd be worth trying again)
    * 4anime.to
        - If UrlResource doesn't work, then try this before kissanime b/c they don't put IP in src so <video> could have their video plugged right in
    * Add bypass logic for Cloudflare's "One more step" captcha page
    * Duplicate refactors done in other branch on master
    * Decide on best buffer size (1080p is about 16.5 MB/min, 720p is about 9 MB/min) if UrlResource doesn't work
    * Way to download videos