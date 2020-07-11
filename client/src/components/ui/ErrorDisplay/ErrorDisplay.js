import React from 'react';
import PropTypes from 'prop-types';

function ErrorDisplay(props) {
    if (!props.show) {
        return '';
    }

    return (
        <div className={`${props.className} ${props.fullScreen ? 'full-screen-minus-scrollbar' : ''}`}>
            <div className={props.fullScreen ? 'absolute-center' : ''}>
                <h3 className={'mr-1'}>Sorry, something went wrong.</h3>
                <h3>{props.suggestion}</h3>
            </div>
        </div>
    );
}

ErrorDisplay.propTypes = {
    className: PropTypes.string,
    fullScreen: PropTypes.bool,
    show: PropTypes.bool,
    suggestion: PropTypes.string
};

ErrorDisplay.defaultProps = {
    className: '',
    fullScreen: false,
    show: false,
    suggestion: 'Try refreshing the page.'
};

export default ErrorDisplay;
