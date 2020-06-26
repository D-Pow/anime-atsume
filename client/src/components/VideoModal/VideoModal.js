import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Modal from 'components/ui/Modal';
import Spinner from 'components/ui/Spinner';
import { searchForEpisodeHost } from 'services/EpisodeHostSearchService';
import { getImageSrcPath, getVideoSrcPath, getVideoNameDataFromUrl } from 'services/Urls';

function VideoModal(props) {
    const [ showSpinner, setShowSpinner ] = useState(false);
    const [ captchaPrompts, setCaptchaPrompts ] = useState([]);
    const [ captchaOptions, setCaptchaOptions ] = useState([]);
    const [ captchaAnswers, setCaptchaAnswers ] = useState([]);
    const [ videoOptions, setVideoOptions ] = useState([]);

    async function fetchEpisodeHost(episodeUrl, captchaAttempt) {
        setShowSpinner(true);
        setCaptchaPrompts([]);
        setCaptchaOptions([]);
        setCaptchaAnswers([]);

        const res = await searchForEpisodeHost(episodeUrl, captchaAttempt);
        const { data, captchaContent } = res;

        if (captchaContent) {
            setCaptchaPrompts(captchaContent.promptTexts);
            setCaptchaOptions(captchaContent.imgIdsAndSrcs);
        }

        if (data) {
            setVideoOptions(data);
        }

        setShowSpinner(false);
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
        setCaptchaAnswers(prevState => {
            const previousAnswers = [...prevState];
            previousAnswers.push(formId);
            return previousAnswers;
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
    };

    useEffect(() => {
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
                <h5>{captchaPrompts[currentPromptIndex]}</h5>
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
                        alt={'failed to load. sorry'}
                    />
                    {i % 2 === 0 ? '' : (<React.Fragment><br/><br/></React.Fragment>)}
                </React.Fragment>
            ))}
        </React.Fragment>
    );

    const renderVideo = () => {
        if (!videoOptions.length) {
            return;
        }

        const { showName, episodeName } = getVideoNameDataFromUrl(props.episodeUrl);
        const { label, file } = videoOptions[0];

        return (
            <React.Fragment>
                <video controls>
                    <source
                        type={'video/mp4'}
                        src={getVideoSrcPath(showName, episodeName, label, file)}
                    />
                </video>
            </React.Fragment>
        );
    };

    const renderedBody = (props.show && props.episodeUrl)
        ? showSpinner
            ? <Spinner className={'w-15 h-15 flex-center'} show={true} />
            : captchaPrompts.length
                ? renderCaptchaImages()
                : renderVideo()
        : '';

    return (
        <Modal
            escapeClosesModal={videoOptions.length === 0}
            show={props.show}
            title={renderedTitle}
            onClose={props.onClose}
        >
            <div className={'overflow-auto'} style={{ minHeight: '200px' }}>
                {renderedBody}
            </div>
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
    episodeUrl: null,
    show: false,
    onClose: () => {}
};

export default VideoModal;
