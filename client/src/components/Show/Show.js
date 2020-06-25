import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { fetchKitsuTitleSearch } from 'services/KitsuAnimeSearchService';
import { searchForShow } from 'services/ShowSearchService';
import Spinner from 'components/ui/Spinner';

function Show(props) {
    const title = decodeURIComponent(props.title);

    const [ selectedTab, setSelectedTab ] = useState(0);
    const [ kitsuResult, setKitsuResult ] = useState(null);
    const [ episodeResults, setEpisodeResults ] = useState(null);
    const [ showsExpanded, setShowsExpanded ] = useState([]);

    async function fetchKitsuInfo() {
        const response = await fetchKitsuTitleSearch(title.toLowerCase());
        const allKitsuResults = response.data;
        const showInfo = allKitsuResults.find(show => show.attributes.canonicalTitle === title);

        setKitsuResult(showInfo);
    }

    async function fetchShowAndEpisodesList() {
        const episodeResults = await searchForShow(title);
        setShowsExpanded(
            Array
                .from({ length: episodeResults.results.length })
                .map(() => false)
        );
        setEpisodeResults(episodeResults);
    }

    useEffect(() => {
        fetchKitsuInfo();
        fetchShowAndEpisodesList();
    }, []);

    const toggleIsShowExpanded = showIndex => {
        setShowsExpanded(prevState => {
            const prevShowsExpanded = [...prevState];
            prevShowsExpanded[showIndex] = !prevShowsExpanded[showIndex];
            return prevShowsExpanded;
        });
    };

    const renderPossibleMatchesAndEpisodes = ({ title: showTitle, episodes: showEpisodes }, i) => (
        <ul className={'text-left'} key={i}>
            <li>
                <h4>
                    <a className={'text-primary cursor-pointer'} onClick={() => toggleIsShowExpanded(i)}>
                        {showTitle} - {showEpisodes.length}
                    </a>
                </h4>
                <ul className={`collapse ${showsExpanded[i] ? 'show' : ''}`}>
                    {showEpisodes.map(({ title: episodeTitle, url }, j) => (
                        <li key={`${i}-${j}`}>
                            <a className={'text-primary cursor-pointer'}>
                                {episodeTitle}
                            </a>
                        </li>
                    ))}
                </ul>
            </li>
        </ul>
    )

    const renderBody = () => {
        if (!kitsuResult || !episodeResults) {
            return (
                <div className={'full-screen-minus-scrollbar'}>
                    <Spinner className={'w-25 h-25 absolute-center'} show={true} />
                </div>
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
                    <React.Fragment>
                        <h5 className={'capitalize-first'}>
                            {episodeCount === 1
                                ? showType
                                : episodeCount + ' episodes'
                            }
                        </h5>
                        <p>
                            {synopsis}
                        </p>
                    </React.Fragment>
                )
            },
            {
                tabTitle: 'Possible Show/Episode Matches',
                content: (
                    <React.Fragment>
                        {episodeResults.results.map(renderPossibleMatchesAndEpisodes)}
                    </React.Fragment>
                )
            }
        ];

        const renderedTabNavigation = (
            <nav>
                <ul className={'pagination'}>
                    {tabs.map(({ tabTitle }, i) => (
                        <li className={'page-item'} key={i} onClick={() => setSelectedTab(i)}>
                            <a className={'page-link cursor-pointer'}>
                                {tabTitle}
                            </a>
                        </li>
                    ))}
                </ul>
            </nav>
        );

        return (
            <div className={'row pt-5'}>
                <div className={'card mb-5 col-12'}>
                    <div className={'row'}>
                        <div className={'card-body'}>
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
        );
    };

    return (
        <React.Fragment>
            <div className={'row pb-5'}>
                <h1 className={'text-center mx-auto mt-5'}>
                    {title}
                </h1>
            </div>

            {renderBody()}
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
