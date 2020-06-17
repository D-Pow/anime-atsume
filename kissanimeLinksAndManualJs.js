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

(async function addVideoToCurrentScreen() {
    const res = await fetch('http://localhost:8080/getNovelPlanetSources', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            "novelPlanetUrl": "https://www.novelplanet.me/v/4dvj42p-yv1"
        })
    }).then(res => res.json());

    const { file } = res.data[0];
    const urlPrefix = 'http://localhost:8080/novelPlanetVideo?url=';

    try {
        document.body.removeChild(document.querySelector('video'));
    } catch(e) {}

    const video = document.createElement('video');
    const source = document.createElement('source')

    video.controls = true
    source.type = 'video/mp4'
    source.src = urlPrefix + file;

    video.appendChild(source);
    document.body.appendChild(video);

    console.log(file);
})()

// https://kissanime.ru/Anime/Shigatsu-wa-Kimi-no-Uso
document.querySelector('form').submit()

// https://kissanime.ru/Anime/Shigatsu-wa-Kimi-no-Uso/Episode-001?id=81025&s=nova