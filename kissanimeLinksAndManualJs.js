function getAnonymousFunctionFromSetTimeout(jsString) {
    return jsString.match(/(?<=setTimeout\()[\s\S]+\}(?=, ?\d{4}\))/)[0];
}

function evalCloudflareDdosValidateFunc() {
    const setTimeoutFunc = getAnonymousFunctionFromSetTimeout(document.head.innerHTML);
    eval(`(${setTimeoutFunc})()`);
}

evalCloudflareDdosValidateFunc()

// Search function; returns a bunch of <a/> tags
async function searchKissanime(keyword, type = 'Anime') {
    const formDataBody = new URLSearchParams();

    formDataBody.append('type', type);
    formDataBody.append('keyword', keyword);

    const resultAnchors = await fetch('https://kissanime.ru/Search/SearchSuggestx', {
        method: 'POST',
        body: formDataBody
    }).then(res => res.text());

    const domParser = new DOMParser();
    const resultDom = domParser.parseFromString(resultAnchors, 'text/html');
    const resultData = [...resultDom.querySelectorAll('a')].map(aElem => ({
        url: aElem.href,
        title: aElem.textContent
    }));

    console.log(resultData);

    // cookie: _ga=GA1.2.1421808091.1562539474; __atuvc=0%7C13%2C0%7C14%2C0%7C15%2C0%7C16%2C10%7C17
    // Note that 'your lie' and 'shigatsu wa kimi no uso' both return the same URL but different titles
}

function getCookie(cookie = document.cookie) {
    return cookie.split('; ').reduce((cookieObj, entry) => {
        const keyVal = entry.split('=');
        const key = keyVal[0];
        const value = keyVal.slice(1).join('=');

        cookieObj[key] = value;

        return cookieObj;
    }, {});
}



async function searchTitle(title) {
    const searchResults = await fetch('/searchKissanime', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            title
        })
    }).then(res => res.json());

    console.log(searchResults);
}

async function getEpisodeHost(episodeUrl, captchaAnswer = null) {
    document.body.innerHTML = '';

    const { videoHostUrl, captchaContent, data } = await fetch('/getVideosForEpisode', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            episodeUrl,
            captchaAnswer
        })
    }).then(res => res.json());

    if (captchaContent) {
        captchaContent.promptTexts.forEach(prompt => {
            const h1 = document.createElement('h1');
            h1.innerText = prompt;
            document.body.appendChild(h1);
        })

        captchaContent.imgIdsAndSrcs.forEach(({ url, title }) => {
            const img = document.createElement('img');
            img.src = `/image/${url}`;

            const h3 = document.createElement('h3');
            h3.innerText = title;

            const div = document.createElement('div');
            div.appendChild(h3);
            div.appendChild(img);
            document.body.appendChild(div);
        });
    } else if (videoHostUrl) {
        const h1 = document.createElement('h1');
        h1.innerText = videoHostUrl;
        document.body.appendChild(h1);
    } else if (data) {
        const [ blank, animeText, showName, episodeName ] = new URL(episodeUrl).pathname.split('/');
        const { file: novelPlanetSrcUrl, label: videoQuality } = data[0];
        addVideoFromHostUrlToScreen(showName, episodeName, videoQuality, novelPlanetSrcUrl);
    }
}

async function getNovelPlanetSources(novelPlanetUrl) {
    return await fetch('/getNovelPlanetSources', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ novelPlanetUrl })
    }).then(res => res.json());
}

function addVideoFromHostUrlToScreen(showName, episodeName, quality, novelPlanetUrl) {
    const srcUrl = `/novelPlanetVideo?show=${showName}&episode=${episodeName}&quality=${quality}&url=${novelPlanetUrl}`;

    try {
        document.body.removeChild(document.querySelector('video'));
    } catch(e) {}

    const video = document.createElement('video');
    const source = document.createElement('source')

    video.controls = true
    source.type = 'video/mp4'
    source.src = srcUrl;

    video.appendChild(source);
    document.body.appendChild(video);
}

// https://kissanime.ru/Anime/Shigatsu-wa-Kimi-no-Uso
document.querySelector('form').submit()

// https://kissanime.ru/Anime/Shigatsu-wa-Kimi-no-Uso/Episode-001?id=81025&s=nova
