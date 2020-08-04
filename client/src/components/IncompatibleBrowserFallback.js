import React from 'react';
import PropTypes from 'prop-types';

function IncompatibleBrowserFallback(props) {
    let classNames = '';

    if (props.centered) {
        classNames = 'absolute-center top-20';
    }

    return (
        <div className={`text-center w-100 ${classNames} ${props.className}`}>
            <h1 className={'w-80 m-auto'}>
                {props.text}
            </h1>
        </div>
    );
}

IncompatibleBrowserFallback.propTypes = {
    className: PropTypes.string,
    centered: PropTypes.bool,
    text: PropTypes.string
};

IncompatibleBrowserFallback.defaultProps = {
    className: '',
    centered: true,
    text: 'Please use a modern browser (Chrome, Firefox) to view this website.'
};

export default IncompatibleBrowserFallback;
