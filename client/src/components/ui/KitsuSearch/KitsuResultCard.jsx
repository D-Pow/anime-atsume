import React from 'react';
import PropTypes from 'prop-types';

import Anchor from '@/components/ui/Anchor';
import { getMyAnimeListSearchUrl } from '@/services/Urls';

function KitsuResultCard({
    anchorImageFunc,
    anchorImageTarget,
    anchorTitleFunc,
    anchorTitleTarget,
    kitsuResult,
}) {
    if (!kitsuResult || !kitsuResult.attributes) {
        return '';
    }

    const {
        canonicalTitle,
        synopsis,
        episodeCount,
        showType,
        posterImage: {
            small,
        },
    } = kitsuResult.attributes;

    return (
        <React.Fragment>
            <div className={'col-sm-12 col-md-6'}>
                <Anchor className={'m-auto'} target={anchorImageTarget} href={anchorImageFunc(canonicalTitle)}>
                    <img className={'align-self-center img-thumbnail'} src={small} alt={canonicalTitle} />
                </Anchor>
            </div>
            <div className={'media-body align-self-center ml-2 mt-2'}>
                <h5>
                    <Anchor target={anchorTitleTarget} href={anchorTitleFunc(canonicalTitle)}>
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
    anchorImageFunc: PropTypes.func,
    anchorImageTarget: PropTypes.oneOf(Object.values(Anchor.Targets)),
    anchorTitleFunc: PropTypes.func,
    anchorTitleTarget: PropTypes.oneOf(Object.values(Anchor.Targets)),
    kitsuResult: PropTypes.object,
};

KitsuResultCard.defaultProps = {
    anchorImageFunc: title => getMyAnimeListSearchUrl(title),
    anchorImageTarget: Anchor.Targets.SAME_TAB,
    anchorTitleFunc: title => getMyAnimeListSearchUrl(title),
    anchorTitleTarget: Anchor.Targets.SAME_TAB,
};

export default KitsuResultCard;
