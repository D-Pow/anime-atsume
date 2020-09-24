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
