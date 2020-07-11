import React from 'react';
import PropTypes from 'prop-types';

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

    const fullScreenCls = props.fullScreen ? 'w-25 h-25 absolute-center' : '';
    const spinnerCls = `spin-infinite duration-12 ${typeCls} ${props.className} ${fullScreenCls}`;

    const renderedSpinner = (
        <span>
            <div className={spinnerCls} />
        </span>
    );

    if (props.fullScreen) {
        return (
            <div className={'full-screen-minus-scrollbar'}>
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
