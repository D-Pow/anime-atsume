import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { useKeyboardEvent } from 'utils/Hooks';

function SearchBar(props) {
    const handleTyping = ({ target: { value }}) => {
        props.handleTyping(value);
    };

    const [ keyDown, setKeyDown ] = useKeyboardEvent();

    if (keyDown === 'Enter') {
        props.handleSubmit();
        setKeyDown(null);
    }

    // font-awesome replaces <i/> with <svg/> so wrap in a tag
    // that will always be the same so React can mount/unmount as needed
    const defaultDisplay = (<span><i className={'fas fa-search'} /></span>);
    const renderedDisplay = props.btnDisplay ? props.btnDisplay : defaultDisplay;

    return (
        <div className={'row mt-3 mb-5'}>
            <div className={'col-12 col-md-6 mx-auto'}>
                <div className={'input-group my-3'}>
                    <input
                        className={'form-control input-large remove-focus-highlight'}
                        type={'text'}
                        placeholder={'e.g. "Kimi no na wa"'}
                        value={props.value}
                        onChange={handleTyping}
                    />
                    <div className={'input-group-append'}>
                        <button className={'btn btn-outline-secondary remove-focus-highlight'} onClick={() => props.handleSubmit()}>
                            {renderedDisplay}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

SearchBar.propTypes = {
    btnDisplay: PropTypes.node,
    value: PropTypes.string,
    handleTyping: PropTypes.func,
    handleSubmit: PropTypes.func
};

SearchBar.defaultProps = {
    btnDisplay: null,
    value: '',
    handleTyping: () => {},
    handleSubmit: () => {}
};

export default SearchBar;