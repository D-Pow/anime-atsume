import { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import { useWindowEvent } from '@/utils/Hooks';

function Video(props) {
    const maxVideoVolume = 1;
    const minVideoVolume = 0;
    const defaultVolumeDelta = 0.05;
    const defaultSeekSpeedDelta = 5;
    const [ seekSpeed, setSeekSpeed ] = useState(defaultSeekSpeedDelta);
    const [ keyEvent, setKeyEvent ] = useWindowEvent('keydown');
    let videoRef = useRef(null);

    if (props.videoRef) {
        videoRef = props.videoRef;
    }

    /*
     * Custom video key listeners to normalize video experience
     * across browsers. Includes the following:
     *
     * Same as default browser listeners:
     *  - Changing the volume
     *  - Seeking through the video
     *  - Play/pause (since it was overwritten in useEffect())
     *
     * New/not included in default browser listeners:
     *  - Changing the seek speed (with Shift)
     *  - Entering fullscreen (with F)
     */
    function handleKeyEvent() {
        const video = videoRef.current;

        if (!video) {
            return;
        }

        switch (keyEvent.key) {
            case 'ArrowLeft':
                video.currentTime -= seekSpeed;
                break;
            case 'ArrowRight':
                video.currentTime += seekSpeed;
                break;
            case 'ArrowUp':
                if (keyEvent.shiftKey) {
                    setSeekSpeed(seekSpeed + 1);
                } else if (video.volume <= (maxVideoVolume - defaultVolumeDelta)) {
                    video.volume += defaultVolumeDelta;
                } else {
                    video.volume = maxVideoVolume;
                }
                break;
            case 'ArrowDown':
                if (keyEvent.shiftKey) {
                    setSeekSpeed(seekSpeed - 1);
                } else if (video.volume >= (minVideoVolume + defaultVolumeDelta)) {
                    video.volume -= defaultVolumeDelta;
                } else {
                    video.volume = minVideoVolume;
                }
                break;
            case 'f':
                video.requestFullscreen();
                break;
            case ' ':
                if (!video.paused) {
                    video.pause();
                } else {
                    video.play();
                }
                break;
        }

        setKeyEvent(null);
    }

    if (keyEvent) {
        handleKeyEvent();
    }

    useEffect(() => {
        if (videoRef.current) {
            // Disable browser's inherent key listeners
            // by marking the video as not-focused
            videoRef.current.onfocus = () => videoRef.current.blur();
        }
    }, [ videoRef ]);

    if (props.isIframe) {
        return (
            <iframe
                className={props.className}
                allowFullScreen
                ref={videoRef}
                src={props.src}
                {...props.videoElementProps}
            />
        );
    }

    return (
        <video className={props.className} controls autoPlay ref={videoRef} {...props.videoElementProps}>
            <source src={props.src} type={props.type} />
        </video>
    );
}

Video.propTypes = {
    className: PropTypes.string,
    src: PropTypes.string,
    isIframe: PropTypes.bool,
    type: PropTypes.string,
    videoElementProps: PropTypes.object,
    videoRef: PropTypes.object,
};

Video.defaultProps = {
    className: '',
    src: '',
    isIframe: false,
    type: 'video/mp4',
    videoElementProps: {},
};

export default Video;
