import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { fetchKitsuTitleSearch } from 'services/KitsuAnimeSearchService';
import { searchForShow } from 'services/ShowSearchService';
import { getMyAnimeListSearchUrl } from 'services/Urls';
import { useStorage } from 'utils/Hooks';
import Spinner from 'components/ui/Spinner';
import VideoModal from 'components/VideoModal';
import Anchor from 'components/ui/Anchor';

function Show(props) {
    const title = decodeURIComponent(props.title);

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
        const episodeResults = await searchForShow(title);

        episodeResults.results.forEach(show => {
            show.episodes.reverse();
        });

        setEpisodeResults(episodeResults);
    }

    useEffect(() => {
        fetchKitsuInfo();
        fetchShowAndEpisodesList();
    }, []);

    function handleVideoLoad() {
        // update show progress in window storage
        const showTitle = episodeResults.results[selectedShow].title;
        const { episodeTitle } = selectedEpisode;

        setShowsProgress(prevState => {
            const prevProgress = {...prevState};
            prevProgress[showTitle] = episodeTitle;
            return prevProgress;
        });
    }

    const renderEpisodesForSelectedShow = ({ title: episodeTitle, url: episodeUrl }, i) => (
        <a
            className={'list-group-item text-primary cursor-pointer'}
            key={i}
            onClick={() => setSelectedEpisode({ episodeTitle, episodeUrl })}
        >
            {episodeTitle}
        </a>
    );

    const renderPossibleShowMatches = ({ title: showTitle, episodes: showEpisodes}, i) => {
        const renderedEpisodeCountBadge = (
            <h4>
                <span className={`ml-1 d-xs-none badge badge-pill badge-${selectedShow === i ? 'dark' : 'primary'}`}>
                    {showEpisodes.length}
                </span>
            </h4>
        );

        return (
            <button
                className={`list-group-item remove-focus-highlight ${selectedShow === i ? 'active' : ''}`}
                key={i}
                onClick={() => setSelectedShow(i)}
            >
                <div className={'d-sm-none d-xs-flex justify-content-between align-items-center'}>
                    <h5 className={'mb-2'}>
                        {showTitle}
                    </h5>
                    {renderedEpisodeCountBadge}
                </div>
                <div className={'d-xs-none d-sm-flex justify-content-between align-items-center'}>
                    <h3 className={'mb-2'}>
                        {showTitle}
                    </h3>
                    {renderedEpisodeCountBadge}
                </div>
            </button>
        );
    };

    if (!kitsuResult || !episodeResults) {
        return (
            <Spinner fullScreen={true} show={true} />
        );
    }

    const renderLastWatchedEpisodeText = () => {
        if (!episodeResults || selectedShow == null) {
            return null;
        }

        const showTitle = episodeResults.results[selectedShow].title;
        const lastWatchedEpisode = showsProgress[showTitle];

        if (lastWatchedEpisode) {
            return (
                <div className={'row mb-3'}>
                    <div className={'col-12'}>
                        <h5 className={'mb-1'}>Last watched: </h5>
                        <h5 className={'underline'}>{lastWatchedEpisode}</h5>
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

    const tabs = [
        {
            tabTitle: 'Overview',
            content: (
                <div className={'row'}>
                    <div className={'col-centered col-lg-4 my-3 d-flex'}>
                        <img
                            className={'my-auto flex-center'}
                            src={small}
                            alt={canonicalTitle}
                            style={{ maxWidth: '95%' }}
                        />
                    </div>
                    <div className={'col-sm-12 col-lg-8 d-flex'}>
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
            )
        },
        {
            tabTitle: 'Watch',
            content: (
                <div className={'row'}>
                    <div className={'col-6'}>
                        <div className={'d-xs-block d-sm-none'}>
                            <h4 className={'mb-2'}>Shows</h4>
                        </div>
                        <div className={'d-xs-none d-sm-block'}>
                            <h3 className={'mb-2 d-inline-block'}>Shows</h3>
                            <h4 className={'d-inline-block ml-1'}>(# episodes)</h4>
                        </div>
                        <div className={'text-left list-group overflow-auto'} style={{ maxHeight: '400px' }}>
                            {episodeResults.results.map(renderPossibleShowMatches)}
                        </div>
                    </div>
                    <div className={'col-6'}>
                        <div className={'d-xs-block d-sm-none'}>
                            <h4 className={'mb-2'}>Episodes</h4>
                        </div>
                        <div className={'d-xs-none d-sm-block'}>
                            <h3 className={'mb-2'}>Episodes</h3>
                        </div>
                        {renderLastWatchedEpisodeText()}
                        <div className={'text-left list-group overflow-auto'} style={{ maxHeight: '400px' }}>
                            {selectedShow != null && episodeResults.results[selectedShow].episodes.map(renderEpisodesForSelectedShow)}
                        </div>
                    </div>
                </div>
            )
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
            <div className={'row pb-3'}>
                <h1 className={'text-center mx-auto mt-5'}>
                    {title}
                </h1>
            </div>

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
                onVideoLoad={handleVideoLoad}
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
