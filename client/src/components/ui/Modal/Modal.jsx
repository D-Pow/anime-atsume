import { useState } from 'react';
import PropTypes from 'prop-types';

import { useRootClose, useBlockDocumentScrolling } from '@/utils/Hooks';

import ModalHeader from './ModalHeader';
import ModalBody from './ModalBody';
import ModalFooter from './ModalFooter';

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
    modalContainerProps,
    modalContentProps,
    forwardRef,
}) {
    const [ hideMomentarily, setHideMomentarily ] = useState(false);
    const [ rootWasClosed, resetRootClosed ] = useRootClose(
        { attribute: 'class', value: 'modal-content' },
        { attribute: 'class', value: 'modal fade' },
        escapeClosesModal,
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
        () => (show && preventDocumentScrolling),
    );

    const displayCls = (show && !hideMomentarily) ? 'show' : '';
    const sizeStyle = show ? '' : '0%';

    return (
        <div
            className={`modal fade d-flex flex-center ${displayCls}`}
            style={{
                // Bootstrap's CSS for the modal backdrop's opacity and size doesn't work correctly. Override it here
                background: 'rgba(0, 0, 0, 0.7)',
                width: sizeStyle,
                height: sizeStyle,
            }}
        >
            <div className={'modal-dialog modal-dialog-centered flex-center width-fit'} {...modalContainerProps}>
                <div className={'modal-content overflow-auto ' + className} ref={forwardRef} {...modalContentProps}>

                    <ModalHeader showCloseButton={showCloseButton} onClose={handleClose}>
                        {title}
                    </ModalHeader>

                    <ModalBody useGridForBody={useGridForBody}>
                        {children}
                    </ModalBody>

                    <ModalFooter useGridForFooter={useGridForFooter}>
                        {footer}
                    </ModalFooter>

                </div>
            </div>
        </div>
    );
}

Modal.propTypes = {
    ...ModalHeader.propTypes,
    ...ModalBody.propTypes,
    ...ModalFooter.propTypes,
    className: PropTypes.string,
    title: PropTypes.node,
    footer: PropTypes.node,
    escapeClosesModal: PropTypes.bool,
    preventDocumentScrolling: PropTypes.bool,
    show: PropTypes.bool,
    modalContainerProps: PropTypes.object,
    modalContentProps: PropTypes.object,
    forwardRef: PropTypes.object,
};

Modal.defaultProps = {
    className: '',
    escapeClosesModal: true,
    preventDocumentScrolling: true,
    show: false,
    modalContainerProps: {},
    modalContentProps: {},
    forwardRef: null,
};

export default Modal;
