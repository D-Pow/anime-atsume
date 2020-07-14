import React from 'react';
import PropTypes from 'prop-types';
import { isFirefoxBrowser } from 'utils/BrowserIdentification';

function Spinner(props) {
    if (!props.show) {
        return '';
    }

    let typeCls;

    switch(props.type) {
        case Spinner.Type.CIRCLE:
            typeCls = 'spinner-border spinner-border-sm';
            break;
        case Spinner.Type.DOTS:
        default:
            typeCls = 'fas fa-spinner';
            break;
    }

    const fullScreenCls = props.fullScreen
        ? isFirefoxBrowser()
            ? 'w-25 h-25'
            : 'w-50 h-50'
        : '';
    const spinnerCls = `spin-infinite duration-12 ${typeCls} ${props.className} ${fullScreenCls}`;

    const renderedSpinner = (
        <span className={'text-center'}>
            <div className={spinnerCls} />
        </span>
    );

    if (props.fullScreen) {
        return (
            <div className={'w-100 d-flex justify-content-center align-items-center'} style={{ height: '100vh' }}>
                {renderedSpinner}
            </div>
        );
    }

    return renderedSpinner
}

Spinner.Type = {
    CIRCLE: 'circle',
    DOTS: 'dots'
};

Spinner.propTypes = {
    className: PropTypes.string,
    fullScreen: PropTypes.bool,
    type: PropTypes.oneOf(Object.values(Spinner.Type)),
    show: PropTypes.bool
};

Spinner.defaultProps = {
    className: '',
    fullScreen: false,
    type: Spinner.Type.DOTS
};

export default Spinner;
