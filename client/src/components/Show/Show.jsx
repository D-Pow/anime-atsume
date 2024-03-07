import { useEffect, useRef, useState } from 'react';

import { useParams } from 'react-router';

import { fetchKitsuTitleSearch } from '@/services/KitsuAnimeSearchService';
import { searchForShow } from '@/services/ShowSearchService';
import { getMyAnimeListSearchUrl } from '@/services/Urls';
import { useStorage } from '@/utils/Hooks';
import { debounce } from '@/utils/Events';
import { isSafariBrowser } from '@/utils/BrowserIdentification';
import { asNumber } from '@/utils/Numbers';
import Spinner from '@/components/ui/Spinner';
import VideoModal from '@/components/VideoModal';
import Anchor from '@/components/ui/Anchor';
import ErrorDisplay from '@/components/ui/ErrorDisplay';


function Show() {
    const routeParams = useParams();
    const title = decodeURIComponent(routeParams.title);
    const watchSectionScrollListsMaxHeight = '400px';

    const [ hasError, setHasError ] = useState(false);
    const [ kitsuResult, setKitsuResult ] = useState(null);
    const [ episodeResults, setEpisodeResults ] = useState(null);
    const [ selectedTab, setSelectedTab ] = useState(0);
    const [ selectedShow, setSelectedShow ] = useState(null);
    const [ selectedEpisode, setSelectedEpisode ] = useState(null);
    const [ showsProgress, setShowsProgress ] = useStorage('showsProgress', { initialValue: {}});
    const episodesTitleRef = useRef();

    async function fetchKitsuInfo() {
        const response = await fetchKitsuTitleSearch(title.toLowerCase());
        const allKitsuResults = response.data;
        let showInfo = allKitsuResults.find(show => show.attributes.canonicalTitle === title);

        if (allKitsuResults && !showInfo) {
            // Could not find searched show in kitsuResults, likely
            // from the user manually typing a show name in the URL bar.
            // Default to first kitsuResult entry
            showInfo = allKitsuResults[0];
        }

        setKitsuResult(showInfo);
    }

    async function fetchShowAndEpisodesList() {
        try {
            const episodeResults = await searchForShow(title);

            if (episodeResults.status && episodeResults.status > 299) {
                throw `Got HTTP status code ${episodeResults.status} from server. Error: ${episodeResults.error}.`;
            }

            if (episodeResults.results != null && episodeResults.results.length >= 0) {
                const sortEpisodesByTitleInAscendingOrder = (show1, show2) => asNumber(show1.title) - asNumber(show2.title);

                episodeResults.results.forEach(show => {
                    show.episodes.sort(sortEpisodesByTitleInAscendingOrder);
                });
            }

            setEpisodeResults(episodeResults);
        } catch (e) {
            console.error('Error fetching for show matches:', e);
            setHasError(true);
        }
    }

    useEffect(() => {
        fetchKitsuInfo();
        fetchShowAndEpisodesList();
    }, []);

    const getShowTitle = (showIndex = selectedShow) => (episodeResults && Number.isInteger(showIndex))
        ? episodeResults.results[showIndex].title
        : '';
    const getAllShowsProgressForShowMatches = () => (
        episodeResults.results
            ? episodeResults.results
                .map(({ title }, i) => ({
                    episodeTitle: showsProgress[title],
                    showTitle: title,
                    showIndex: i,
                }))
                .filter(showProgress => showProgress.episodeTitle != null)
            : []
    );
    const getIdForSelectableElement = (showIndex, title) => `${showIndex}-${title}`;

    function handleVideoLoad() {
        // update show progress in window storage
        const showTitle = getShowTitle();
        const { episodeTitle } = selectedEpisode;

        setShowsProgress(prevState => {
            const prevProgress = { ...prevState };
            prevProgress[showTitle] = episodeTitle;
            return prevProgress;
        });
    }

    const scrollEpisodeIntoView = debounce(elementId => {
        /*
         * Cannot use React refs here because the list of episode
         * links are dynamically updated and it's possible the show
         * hasn't been selected yet. As such, it's possible the anchor
         * element that the ref should be attached to hasn't been
         * mounted yet and will be null.
         */
        const element = document.getElementById(elementId);
        element.scrollIntoView({ block: 'center', inline: 'center' });
    }, 500);

    const scrollShowIntoView = debounce(elementId => {
        /*
         * Chrome cannot handle multiple scrollIntoView() calls at once
         * and will cancel the previous calls when a new call is made.
         * Thus, manually set the scroll amount to immediately scroll
         * to the show, and give the smooth scroll animation to the
         * episode scroll function.
         * Requires `scroll-behavior: auto`.
         */
        const element = document.getElementById(elementId);
        element.parentElement.scrollTop = element.offsetTop - element.offsetHeight;
    }, 500);

    function handleLastWatchedEpisodeClick(showIndex, episodeTitle) {
        const showElementId = getIdForSelectableElement(showIndex, getShowTitle(showIndex));
        const episodeElementId = getIdForSelectableElement(showIndex, episodeTitle);

        setSelectedShow(showIndex);
        scrollShowIntoView(showElementId);
        scrollEpisodeIntoView(episodeElementId);
    }

    const renderEpisodesForSelectedShow = () => {
        if (selectedShow == null) {
            return;
        }

        return episodeResults.results[selectedShow].episodes.map(({ title: episodeTitle, url: episodeUrl }, i) => {
            const isLastEpisodeWatched = showsProgress[getShowTitle()] === episodeTitle;

            return (
                <a
                    className={`list-group-item cursor-pointer ${isLastEpisodeWatched ? 'active' : 'text-primary'}`}
                    id={getIdForSelectableElement(selectedShow, episodeTitle)}
                    key={i}
                    onClick={() => setSelectedEpisode({ episodeTitle, episodeUrl })}
                >
                    {episodeTitle}
                </a>
            );
        });
    };

    const renderPossibleShowMatches = () => {
        return episodeResults.results.map(({ title: showTitle, episodes: showEpisodes }, i) => {
            const renderedEpisodeCountBadge = (
                <h4>
                    <span className={`ml-1 badge badge-pill badge-${selectedShow === i ? 'dark' : 'primary'}`}>
                        {showEpisodes.length}
                    </span>
                </h4>
            );
            const Button = isSafariBrowser() ? 'div' : 'button';

            return (
                <Button
                    className={`btn list-group-item remove-focus-highlight ${selectedShow === i ? 'active' : ''}`}
                    id={getIdForSelectableElement(i, showTitle)}
                    key={i}
                    onClick={() => {
                        setSelectedShow(i);

                        if (episodesTitleRef.current) {
                            episodesTitleRef.current.scrollIntoView({ block: 'start', inline: 'center' });
                        }
                    }}
                >
                    <div className={'d-flex justify-content-between align-items-center'}>
                        <h5 className={'mb-2 d-flex d-sm-none'}>
                            {showTitle}
                        </h5>
                        <h3 className={'mb-2 d-none d-sm-flex'}>
                            {showTitle}
                        </h3>
                        {renderedEpisodeCountBadge}
                    </div>
                </Button>
            );
        });
    };

    if (hasError) {
        return <ErrorDisplay fullScreen={true} show={hasError} />;
    }

    if (!kitsuResult || !episodeResults) {
        return (
            <Spinner fullScreen={true} show={true} />
        );
    }

    const renderLastWatchedEpisodes = () => {
        if (!episodeResults || selectedTab !== 1) {
            return null;
        }

        const renderedLastWatchedEpisodes = getAllShowsProgressForShowMatches()
            .map(({ showTitle, showIndex, episodeTitle }) => (
                <div className={'row mb-1'} key={showIndex}>
                    <div className={'col-12'}>
                        <span className={'h5'}>
                            <span className={'underline'}>{showTitle}</span>
                            :
                        </span>
                        <button
                            className={'btn btn-link remove-focus-highlight border-0'}
                            onClick={() => handleLastWatchedEpisodeClick(showIndex, episodeTitle)}
                        >
                            <h5 className={'m-0'}>{episodeTitle}</h5>
                        </button>
                    </div>
                </div>
            ));

        if (renderedLastWatchedEpisodes.length) {
            return (
                <div className={'row pb-2'}>
                    <div className={'col-12'}>
                        <h4 className={'mb-2'}>
                            Last watched episodes:
                        </h4>
                        {renderedLastWatchedEpisodes}
                    </div>
                </div>
            );
        }
    };

    const {
        canonicalTitle,
        synopsis,
        episodeCount,
        showType,
        posterImage: {
            small,
        },
    } = kitsuResult.attributes;

    const renderOverviewTab = () => (
        <div className={'row'}>
            <div className={'col-centered col-lg-4 my-3 d-flex'}>
                <img
                    className={'my-auto flex-center'}
                    src={small}
                    alt={canonicalTitle}
                    style={{ maxWidth: '95%' }}
                />
            </div>
            <div className={'col-sm-12 col-lg-8 d-flex justify-content-center'}>
                <div className={'text-center'}>
                    <div className={'row mb-3'}>
                        <div className={'col'}>
                            <h5 className={'capitalize-first'}>
                                {episodeCount === 1
                                    ? showType
                                    : episodeCount + ' episodes'
                                }
                            </h5>
                        </div>
                    </div>
                    <div className={'row mb-3'}>
                        <div className={'col'}>
                            <p>
                                {synopsis}
                            </p>
                        </div>
                    </div>
                    <div className={'row mb-3'}>
                        <div className={'col'}>
                            <Anchor href={getMyAnimeListSearchUrl(title)}>
                                <h5>View on MyAnimeList</h5>
                            </Anchor>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );

    const renderWatchTab = () => {
        if (episodeResults.error) {
            <div className={'row d-flex justify-content-center'}>
                <h4>{episodeResults.error}</h4>
            </div>;
        }

        if (episodeResults.results == null || episodeResults.results.length === 0) {
            return (
                <div className={'row d-flex justify-content-center'}>
                    <h4>Sorry, no episodes were found for this show.</h4>
                </div>
            );
        }

        const scrollDivBorderCls = 'border-top border-bottom';

        const renderedShowMatches = renderPossibleShowMatches();
        const renderedEpisodeMatches = renderEpisodesForSelectedShow();

        const showBorderCls = (renderedShowMatches && renderedShowMatches.length) ? scrollDivBorderCls : '';
        const episodeBorderCls = (renderedEpisodeMatches && renderedEpisodeMatches.length) ? scrollDivBorderCls : '';

        return (
            <div className={'row'}>
                <div className={'col-sm-12 col-md-6 mb-5'}>
                    <div>
                        <h3 className={'mb-2 d-none d-sm-inline-block'}>Shows</h3>
                        <h4 className={'mb-2 d-inline-block d-sm-none'}>Shows</h4>
                        <h4 className={'d-inline-block ml-1'}>(# episodes)</h4>
                    </div>
                    <div
                        className={`text-left list-group overflow-auto ${showBorderCls} fix-strange-z-index-scrollbars scroll-auto`}
                        style={{ maxHeight: watchSectionScrollListsMaxHeight }}
                    >
                        {renderedShowMatches}
                    </div>
                </div>
                <div className={'col-sm-12 col-md-6'}>
                    <div>
                        <h3 className={'mb-2 d-none d-sm-block'}>Episodes</h3>
                        <h4 className={'mb-2 d-block d-sm-none'} ref={episodesTitleRef}>Episodes</h4>
                    </div>
                    <div
                        className={`text-left list-group overflow-auto ${episodeBorderCls} fix-strange-z-index-scrollbars`}
                        style={{ maxHeight: watchSectionScrollListsMaxHeight }}
                    >
                        {renderedEpisodeMatches}
                    </div>
                </div>
            </div>
        );
    };

    const tabs = [
        {
            tabTitle: 'Overview',
            content: renderOverviewTab(),
        },
        {
            tabTitle: 'Watch',
            content: renderWatchTab(),
        },
    ];

    const renderedTabNavigation = (
        <nav>
            <ul className={'pagination'}>
                {tabs.map(({ tabTitle }, i) => (
                    <li
                        className={`page-item ${selectedTab === i ? 'active' : ''}`}
                        key={i}
                        onClick={() => setSelectedTab(i)}
                        style={{ width: `${100 / tabs.length}%` }}
                    >
                        <a className={'page-link cursor-pointer'}>
                            {tabTitle}
                        </a>
                    </li>
                ))}
            </ul>
        </nav>
    );

    return (
        <div className={'col-12'}>
            <div className={'row pb-4'}>
                <h1 className={'text-center mx-auto mt-5'}>
                    {title}
                </h1>
            </div>

            {renderLastWatchedEpisodes()}

            <div className={'row pt-5 px-2'}>
                <div className={'col-12'}>
                    <div className={'card mb-5'}>
                        {renderedTabNavigation}

                        <div className={'card-body'}>
                            {tabs[selectedTab].content}
                        </div>
                    </div>
                </div>
            </div>

            <VideoModal
                {...selectedEpisode}
                show={selectedEpisode != null}
                showTitle={getShowTitle()}
                onClose={() => setSelectedEpisode(null)}
                videoElementProps={{ onLoadStart: handleVideoLoad }}
            />
        </div>
    );
}

Show.propTypes = {};

Show.defaultProps = {};

export default Show;
