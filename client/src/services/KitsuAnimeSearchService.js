import { getKitsuTitleSearchUrl } from 'services/Urls';

export async function fetchKitsuTitleSearch(searchText) {
    try {
        const response = await fetch(getKitsuTitleSearchUrl(searchText));

        return await response.json();
    } catch (e) {
        console.log('Error in fetching Kitsu results: ' + e);

        throw {};
    }
}
