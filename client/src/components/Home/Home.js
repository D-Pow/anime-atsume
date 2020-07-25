import React, { useState, useEffect } from 'react';
import { fetchKitsuTitleSearch } from 'services/KitsuAnimeSearchService';
import SearchBar from 'components/ui/SearchBar';
import KitsuSearchResults from 'components/ui/KitsuSearch/KitsuSearchResults';
import { useQueryParams } from 'utils/Hooks';

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

    return (
        <div className={'text-center mx-auto'}>
            {renderedTitle}
            {renderedDescription}
            <SearchBar
                value={typedText}
                showBtnSpinner={showSpinner}
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
