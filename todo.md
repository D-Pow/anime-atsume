# Front-end

* Spinner too large on Firefox
* VideoModal
    - Show 'use modern browser' in `<ErrorDisplay>`
    - Full-screen doesn't work in Safari. Requires a [special function](https://developer.mozilla.org/en-US/docs/Web/API/Fullscreen_API/Guide)

# Back-end

* Set request timeout?
* Try UrlResource with client's range headers (I don't think I forwarded the range header previously, so it'd be worth trying again)
* Other anime sites (Kissanime was shut down)
    - [Reddit r/animepiracy list](https://www.reddit.com/r/animepiracy/comments/ky7nz0/the_ranimepiracy_index_is_finally_in_beta/)
        + List of accessible sites: https://piracy.moe/
        + **Winner**: KimAnime.com
            * e.g. https://kimanime.com/episode/76557/relife-episode-001 - both `source` tags work cross-domain
    - Twist.moe
    - https://www.wcostream.com/ (Watch cartoon online)
    - 9anime.(to|ru|com?)
    - Kissanime.nz
    - Kissanime.pro
    - Animetribes.ru
    - Animepahe.com
    - Aniwatch.me
* Movies
    - One option: https://www.reddit.com/r/reactjs/comments/i1sxu4/project_stream_torrent_in_the_browser/?utm_medium=android_app&utm_source=share
* Add bypass logic for Cloudflare's "One more step" captcha page
* Add sanitization for database insertions since they come from front-end.
* Decide on best buffer size (1080p is about 16.5 MB/min, 720p is about 9 MB/min) if UrlResource doesn't work
* Way to download videos
