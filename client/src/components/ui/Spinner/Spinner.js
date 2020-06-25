import React from 'react';
import PropTypes from 'prop-types';

function Spinner(props) {
    if (!props.show) {
        return;
    }

    let spinnerCls;

    switch(props.type) {
    case Spinner.Type.CIRCLE:
        spinnerCls = 'spinner-border spinner-border-sm';
        break;
    case Spinner.Type.DOTS:
    default:
        spinnerCls = 'fas fa-spinner';
        break;
    }

    return (
        <span>
            <div className={`spin-infinite duration-12 ${spinnerCls} ${props.className}`} />
        </span>
    )
}

Spinner.Type = {
    CIRCLE: 'circle',
    DOTS: 'dots'
};

Spinner.propTypes = {
    className: PropTypes.string,
    type: PropTypes.oneOf(Object.values(Spinner.Type)),
    show: PropTypes.bool
};

Spinner.defaultProps = {
    className: '',
    type: Spinner.Type.DOTS
};

export default Spinner;
