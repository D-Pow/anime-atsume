import React from 'react';
import PropTypes from 'prop-types';

function Video(props) {
    return (
        <video className={props.className} controls autoPlay ref={videoRef}>
            <source src={props.src} type={props.type} />
        </video>
    );
}

Video.propTypes = {
    className: PropTypes.string,
    src: PropTypes.string,
    type: PropTypes.string
};

Video.defaultProps = {
    className: '',
    src: '',
    type: 'video/mp4'
};

export default Video;
