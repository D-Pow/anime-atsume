import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { fetchKitsuTitleSearch } from 'services/KitsuAnimeSearchService';
import Spinner from 'components/ui/Spinner';

function Show(props) {
    const title = decodeURIComponent(props.title);

    const [ kitsuResult, setKitsuResult ] = useState(null);

    async function fetchKitsuInfo() {
        const response = await fetchKitsuTitleSearch(title.toLowerCase());
        const allKitsuResults = response.data;
        const showInfo = allKitsuResults.find(show => show.attributes.canonicalTitle === title);

        setKitsuResult(showInfo);
    }

    useEffect(() => {
        fetchKitsuInfo();
    }, []);

    const renderBody = () => {
        if (!kitsuResult) {
            return (
                <div className={'full-screen-minus-scrollbar'}>
                    <Spinner className={'w-25 h-25 absolute-center'} show={true} />
                </div>
            );
        }

        const {
            canonicalTitle,
            synopsis,
            posterImage: {
                small
            }
        } = kitsuResult.attributes;

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
                                    <div className={'card-body'}>
                                        {synopsis}
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
