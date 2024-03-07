import { createRoot } from 'react-dom/client';
import '@fortawesome/fontawesome-free/js/fontawesome';
import '@fortawesome/fontawesome-free/js/solid';

import App from '@/components/App';
import { LOCATION_PROTOCOL_HTTPS } from '@/utils/Constants';
import AppContext from '@/utils/AppContext';

import '@/styles/index.scss';

const { Provider } = AppContext;
const rootDiv = document.getElementById('root');
const reactRoot = createRoot(rootDiv);

reactRoot.render(
    <Provider>
        <App />
    </Provider>,
);

// registerServiceWorker();

if (process.env.NODE_ENV === 'production' && window.location.hostname !== 'localhost') {
    if (window.location.protocol !== LOCATION_PROTOCOL_HTTPS) {
        // window.location.protocol = LOCATION_PROTOCOL_HTTPS;
    }
}

// hot reloading
if (process.env.NODE_ENV !== 'production' && module.hot) {
    console.log('hot reloading active');
    module.hot.accept('@/components/App', () => {
        reactRoot.render(
            <Provider>
                <App />
            </Provider>,
        );
    });
}
