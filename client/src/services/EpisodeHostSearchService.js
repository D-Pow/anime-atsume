import { EPISODE_HOST_SEARCH_URL } from '@/services/Urls';

/**
 * @typedef {Object} KissanimeEpisodeHostResponse
 * @property {String} videoHostUrl - URL of original website with nested video.
 * @property {Object} captchaContent - Data associated with captcha returned by original website.
 * @property {String[]} captchaContent.promptTexts - Prompt texts for solving captcha.
 * @property {CaptchaAttempt[]} captchaContent.imgIdsAndSrcs - Form ID, image hash, and image src filename.
 */

/**
 * @typedef {Object} CaptchaAttempt
 * @property {String} formId - ID (usually equal to index) of image in captcha list.
 * @property {String} imageId - Image src filename.
 * @property {String} imageHash - Image hash string.
 * @property {String} promptText - The prompt text this image corresponds to.
 */

/**
 * @typedef {Object} VideoResult
 * @property {string} url - URL of video source (may or may not need to be proxied).
 * @property {string} title - Quality of video (e.g. '1080p').
 * @property {boolean} directSource - If the URL can be plugged into video[src] or if it needs to be proxied.
 */

/**
 * @typedef {Object} EpisodeVideoResults
 * @property {String} url - URL of website that hosts the video originally (either direct or middle-man).
 * @property {VideoResult[]} episodes - Videos offered by the host website.
 */

/**
 * Searches for an episode host, bypassing captcha if required.
 *
 * @param {String} episodeUrl - Episode URL returned from {@link searchForShow.js}.
 * @param {CaptchaAttempt[]} captchaAnswers - List of answers used to attempt to bypass captcha.
 * @returns {Promise<(KissanimeEpisodeHostResponse|EpisodeVideoResults)>} - Data for either solving a captcha or the resolved video info.
 */
export async function searchForEpisodeHost(episodeUrl, captchaAnswers = null) {
    return await fetch(EPISODE_HOST_SEARCH_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            episodeUrl,
            captchaAnswers,
        }),
    }).then(res => res.json());
}
