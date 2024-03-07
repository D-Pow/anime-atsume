import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';

import { importImageAsync } from '@/utils/Events';

function Image(props) {
    const [ imageSrc, setImageSrc ] = useState('');

    async function loadImageSrc() {
        const imageSrc = await importImageAsync(props.image);

        setImageSrc(imageSrc);
    }

    useEffect(() => {
        loadImageSrc();
    }, [ props.image ]);

    function handleLoad(e) {
        props.onLoad(e);
    }

    return (
        <img
            className={`${props.fluidImage ? 'img-fluid' : ''} ${props.className}`}
            src={imageSrc}
            alt={props.image}
            onLoad={handleLoad}
            {...props.aria}
        />
    );
}

Image.propTypes = {
    className: PropTypes.string,
    image: PropTypes.string,
    fluidImage: PropTypes.bool,
    onLoad: PropTypes.func,
    aria: PropTypes.object,
};

Image.defaultProps = {
    className: '',
    image: '',
    fluidImage: true,
    onLoad: () => {},
    aria: {},
};

export default Image;
