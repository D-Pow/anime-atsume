import { getCorsProxyUrl, getKitsuTitleSearchUrl, kitsuTitleSearchUrl } from '@/services/Urls';

export async function fetchKitsuTitleSearch(searchText) {
    try {
        const response = await fetch(getKitsuTitleSearchUrl(searchText));

        return await response.json();
    } catch (e) {
        console.log('Error in fetching Kitsu results: ' + e);
        console.log('Attempting to get them through CORS proxy.');

        try {
            const response = await fetch(getCorsProxyUrl(kitsuTitleSearchUrl + searchText));

            return await response.json();
        } catch (e2) {
            console.log('Kitsu results are *actually* down, not just a CORS problem:', e);

            return '';
        }
    }
}
