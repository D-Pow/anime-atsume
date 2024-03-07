import React from 'react';
import PropTypes from 'prop-types';

function Header(props) {
    const defaultNavEntryProps = {
        className: '',
        wrapperCls: '',
        href: null,
        onClick: () => {},
    };

    const stubEmptyNavEntryPropsWithDefaults = navEntryProps => ({
        ...defaultNavEntryProps,
        ...navEntryProps,
    });

    const renderedNavEntries = props.navEntries.map((entryProps, i) => {
        const navEntryProps = stubEmptyNavEntryPropsWithDefaults(entryProps);

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

    const renderedHeaderChildren = (
        <nav className={'navbar'}>
            <ul className={'navbar-nav flex-row justify-content-between'}>
                {renderedNavEntries}
            </ul>
        </nav>
    );

    const headerCls = `header ${props.border ? 'header-border' : ''} ${props.className}`;

    return (
        <React.Fragment>
            <header className={headerCls}>
                {renderedHeaderChildren}
            </header>

            <div className={'d-block d-sm-none pb-2'} />
        </React.Fragment>
    );
}

Header.propTypes = {
    border: PropTypes.bool,
    className: PropTypes.string,
    navEntries: PropTypes.arrayOf(
        PropTypes.shape({
            children: PropTypes.oneOfType([
                PropTypes.node,
                PropTypes.arrayOf(PropTypes.node),
            ]).isRequired,
            className: PropTypes.string,
            wrapperCls: PropTypes.string,
            href: PropTypes.string,
            onClick: PropTypes.func,
        }),
    ),
};

Header.defaultProps = {
    border: false,
    className: '',
    navEntries: [],
};

export default Header;
