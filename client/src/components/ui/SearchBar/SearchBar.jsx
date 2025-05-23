import { forwardRef } from 'react';
import PropTypes from 'prop-types';

import Spinner from '@/components/ui/Spinner';
import { useKeyboardEvent } from '@/utils/Hooks';

function SearchBar(props, inputRef) {
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
    const renderedDefaultBtn = (<span><i className={'fas fa-search'} /></span>);
    const renderedBtnSpinner = <Spinner type={Spinner.Type.CIRCLE} show={props.showBtnSpinner} />;
    const renderedBtn = props.showBtnSpinner
        ? renderedBtnSpinner
        : props.btnChildren
            ? props.btnChildren
            : renderedDefaultBtn;

    return (
        <div className={'row mt-3 mb-5'}>
            <div className={'col-12 col-md-6 mx-auto'}>
                <div className={'input-group my-3'}>
                    <input
                        className={'form-control remove-focus-highlight'}
                        type={'text'}
                        autoFocus={props.focusOnLoad}
                        placeholder={'e.g. "Kimi no na wa"'}
                        value={props.value}
                        onChange={handleTyping}
                        ref={inputRef}
                    />
                    <div className={'input-group-append'}>
                        <button className={'btn btn-outline-secondary remove-focus-highlight'} onClick={() => props.handleSubmit()}>
                            {renderedBtn}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

SearchBar.propTypes = {
    btnChildren: PropTypes.node,
    focusOnLoad: PropTypes.bool,
    value: PropTypes.string,
    showBtnSpinner: PropTypes.bool,
    handleTyping: PropTypes.func,
    handleSubmit: PropTypes.func,
};

SearchBar.defaultProps = {
    btnChildren: null,
    focusOnLoad: true,
    value: '',
    showBtnSpinner: false,
    handleTyping: () => {},
    handleSubmit: () => {},
};

export default forwardRef(SearchBar);
