import React from 'react';
import PropTypes from 'prop-types';
import Header from 'components/Header';

function About(props) {
    const handleStorageClearButtonClick = () => {
        localStorage.clear();
        alert('Show watch history has been cleared!');
    };

    return (
        <div className={'col-12'}>
            <Header
                className={'position-absolute'}
                navEntries={[
                    {
                        children: (
                            <React.Fragment>
                                <span className={'mr-2'}>
                                    <i className={'fas fa-chevron-left'} />
                                </span>

                                Back
                            </React.Fragment>
                        ),
                        onClick: () => {
                            // Attempt to use back function from react-router-dom's
                            // `history` prop with native window.history object as
                            // a fallback
                            const backFunction = props.history ? props.history.goBack : history.back;

                            backFunction();
                        }
                    }
                ]}
            />

            <div className={'row mt-5'}>
                <h1 className={'mx-auto'}>
                    About
                </h1>
            </div>

            <div className={'row mt-3'}>
                <div className={'col-12 col-sm-9 col-md-8 col-lg-6 mx-auto'}>
                    <div>
                        We have all been there: you visit some website to watch your
                        favorite show just to be bombarded with ads, weird click-bait,
                        and worst of all, those invisible buttons positioned right
                        over the video that open to a sketchy tab when you try to
                        click the play button.
                    </div>
                    <div className={'mt-5'}>
                        <span className={'font-weight-bold'}>Anime Atsume</span> is intended to be the exact opposite: overly
                        simplistic, easy to use interface, and the quickest possible
                        path from finding to watching your shows.
                    </div>
                    <div className={'p mt-5'}>
                        <span className={'font-weight-bold'}>Note:</span>
                        This app uses the {"browser's"} <pre className={'d-inline'}>localStorage</pre> feature to keep
                        track of your last watched episodes for a given show so you can easily continue a series from
                        where you last left off. This data is not collected and is in no way able to track identity
                        or app usage. It is fully in your control. You may clear your watch history if you wish by
                        clicking

                        <button
                            className={'btn btn-link p-0 pl-1 remove-focus-highlight border-0'}
                            onClick={handleStorageClearButtonClick}
                        >
                            here
                        </button>
                        .
                    </div>
                </div>
            </div>
        </div>
    );
}

About.propTypes = {
    history: PropTypes.object // from react-router-dom
}

export default About;
