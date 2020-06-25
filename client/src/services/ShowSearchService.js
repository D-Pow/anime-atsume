import { SHOW_SEARCH_URL } from 'services/Urls';

export async function searchForShow(title) {
    return await fetch(SHOW_SEARCH_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            title
        })
    }).then(res => res.json());
}
