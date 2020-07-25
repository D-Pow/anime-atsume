import React from 'react';
import PropTypes from 'prop-types';
import Header from 'components/Header';

function About(props) {
    return (
        <div className={'col-12'}>
            <Header
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
                    <p>
                        We have all been there: you visit some website to watch your
                        favorite show just to be bombarded with ads, weird click-bait,
                        and worst of all, those invisible buttons positioned right
                        over the video that open to a sketchy tab when you try to
                        click the play button.
                    </p>
                    <p className={'mt-5'}>
                        <span className={'font-weight-bold'}>Anime Atsume</span> is intended to be the exact opposite: overly
                        simplistic, easy to use interface, and the quickest possible
                        path from finding to watching your shows.
                    </p>
                </div>
            </div>
        </div>
    );
}

About.propTypes = {
    history: PropTypes.object // from react-router-dom
}

export default About;
