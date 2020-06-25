import React from 'react';
import PropTypes from 'prop-types';

function Show(props) {
    return (
        <div>{props.title}</div>
    );
}

Show.propTypes = {
    title: PropTypes.string
};

Show.defaultProps = {
    title: ''
};

export default Show;
