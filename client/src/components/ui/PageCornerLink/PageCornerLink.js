import React from 'react';
import PropTypes from 'prop-types';
import Anchor from 'components/ui/Anchor';
import Image from 'components/ui/Image';
import { isMobileBrowser } from 'utils/BrowserIdentification';

function PageCornerLink(props) {
    const isMobile = isMobileBrowser({ onlyXsScreenSizes: true });
    const sizePx = isMobile ? props.sizePx - props.sizePxDecreaseOnMobile : props.sizePx;
    const linearGradientAngleDegree = 45;
    const linearGradientAngleFactor = props.side === PageCornerLink.Sides.LEFT ? -1 : 1;
    const linearGradientAngle = linearGradientAngleDegree * linearGradientAngleFactor;

    const renderChildren = () => {
        if (props.image) {
            return (
                <React.Fragment>
                    <Image image={props.image} />
                    {props.children}
                </React.Fragment>
            );
        }

        return props.children;
    }

    return (
        <div
            className={`text-${props.side} float-${props.side} position-absolute`}
            style={{
                top: 0,
                [props.side]: 0,
                width: `${sizePx}px`,
                height: `${sizePx}px`,
                /*
                 * linear-gradient degrees start from 90%, i.e. a vertical line, where Xdeg rotates it clockwise and
                 * colors go from left to right.
                 * Color percentages are "distances" from left to right, and mark where the colors stop being 100% their
                 * color and start transitioning to the next color. If a left color stops before the right color begins,
                 * they fade into each other (e.g. `30% red, 70% blue` means the red-blue fade goes from 30% to 70%,
                 * covering 40% of the space); If a left color goes beyond the right color/right color goes under the
                 * left color, then there is no transition, making a sharp dividing line instead where the left color's
                 * percentage overrides the right's (e.g. `20% red, 10% blue` means red stops at 20% and blue takes up
                 * the rest of the space).
                 */
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
                {renderChildren()}
            </Anchor>
        </div>
    );
}

PageCornerLink.Sides = {
    LEFT: 'left',
    RIGHT: 'right'
};

PageCornerLink.propTypes = {
    bgColor: PropTypes.string,
    href: PropTypes.string,
    image: PropTypes.string,
    side: PropTypes.oneOf(Object.values(PageCornerLink.Sides)),
    sizePx: PropTypes.number,
    sizePxDecreaseOnMobile: PropTypes.number,
    children: PropTypes.node
};

PageCornerLink.defaultProps = {
    bgColor: 'black',
    href: '',
    side: PageCornerLink.Sides.RIGHT,
    sizePx: 100,
    sizePxDecreaseOnMobile: 20
};

export default PageCornerLink;
