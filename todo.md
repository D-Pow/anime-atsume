# Front-end

* Spinner too large on Firefox
* VideoModal
    - Show 'use modern browser' in `<ErrorDisplay>`
    - Full-screen doesn't work in Safari. Requires a [special function](https://developer.mozilla.org/en-US/docs/Web/API/Fullscreen_API/Guide)

# Back-end

* Convert `directSource` to use a test request from the back-end to determine if the video host allows cross-origin requests or not rather than using an origin string check
* Set request timeout?
* Try UrlResource with client's range headers (I don't think I forwarded the range header previously, so it'd be worth trying again)
* Other anime sites (Kissanime was shut down)
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
