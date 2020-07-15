import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { useRootClose, useBlockDocumentScrolling } from 'utils/Hooks';
import { isSafariBrowser } from 'utils/BrowserIdentification';

function Modal({
    className,
    title,
    children,
    footer,
    escapeClosesModal,
    useGridForBody,
    useGridForFooter,
    preventDocumentScrolling,
    show,
    showCloseButton,
    onClose,
    forwardRef
}) {
    const [ hideMomentarily, setHideMomentarily ] = useState(false);
    const [ rootWasClosed, resetRootClosed ] = useRootClose(
        { attribute: 'class', value: 'modal-content' },
        { attribute: 'class', value: 'modal fade' },
        escapeClosesModal
    );

    const handleClose = () => {
        // The fade-(in|out) animation relies on the modal being rendered at full screen width/height and then
        // 'show' added/removed accordingly.
        // Because of this, if we simply use the `props.show` value in computing `displayCls` below,
        // the modal will be removed without showing any animation.
        // In order for the user to see the fade-out animation, the 'show' className must be
        // removed *before* the modal is actually removed; thus, delay the actual removal of the
        // modal until after 'show' is removed (i.e. before the size is set to 0).
        setHideMomentarily(true);

        setTimeout(() => {
            onClose();
            setHideMomentarily(false);
        }, 500);
    };

    if (rootWasClosed) {
        // reset keyDown/clickPath so that previous values aren't used if the modal is closed and then re-opened
        resetRootClosed();

        if (show) {
            handleClose();
        }
    }

    useBlockDocumentScrolling(
        () => (show && preventDocumentScrolling)
    );

    const displayCls = (show && !hideMomentarily) ? 'show' : '';
    const sizeStyle = show ? '' : '0%';
    // Default title text to be a header.
    // Clear the margin since that's handled by .modal-title
    const renderedTitle = typeof title === typeof ''
        ? <h4 className={'margin-clear'}>{title}</h4>
        : title;

    return (
        <div className={`modal fade d-block ${displayCls}`}
            style={{
                // Bootstrap's CSS for the modal backdrop's opacity and size doesn't work correctly. Override it here
                background: 'rgba(0, 0, 0, 0.7)',
                width: sizeStyle,
                height: sizeStyle
            }}
        >
            <div className={'modal-dialog modal-dialog-centered width-fit m-auto'} style={{ maxWidth: '90vw' }}>
                <div className={'modal-content overflow-auto ' + className} style={{ maxHeight: '90vh' }} ref={forwardRef}>

                    <div className={'modal-header'} style={isSafariBrowser() ? { display: '-webkit-box' } : {}}>
                        <div className={'modal-title'}>
                            {renderedTitle}
                        </div>
                        {showCloseButton && (
                            <button className={'close'} onClick={handleClose}>
                                <span>&times;</span>
                            </button>
                        )}
                    </div>

                    <div className={'modal-body'}>
                        <div className={useGridForBody ? 'container-fluid' : ''}>
                            {children}
                        </div>
                    </div>

                    {footer && (
                        <div className={'modal-footer'}>
                            <div className={useGridForFooter ? 'container-fluid' : ''}>
                                {footer}
                            </div>
                        </div>
                    )}

                </div>
            </div>
        </div>
    );
}

Modal.propTypes = {
    className: PropTypes.string,
    title: PropTypes.node,
    children: PropTypes.node,
    footer: PropTypes.node,
    escapeClosesModal: PropTypes.bool,
    useGridForBody: PropTypes.bool,
    useGridForFooter: PropTypes.bool,
    preventDocumentScrolling: PropTypes.bool,
    show: PropTypes.bool,
    showCloseButton: PropTypes.bool,
    onClose: PropTypes.func,
    forwardRef: PropTypes.object
};

Modal.defaultProps = {
    className: '',
    title: '',
    children: '',
    footer: '',
    escapeClosesModal: true,
    useGridForBody: true,
    useGridForFooter: true,
    preventDocumentScrolling: true,
    show: false,
    showCloseButton: true,
    onClose: () => {}
};

export default Modal;
