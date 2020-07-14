import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { fetchKitsuTitleSearch } from 'services/KitsuAnimeSearchService';
import { searchForShow } from 'services/ShowSearchService';
import { getMyAnimeListSearchUrl } from 'services/Urls';
import { useStorage } from 'utils/Hooks';
import { debounce } from 'utils/Events';
import Spinner from 'components/ui/Spinner';
import VideoModal from 'components/VideoModal';
import Anchor from 'components/ui/Anchor';
import ErrorDisplay from 'components/ui/ErrorDisplay';

function Show(props) {
    const title = decodeURIComponent(props.title);
    const watchSectionScrollListsMaxHeight = '400px';

    const [ hasError, setHasError ] = useState(false);
    const [ kitsuResult, setKitsuResult ] = useState(null);
    const [ episodeResults, setEpisodeResults ] = useState(null);
    const [ selectedTab, setSelectedTab ] = useState(0);
    const [ selectedShow, setSelectedShow ] = useState(null);
    const [ selectedEpisode, setSelectedEpisode ] = useState(null);
    const [ showsProgress, setShowsProgress ] = useStorage('showsProgress', { initialValue: {} });

    async function fetchKitsuInfo() {
        const response = await fetchKitsuTitleSearch(title.toLowerCase());
        const allKitsuResults = response.data;
        const showInfo = allKitsuResults.find(show => show.attributes.canonicalTitle === title);

        setKitsuResult(showInfo);
    }

    async function fetchShowAndEpisodesList() {
        try {
            const episodeResults = await searchForShow(title);

            if (episodeResults.status && episodeResults.status > 299) {
                throw `Got HTTP status code ${episodeResults.status} from server. Error: ${episodeResults.error}.`;
            }

            if (episodeResults.results != null && episodeResults.results.length >= 0) {
                episodeResults.results.forEach(show => {
                    show.episodes.reverse();
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

    const getShowTitle = (showIndex = selectedShow) => episodeResults.results[showIndex].title;
    const getAllShowsProgressForShowMatches = () => (
        episodeResults.results
            ? episodeResults.results
                .map(({ title }, i) => ({
                    episodeTitle: showsProgress[title],
                    showTitle: title,
                    showIndex: i
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
            const prevProgress = {...prevState};
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
        element.parentElement.scrollTop = element.offsetTop - element.offsetHeight
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
        return episodeResults.results.map(({ title: showTitle, episodes: showEpisodes}, i) => {
            const renderedEpisodeCountBadge = (
                <h4>
                    <span className={`ml-1 d-none d-sm-block badge badge-pill badge-${selectedShow === i ? 'dark' : 'primary'}`}>
                        {showEpisodes.length}
                    </span>
                </h4>
            );

            return (
                <button
                    className={`list-group-item remove-focus-highlight ${selectedShow === i ? 'active' : ''}`}
                    id={getIdForSelectableElement(i, showTitle)}
                    key={i}
                    onClick={() => setSelectedShow(i)}
                >
                    <div className={'d-flex d-sm-none justify-content-between align-items-center'}>
                        <h5 className={'mb-2'}>
                            {showTitle}
                        </h5>
                        {renderedEpisodeCountBadge}
                    </div>
                    <div className={'d-none d-sm-flex justify-content-between align-items-center'}>
                        <h3 className={'mb-2'}>
                            {showTitle}
                        </h3>
                        {renderedEpisodeCountBadge}
                    </div>
                </button>
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
            small
        }
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
        if (episodeResults.results == null || episodeResults.results.length === 0) {
            return (
                <div className={'row d-flex justify-content-center'}>
                    <h4>Sorry, no episodes were found for this show.</h4>
                </div>
            );
        }

        return (
            <div className={'row'}>
                <div className={'col-6'}>
                    <div className={'d-block d-sm-none'}>
                        <h4 className={'mb-2'}>Shows</h4>
                    </div>
                    <div className={'d-none d-sm-block'}>
                        <h3 className={'mb-2 d-inline-block'}>Shows</h3>
                        <h4 className={'d-inline-block ml-1'}>(# episodes)</h4>
                    </div>
                    <div
                        className={'text-left list-group overflow-auto fix-strange-z-index-scrollbars scroll-auto'}
                        style={{ maxHeight: watchSectionScrollListsMaxHeight }}
                    >
                        {renderPossibleShowMatches()}
                    </div>
                </div>
                <div className={'col-6'}>
                    <div className={'d-block d-sm-none'}>
                        <h4 className={'mb-2'}>Episodes</h4>
                    </div>
                    <div className={'d-none d-sm-block'}>
                        <h3 className={'mb-2'}>Episodes</h3>
                    </div>
                    <div
                        className={'text-left list-group overflow-auto fix-strange-z-index-scrollbars'}
                        style={{ maxHeight: watchSectionScrollListsMaxHeight }}
                    >
                        {renderEpisodesForSelectedShow()}
                    </div>
                </div>
            </div>
        );
    };

    const tabs = [
        {
            tabTitle: 'Overview',
            content: renderOverviewTab()
        },
        {
            tabTitle: 'Watch',
            content: renderWatchTab()
        }
    ];

    const renderedTabNavigation = (
        <nav>
            <ul className={'pagination'}>
                {tabs.map(({ tabTitle }, i) => (
                    <li
                        className={`page-item ${selectedTab === i ? 'active' : ''}`}
                        key={i}
                        onClick={() => setSelectedTab(i)}
                        style={{ width: `${100 / tabs.length}%`}}
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
        <React.Fragment>
            <div className={'row pb-4'}>
                <h1 className={'text-center mx-auto mt-5'}>
                    {title}
                </h1>
            </div>

            {renderLastWatchedEpisodes()}

            <div className={'row pt-5'}>
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
                onClose={() => setSelectedEpisode(null)}
                videoElementProps={{ onLoadStart: handleVideoLoad }}
            />
        </React.Fragment>
    );
}

Show.propTypes = {
    title: PropTypes.string
};

Show.defaultProps = {
    title: ''
};

export default Show;
