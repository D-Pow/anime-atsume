import React from 'react';
import PropTypes from 'prop-types';

function Header(props) {
    const defaultNavEntryProps = {
        className: '',
        wrapperCls: '',
        href: null,
        onClick: () => {}
    };

    const renderedNavEntries = props.navEntries.map((entryProps, i) => {
        const navEntryProps = { ...defaultNavEntryProps, ...entryProps };

        return (
            <li className={`nav-item p-2 ${navEntryProps.wrapperCls}`} key={i}>
                <a
                    className={`nav-link cursor-pointer ${navEntryProps.className}`}
                    href={navEntryProps.href}
                    onClick={navEntryProps.onClick}
                >
                    {navEntryProps.children}
                </a>
            </li>
        );
    });

    return (
        <header className={`header ${props.border ? 'header-border' : ''}`}>
            <nav className={'navbar'}>
                <ul className={'navbar-nav flex-row justify-content-between'}>
                    {renderedNavEntries}
                </ul>
            </nav>
        </header>
    );
}

Header.propTypes = {
    border: PropTypes.bool,
    navEntries: PropTypes.arrayOf(
        PropTypes.shape({
            children: PropTypes.oneOfType([
                PropTypes.node,
                PropTypes.arrayOf(PropTypes.node)
            ]).isRequired,
            className: PropTypes.string,
            wrapperCls: PropTypes.string,
            href: PropTypes.string,
            onClick: PropTypes.func
        })
    )
};

Header.defaultProps = {
    border: false,
    navEntries: []
};

export default Header;
