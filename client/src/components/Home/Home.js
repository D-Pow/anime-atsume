import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { fetchKitsuTitleSearch } from 'services/KitsuAnimeSearchService';
import PageCornerLink from 'components/ui/PageCornerLink';
import SearchBar from 'components/ui/SearchBar';
import KitsuSearchResults from 'components/ui/KitsuSearch/KitsuSearchResults';
import { useQueryParams } from 'utils/Hooks';
import { LINKS } from 'utils/Constants';
import { ReactComponent as GitHubLogo } from 'assets/github_logo.svg';

function Home() {
    const pageText = {
        title: 'Anime Atsume',
        description: 'Search aggregator for many anime shows.'
    };
    const searchQueryParam = 'search';

    const [ typedText, setTypedText ] = useState('');
    const [ kitsuResults, setKitsuResults ] = useState(null);
    const [ showSpinner, setShowSpinner ] = useState(false);
    const [ queryParams, setQueryParam ] = useQueryParams();

    const handleSubmit = async textToSearch => {
        const query = textToSearch ? textToSearch : typedText;
        const searchQuery = query.toLowerCase();

        setShowSpinner(true);
        setQueryParam(searchQueryParam, query);

        const response = await fetchKitsuTitleSearch(searchQuery);
        setKitsuResults(response);
        setShowSpinner(false);
    };

    useEffect(() => {
        const previousSearchedQuery = queryParams[searchQueryParam];

        if (previousSearchedQuery) {
            // React hooks' setState() function is not guaranteed to
            // update the state immediately, so pass the previousSearchedQuery
            // as an argument to handleSubmit()
            setTypedText(previousSearchedQuery);
            handleSubmit(previousSearchedQuery);
        }
    }, []); // Add no dependencies so useEffect() only runs on first page load

    const anchorHrefFunction = title => `#/show/${encodeURIComponent(title)}`;

    const renderedTitle = (
        <div className={'row'}>
            <div className={'col-12 text-center mx-auto mt-5'}>
                <h1>{pageText.title}</h1>
            </div>
        </div>
    );

    const renderedDescription = (
        <div className={'row mt-3'}>
            <div className={'col-12 col-md-6 text-center mx-auto'}>
                <h6>{pageText.description}</h6>
            </div>
        </div>
    );

    const renderedAboutLink = (
        <div className={'row mt-3'}>
            <h6 className={'col-12 col-md-6 text-center mx-auto'}>
                <Link
                    className={'underline'}
                    to={'/about'}
                >
                    What is {pageText.title}?
                </Link>
            </h6>
        </div>
    );

    return (
        <div className={'text-center mx-auto col-12'}>
            <PageCornerLink href={LINKS.AnimeAtsume}>
                <GitHubLogo fill={'white'} />
            </PageCornerLink>
            {renderedTitle}
            {renderedDescription}
            {renderedAboutLink}
            <SearchBar
                value={typedText}
                showBtnSpinner={showSpinner}
                focusOnLoad={!queryParams[searchQueryParam]}
                handleTyping={setTypedText}
                handleSubmit={handleSubmit}
            />
            <KitsuSearchResults
                anchorImageFunc={anchorHrefFunction}
                anchorTitleFunc={anchorHrefFunction}
                kitsuResults={kitsuResults}
            />
        </div>
    );
}

export default Home;
