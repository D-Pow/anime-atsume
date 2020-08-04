import React from 'react';
import PropTypes from 'prop-types';

function IncompatibleBrowserFallback(props) {
    const DisplayElement = props.displayElement;
    let classNames = '';

    if (props.centered) {
        classNames = 'absolute-center top-20';
    }

    return (
        <div className={`text-center w-100 ${classNames} ${props.className}`}>
            <DisplayElement className={'w-80 m-auto'}>
                {props.text}
            </DisplayElement>
        </div>
    );
}

IncompatibleBrowserFallback.propTypes = {
    className: PropTypes.string,
    centered: PropTypes.bool,
    displayElement: PropTypes.string,
    text: PropTypes.string
};

IncompatibleBrowserFallback.defaultProps = {
    className: '',
    centered: true,
    displayElement: 'h1',
    text: 'Please use a modern browser (Chrome, Firefox) to view this website.'
};

export default IncompatibleBrowserFallback;
