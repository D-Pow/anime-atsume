# Front-end
* Spinner too large on Firefox
* VideoModal
    - Show 'use modern browser' in `<ErrorDisplay>`
    - Add videoHostUrl in `<ErrorDisplay>`

# Back-end
* Update `/video?url=X` to include `?directSource=(true|false)` to determine if CORS proxy is necessary
* Set request timeout?
* Try UrlResource with client's range headers (I don't think I forwarded the range header previously, so it'd be worth trying again)
* Other anime sites (Kissanime was shut down)
    - Twist.moe
    - 4anime.to (they don't put IP in src so `<video>` could have their video plugged right in)
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
* Duplicate refactors done in other branch on master
    - Include refactor: return video response as `{ url, isVideoSrcNestable }` or related var name.
* Add sanitization for database insertions since they come from front-end.
* Decide on best buffer size (1080p is about 16.5 MB/min, 720p is about 9 MB/min) if UrlResource doesn't work
* Way to download videos
* Maybe make `dao.model.CaptchaAnswer` primary key more like this format:
    ```java
    public class MyTable {
        // ...
        @Data
        @Embeddable
        public static class MyTablePk implements Serializable {
            @SequenceGenerator(
                name="seq_gen_name_in_java",
                sequenceName="sequence_name_in_db",
                allocationSize=1
            )
            @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq_gen_name_in_java")
            @Column(name="id", nullable=false)
            private int id;

            @Column(name="version", nullable=false)
            private int version;
        }

        @EmbeddedId
        private MyTablePk myTablePk;
    ```
