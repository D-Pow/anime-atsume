import React from 'react';
import PropTypes from 'prop-types';
import { isSafariBrowser } from '@/utils/BrowserIdentification';

function ModalHeader({
    children,
    showCloseButton,
    onClose
}) {
    // Default title text to be a header.
    // Clear the margin since that's handled by .modal-title
    const renderedTitle = typeof children === typeof ''
        ? <h4 className={'margin-clear'}>{children}</h4>
        : children;

    if (!children && !showCloseButton) {
        return null;
    }

    return (
        <div className={'modal-header'} style={isSafariBrowser() ? { display: '-webkit-box' } : {}}>
            <div className={'modal-title'}>
                {renderedTitle}
            </div>
            {showCloseButton && (
                <button className={'close'} onClick={onClose}>
                    <span>&times;</span>
                </button>
            )}
        </div>
    );
}

ModalHeader.propTypes = {
    children: PropTypes.node,
    showCloseButton: PropTypes.bool,
    onClose: PropTypes.func,
};

ModalHeader.defaultProps = {
    children: null,
    showCloseButton: true,
    onClose: () => {},
};

export default ModalHeader;
