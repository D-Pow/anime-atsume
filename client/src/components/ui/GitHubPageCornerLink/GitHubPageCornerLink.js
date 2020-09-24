import React from 'react';
import PropTypes from 'prop-types';
import Anchor from 'components/ui/Anchor';
import { isMobileBrowser } from 'utils/BrowserIdentification';
import { ReactComponent as GitHubLogo } from 'assets/github_logo.svg';

function GitHubPageCornerLink(props) {
    const isMobile = isMobileBrowser({ onlyXsScreenSizes: true });
    const sizePx = isMobile ? props.sizePx - props.sizePxDecreaseOnMobile : props.sizePx;

    return (
        <div
            className={'text-right float-right position-absolute'}
            style={{
                top: 0,
                right: 0,
                width: `${sizePx}px`,
                height: `${sizePx}px`,
                backgroundImage: 'linear-gradient(45deg, transparent 55%, black 45%)',
                zIndex: 1000 // ensure div is placed on top of other, overlapping elements
            }}
        >
            <div
                className={'position-relative w-50 h-50'}
                style={{
                    top: '7%',
                    left: '42%'
                }}
            >
                <Anchor
                    href={props.href}
                >
                    <GitHubLogo className={'w-75 h-75'} fill={'white'} />
                </Anchor>
            </div>
        </div>
    );
}

GitHubPageCornerLink.propTypes = {
    href: PropTypes.string,
    sizePx: PropTypes.number,
    sizePxDecreaseOnMobile: PropTypes.number
};

GitHubPageCornerLink.defaultProps = {
    href: '',
    sizePx: 100,
    sizePxDecreaseOnMobile: 20
};

export default GitHubPageCornerLink;
