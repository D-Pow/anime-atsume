export function getSearchUrl(baseUrl, textToEncode) {
    return baseUrl + encodeURIComponent(textToEncode);
}

export const kitsuTitleSearchUrl = 'https://kitsu.io/api/edge/anime?filter[text]=';
export const myAnimeListSearchUrl = 'https://myanimelist.net/anime.php?q=';

export const getKitsuTitleSearchUrl = searchText => getSearchUrl(kitsuTitleSearchUrl, searchText);

export const getMyAnimeListSearchUrl = searchText => getSearchUrl(myAnimeListSearchUrl, searchText);
