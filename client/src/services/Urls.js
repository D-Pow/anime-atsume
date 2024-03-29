export function getSearchUrl(baseUrl, textToEncode) {
    return baseUrl + encodeURIComponent(textToEncode);
}

export const kitsuTitleSearchUrl = 'https://kitsu.io/api/edge/anime?filter[text]=';
export const myAnimeListSearchUrl = 'https://myanimelist.net/anime.php?q=';

export const getKitsuTitleSearchUrl = searchText => getSearchUrl(kitsuTitleSearchUrl, searchText);
export const getMyAnimeListSearchUrl = searchText => getSearchUrl(myAnimeListSearchUrl, searchText);

export const SHOW_SEARCH_URL = '/searchAnime';
export const EPISODE_HOST_SEARCH_URL = '/getVideosForEpisode';
export const VIDEO_BASE_PATH = '/video';
export const IMAGE_BASE_PATH = '/image';
export const CORS_PROXY_URL = '/corsProxy?url=';

export const sanitizeUrlParam = param => param.replaceAll(' ', '-').replace(/[^a-zA-Z0-9-]/g, '');
export const getCorsProxyUrl = url => getSearchUrl(CORS_PROXY_URL, url);
export const getVideoSrcPath = (showName, episodeName, quality, hostUrl) =>
    `${VIDEO_BASE_PATH}/${sanitizeUrlParam(showName)}/${sanitizeUrlParam(episodeName)}/${sanitizeUrlParam(quality)}?url=${encodeURIComponent(hostUrl)}`;
export const getImageSrcPath = imageId => `${IMAGE_BASE_PATH}/${imageId}`;

export function getVideoNameDataFromUrl(kissanimeEpisodeUrl) {
    const [ , , showName, episodeName ] = new URL(kissanimeEpisodeUrl).pathname.split('/');

    return {
        showName,
        episodeName,
    };
}
