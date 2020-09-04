Front-end

Back-end
    * Set request timeout?
    * Try UrlResource with client's range headers (I don't think I forwarded the range header previously, so it'd be worth trying again)
    * Other anime sites (Kissanime was shut down)
        - Twist.moe
        - 4anime.to (they don't put IP in src so <video> could have their video plugged right in)
        - https://www.wcostream.com/ (Watch cartoon online)
        - 9anime.(to|ru|com?)
        - Kissanime.pro
        - Animetribes.ru
        - Animepahe.com
        - Aniwatch.me
    * Movies
        - One option: https://www.reddit.com/r/reactjs/comments/i1sxu4/project_stream_torrent_in_the_browser/?utm_medium=android_app&utm_source=share
    * Add bypass logic for Cloudflare's "One more step" captcha page
    * Duplicate refactors done in other branch on master
        - Include refactor: return video response as `{ url, isVideoSrcNestable }` or related var name.
    * Add sanitization for database insertions since they come from front-end.
    * Decide on best buffer size (1080p is about 16.5 MB/min, 720p is about 9 MB/min) if UrlResource doesn't work
    * Way to download videos
