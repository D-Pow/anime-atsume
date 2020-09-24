import React from 'react';
import PropTypes from 'prop-types';
import Anchor from 'components/ui/Anchor';
import { isMobileBrowser } from 'utils/BrowserIdentification';
import { ReactComponent as GitHubLogo } from 'assets/github_logo.svg';

function GitHubPageCornerLink(props) {
    const isMobile = isMobileBrowser({ onlyXsScreenSizes: true });
    const sizePx = isMobile ? props.sizePx - props.sizePxDecreaseOnMobile : props.sizePx;
    const linearGradientAngleDegree = 45;
    const linearGradientAngleFactor = props.side === GitHubPageCornerLink.Sides.LEFT ? -1 : 1;
    const linearGradientAngle = linearGradientAngleDegree * linearGradientAngleFactor;

    return (
        <div
            className={`text-${props.side} float-${props.side} position-absolute`}
            style={{
                top: 0,
                [props.side]: 0,
                width: `${sizePx}px`,
                height: `${sizePx}px`,
                backgroundImage: `linear-gradient(${linearGradientAngle}deg, transparent 55%, ${props.bgColor} 45%)`,
                zIndex: 1000 // ensure div is placed on top of other, overlapping elements
            }}
        >
            <Anchor
                href={props.href}
                className={'d-inline-block position-relative w-40 h-40'}
                aria={{
                    style: {
                        top: 6,
                        [props.side]: 6
                    }
                }}
            >
                <GitHubLogo fill={'white'} />
            </Anchor>
        </div>
    );
}

GitHubPageCornerLink.Sides = {
    LEFT: 'left',
    RIGHT: 'right'
};

GitHubPageCornerLink.propTypes = {
    bgColor: PropTypes.string,
    href: PropTypes.string,
    side: PropTypes.oneOf(Object.values(GitHubPageCornerLink.Sides)),
    sizePx: PropTypes.number,
    sizePxDecreaseOnMobile: PropTypes.number
};

GitHubPageCornerLink.defaultProps = {
    bgColor: 'black',
    href: '',
    side: GitHubPageCornerLink.Sides.LEFT,
    sizePx: 100,
    sizePxDecreaseOnMobile: 20
};

export default GitHubPageCornerLink;
