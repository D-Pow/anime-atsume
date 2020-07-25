import React from 'react';

function About() {
    return (
        <div className={'col-12'}>
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

export default About;
