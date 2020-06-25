import { EPISODE_HOST_SEARCH_URL } from 'services/Urls';

/**
 * @typedef {Object} EpisodeHostResponse
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
 * @typedef {Object} NovelPlanetSource
 * @property {String} websiteUrl - URL of website that hosts the video originally (as a middle-man).
 * @property {Object} data - Data of the actual video files.
 * @property {String} data.file - URL of video.
 * @property {String} data.label - Video quality of video.
 * @property {String} data.type - Mime type of video.
 */

/**
 * Searches for an episode host, bypassing captcha if required.
 *
 * @param {String} episodeUrl - Episode URL returned from {@link searchForShow.js}.
 * @param {CaptchaAttempt} captchaAnswers - List of answers used to attempt to bypass captcha.
 * @returns {Promise<(EpisodeHostResponse|NovelPlanetSource)>} - Data for either solving a captcha or the resolved video info.
 */
export async function searchForEpisodeHost(episodeUrl, captchaAnswers = null) {
    return await fetch(EPISODE_HOST_SEARCH_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            episodeUrl,
            captchaAnswers
        })
    }).then(res => res.json());
}
