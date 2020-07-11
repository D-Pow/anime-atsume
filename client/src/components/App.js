import React from 'react';
import { HashRouter as Router, Route, Redirect } from 'react-router-dom';
import Spinner from 'components/ui/Spinner';

/**
 * Lazy-load components so the page spinner is prioritized, loaded quickly, and unblocked from animating.
 * This speeds up the initial page load for the user.
 *
 * Split import() and lazy() calls from each other so that component-loading is initiated immediately
 * instead of waiting to load until they are in view. This has the net effect of allowing the Spinner
 * to load first, but then loading the rest of the components as soon as the Spinner is rendered.
 * If the promise were nested inside the lazy() call instead, then e.g. the About component wouldn't
 * be loaded until the user traverses to /about.
 */

const homeImportPromise = import(/* webpackChunkName: 'Home' */ 'components/Home');
const Home = React.lazy(() => homeImportPromise);

const showImportPromise = import(/* webpackChunkName: 'Home' */ 'components/Show');
const Show = React.lazy(() => showImportPromise);

const routes = [
    {
        path: '/',
        render: () => <Redirect to="/home" />,
        name: 'Home',
        exact: true
    },
    {
        path: '/home',
        component: Home,
        name: 'Home',
        exact: true
    },
    {
        path: '/show/:showName',
        render: ({ match }) => <Show title={match.params.showName} />,
        name: 'Show'
    }
];

function App() {
    return (
        <React.Suspense
            fallback={<Spinner fullScreen={true} show={true} />}
        >
            <div className={'app container-fluid text-center'}>
                <div className={'col-12'}>
                    <Router>
                        <React.Fragment>
                            {routes.map(routeAria => (
                                <Route key={routeAria.path} {...routeAria} />
                            ))}
                        </React.Fragment>
                    </Router>
                </div>
            </div>
        </React.Suspense>
    );
}

export default App;
