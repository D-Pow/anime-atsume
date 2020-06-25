import React from 'react';
import PropTypes from 'prop-types';
import Anchor from 'components/ui/Anchor';
import { getMyAnimeListSearchUrl } from 'services/Urls';

function KitsuResultCard({ kitsuResult }) {
    if (!kitsuResult || !kitsuResult.attributes) {
        return '';
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

    return (
        <React.Fragment>
            <Anchor href={getMyAnimeListSearchUrl(canonicalTitle)}>
                <img className={'align-self-center img-thumbnail'} src={small} alt={canonicalTitle} />
            </Anchor>
            <div className={'media-body align-self-center ml-2 mt-2'}>
                <h5>
                    <Anchor href={getMyAnimeListSearchUrl(canonicalTitle)}>
                        {canonicalTitle}
                    </Anchor>
                    {` (${episodeCount === 1 ? showType : episodeCount + ' episodes'})`}
                </h5>
                <p>{synopsis}</p>
            </div>
        </React.Fragment>
    );
}

KitsuResultCard.propTypes = {
    kitsuResult: PropTypes.object
};

export default KitsuResultCard;
