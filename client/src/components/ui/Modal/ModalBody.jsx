import PropTypes from 'prop-types';

function ModalBody(props) {
    const {
        useGridForBody,
        children,
    } = props;

    return (
        <div className={'modal-body'}>
            <div className={useGridForBody ? 'container-fluid' : ''}>
                {children}
            </div>
        </div>
    );
}

ModalBody.propTypes = {
    children: PropTypes.node,
    useGridForBody: PropTypes.bool,
};

ModalBody.defaultProps = {
    children: null,
    useGridForBody: true,
};

export default ModalBody;
