import React, { useState } from 'react';
import { fetchKitsuTitleSearch } from 'services/KitsuAnimeSearchService';
import SearchBar from 'components/ui/SearchBar';
import KitsuSearchResults from 'components/ui/KitsuSearch/KitsuSearchResults';
import Spinner from 'components/ui/Spinner';

function Home() {
    const pageText = {
        title: 'Anime Atsume',
        description: 'Search aggregator for many anime shows.'
    };

    const [ typedText, setTypedText ] = useState('');
    const [ kitsuResults, setKitsuResults ] = useState(null);
    const [ showSpinner, setShowSpinner ] = useState(false);

    const handleSubmit = async selectedDropdownText => {
        setShowSpinner(true);
        const searchQuery = selectedDropdownText || typedText;
        const response = await fetchKitsuTitleSearch(searchQuery.toLowerCase());
        setKitsuResults(response);
        setShowSpinner(false);
    };

    const anchorHrefFunction = title => `#/show/${title.replace(/\s+/g, '-')}`;

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

    const renderedSearchButtonContent = showSpinner ? <Spinner type={Spinner.Type.CIRCLE} show={showSpinner} /> : null;

    return (
        <div className={'text-center mx-auto'}>
            {renderedTitle}
            {renderedDescription}
            <SearchBar
                btnDisplay={renderedSearchButtonContent}
                value={typedText}
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
