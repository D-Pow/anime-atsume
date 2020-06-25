import React from 'react';
import PropTypes from 'prop-types';
import Modal from 'components/ui/Modal';

function VideoModal(props) {
    return (
        <Modal
            show={props.show}
            title={props.episodeTitle}
            onClose={props.onClose}
        >
            {props.episodeUrl}
        </Modal>
    );
}

VideoModal.propTypes = {
    episodeTitle: PropTypes.string,
    episodeUrl: PropTypes.string,
    show: PropTypes.bool,
    onClose: PropTypes.func
};

VideoModal.defaultProps = {
    episodeTitle: '',
    episodeUrl: '',
    show: false,
    onClose: () => {}
};

export default VideoModal;
