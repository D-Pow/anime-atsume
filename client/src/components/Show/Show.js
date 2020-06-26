import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { fetchKitsuTitleSearch } from 'services/KitsuAnimeSearchService';
import { searchForShow } from 'services/ShowSearchService';
import { getMyAnimeListSearchUrl } from 'services/Urls';
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

        setSelectedShow(0);
        setEpisodeResults(episodeResults);
    }

    useEffect(() => {
        fetchKitsuInfo();
        fetchShowAndEpisodesList();
    }, []);

    const renderEpisodesForSelectedShow = ({ title: episodeTitle, url: episodeUrl }, i) => (
        <a
            className={'list-group-item text-primary cursor-pointer'}
            key={i}
            onClick={() => setSelectedEpisode({ episodeTitle, episodeUrl })}
        >
            {episodeTitle}
        </a>
    );

    const renderPossibleShowMatches = ({ title: showTitle, episodes: showEpisodes}, i) => (
        <button
            className={`list-group-item remove-focus-highlight ${selectedShow === i ? 'active' : ''}`}
            key={i}
            onClick={() => setSelectedShow(i)}
        >
            <h4 className={'d-flex justify-content-between align-items-center'}>
                {showTitle}
                <span className={`badge badge-pill badge-${selectedShow === i ? 'dark' : 'primary'}`}>
                    {showEpisodes.length}
                </span>
            </h4>
        </button>
    );

    if (!kitsuResult || !episodeResults) {
        return (
            <Spinner fullScreen={true} show={true} />
        );
    }

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
            )
        },
        {
            tabTitle: 'Possible Show/Episode Matches',
            content: (
                <div className={'row'}>
                    <div className={'col-6 overflow-auto'} style={{ maxHeight: '400px' }}>
                        <h3 className={'mb-2'}>Shows</h3>
                        <div className={'text-left list-group'}>
                            {episodeResults.results.map(renderPossibleShowMatches)}
                        </div>
                    </div>
                    <div className={'col-6 overflow-auto'} style={{ maxHeight: '400px' }}>
                        <h3 className={'mb-2'}>Episodes</h3>
                        <div className={'text-left list-group'}>
                            {episodeResults.results[selectedShow].episodes.map(renderEpisodesForSelectedShow)}
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
                <div className={'card mb-5 col-12'}>
                    <div className={'card-body'}>
                        <div className={'row'}>
                            <div className={'col-centered col-lg-4 my-3 d-inline-block'}>
                                <img className={'my-auto flex-center'} src={small} alt={canonicalTitle} />
                            </div>

                            <div className={'col-sm-12 col-lg-8 d-inline-block'}>
                                <div className={'card d-inline-block w-100'}>
                                    {renderedTabNavigation}

                                    <div className={'card-body'}>
                                        {tabs[selectedTab].content}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <VideoModal
                {...selectedEpisode}
                show={selectedEpisode != null}
                onClose={() => setSelectedEpisode(null)}
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
