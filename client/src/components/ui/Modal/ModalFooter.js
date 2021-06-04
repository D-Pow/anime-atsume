import React from 'react';
import PropTypes from 'prop-types';

function ModalFooter({
    useGridForFooter,
    children
}) {
    if (!children) {
        return null;
    }

    return (
        <div className={'modal-footer'}>
            <div className={useGridForFooter ? 'container-fluid' : ''}>
                {children}
            </div>
        </div>
    );
}

ModalFooter.propTypes = {
    children: PropTypes.node,
    useGridForFooter: PropTypes.bool,
};

ModalFooter.defaultProps = {
    children: null,
    useGridForFooter: true,
};

export default ModalFooter;
