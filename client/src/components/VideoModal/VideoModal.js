import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import Modal from 'components/ui/Modal';
import Video from 'components/ui/Video';
import Spinner from 'components/ui/Spinner';
import ErrorDisplay from 'components/ui/ErrorDisplay';
import { searchForEpisodeHost } from 'services/EpisodeHostSearchService';
import { getImageSrcPath, getVideoSrcPath, getVideoNameDataFromUrl } from 'services/Urls';

function VideoModal(props) {
    const defaultStateValues = {
        hasError: false,
        showSpinner: false,
        captchaPrompts: [],
        captchaOptions: [],
        captchaAnswers: [],
        videoOptions: [],
        captchaImagesLoaded: new Set()
    };

    const [ hasError, setHasError ] = useState(defaultStateValues.hasError);
    const [ showSpinner, setShowSpinner ] = useState(defaultStateValues.showSpinner);
    const [ captchaPrompts, setCaptchaPrompts ] = useState(defaultStateValues.captchaPrompts);
    const [ captchaOptions, setCaptchaOptions ] = useState(defaultStateValues.captchaOptions);
    const [ captchaAnswers, setCaptchaAnswers ] = useState(defaultStateValues.captchaAnswers);
    const [ videoOptions, setVideoOptions ] = useState(defaultStateValues.videoOptions);
    const [ captchaImagesLoaded, setCaptchaImagesLoaded ] = useState(defaultStateValues.captchaImagesLoaded);
    const modalRef = useRef(null);

    const isDisplayingVideo = videoOptions.length > 0;

    const resetState = () => {
        setHasError(defaultStateValues.hasError);
        setCaptchaPrompts(defaultStateValues.captchaPrompts);
        setCaptchaOptions(defaultStateValues.captchaOptions);
        setCaptchaAnswers(defaultStateValues.captchaAnswers);
        setVideoOptions(defaultStateValues.videoOptions);
        setCaptchaImagesLoaded(defaultStateValues.captchaImagesLoaded);
    };

    const handleClose = () => {
        resetState();
        props.onClose();
    };

    async function fetchEpisodeHost(episodeUrl, captchaAttempt) {
        setShowSpinner(true);
        resetState();

        try {
            const res = await searchForEpisodeHost(episodeUrl, captchaAttempt);

            if (res.status && res.status > 299) {
                throw `Got HTTP status code ${res.status} from server. Error: ${res.error}.`;
            }

            const { data, captchaContent } = res;

            if (captchaContent) {
                setCaptchaPrompts(captchaContent.promptTexts);
                setCaptchaOptions(captchaContent.imgIdsAndSrcs);
            }

            if (data) {
                setVideoOptions(data);
            }

            setShowSpinner(false);
        } catch (e) {
            console.error('Error fetching for episodes:', e);
            setHasError(true);
        }
    }

    useEffect(() => {
        if (props.show && props.episodeUrl) {
            fetchEpisodeHost(props.episodeUrl);
        }
    }, [ props.show, props.episodeUrl ]);

    const submitAnswers = () => {
        const answers = captchaAnswers.map((formId, promptIndex) => {
            const associatedCaptchaOption = captchaOptions
                .find(option => option.formId === formId);
            const associatedPrompt = captchaPrompts[promptIndex];
            const answer = {...associatedCaptchaOption};

            answer.promptText = associatedPrompt;

            return answer;
        });

        fetchEpisodeHost(props.episodeUrl, answers);
    };

    const addCaptchaAnswer = formId => {
        setCaptchaAnswers(prevCaptchaAnswers => {
            const newCapchaAnswers = [...prevCaptchaAnswers];
            newCapchaAnswers.push(formId);
            return newCapchaAnswers;
        });
    };

    const isFormIdAlreadyInAnswers = formId => {
        return captchaAnswers.includes(formId);
    };

    const handleImageSelection = formId => {
        if (!isFormIdAlreadyInAnswers(formId)) {
            addCaptchaAnswer(formId);
        } else {
            setCaptchaAnswers([]);
        }

        modalRef.current.scrollTo(0, 0);
    };

    const handleCaptchaImageLoaded = imageIndex => {
        setCaptchaImagesLoaded(prevImagesLoaded => {
            const imagesLoaded = new Set(prevImagesLoaded);
            imagesLoaded.add(imageIndex);
            return imagesLoaded;
        });
    };

    useEffect(() => {
        // useEffect is necessary instead of calling submitAnswers() in handleImageSelection()
        // because state updates are async and not guaranteed to take effect as soon as setState()
        // is called, and because the useState() hook doesn't have a callback function like
        // class components' setState() does
        if (props.show
            && props.episodeUrl
            && (captchaAnswers.length > 0)
            && (captchaAnswers.length === captchaPrompts.length)
        ) {
            submitAnswers();
        }
    }, [ props.show, props.episodeUrl, captchaAnswers.length, captchaPrompts.length ]);

    const currentPromptIndex = captchaAnswers.length;

    const renderedTitle = captchaPrompts.length
        ? (
            <div className={'text-center'}>
                <h4>Please solve this captcha</h4>
                <h5 className={'d-inline'}>
                    ({currentPromptIndex+1}/{captchaPrompts.length})
                </h5>
                <h5 className={'text-danger d-inline ml-1'}>
                    {captchaPrompts[currentPromptIndex]}
                </h5>
            </div>
        ) : props.episodeTitle;

    const renderCaptchaImages = () => (
        <React.Fragment>
            {captchaOptions.map(({ formId, imageId }, i) => (
                <React.Fragment key={formId}>
                    <img
                        className={
                            isFormIdAlreadyInAnswers(formId)
                                ? 'border border-medium border-primary rounded'
                                : ''
                        }
                        src={getImageSrcPath(imageId)}
                        onClick={() => handleImageSelection(formId)}
                        onLoad={() => handleCaptchaImageLoaded(i)}
                        alt={'failed to load. sorry'}
                    />
                    {i % 2 === 0 ? '' : (<React.Fragment><br/><br/></React.Fragment>)}
                    <Spinner show={!captchaImagesLoaded.has(i)} />
                </React.Fragment>
            ))}
        </React.Fragment>
    );

    const renderVideo = () => {
        if (!isDisplayingVideo) {
            return;
        }

        const { showName, episodeName } = getVideoNameDataFromUrl(props.episodeUrl);
        const { label, file } = videoOptions[0];

        return (
            <Video
                className={'w-100'}
                src={getVideoSrcPath(showName, episodeName, label, file)}
                videoElementProps={props.videoElementProps}
            />
        );
    };

    const renderedBody = (props.show && props.episodeUrl)
        ? hasError
            ? <ErrorDisplay suggestion={'Try closing and re-opening the modal.'} show={hasError} />
            : showSpinner
                ? <Spinner className={'w-40 h-40'} show={true} />
                : captchaPrompts.length
                    ? renderCaptchaImages()
                    : renderVideo()
        : '';

    return (
        <Modal
            className={'scroll-smooth'}
            escapeClosesModal={!isDisplayingVideo}
            show={props.show}
            title={renderedTitle}
            onClose={handleClose}
            forwardRef={modalRef}
        >
            <div
                className={`overflow-auto ${showSpinner ? 'd-flex justify-content-center align-items-center' : ''}`}
                style={{ minHeight: '200px' }}
            >
                {renderedBody}
            </div>
        </Modal>
    );
}

VideoModal.propTypes = {
    episodeTitle: PropTypes.string,
    episodeUrl: PropTypes.string,
    show: PropTypes.bool,
    onClose: PropTypes.func,
    videoElementProps: PropTypes.object
};

VideoModal.defaultProps = {
    episodeTitle: '',
    episodeUrl: null,
    show: false,
    onClose: () => {},
    videoElementProps: {}
};

export default VideoModal;
