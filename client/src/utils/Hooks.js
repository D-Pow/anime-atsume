import { useState, useEffect, useRef } from 'react';

import { elementIsInClickPath, getClickPath } from '@/utils/Events';
import { getQueryParams, pushQueryParamOnHistory } from '@/utils/BrowserNavigation';
import { objEquals } from '@/utils/Objects';

/**
 * Valid JSON primitive types.
 *
 * @typedef {(String|Number|Object|Array|boolean|null)} JsonPrimitive
 */
/**
 * The type of a hook's setState(value) function's {@code value} parameter.
 * Can either be the new state value or a function that takes in the previous
 * state's value and returns the new state value.
 *
 * @typedef {(JsonPrimitive | function(prevState:JsonPrimitive):JsonPrimitive)} HookSetStateParam
 */
/**
 * A hook's setState() function, which receives a {@link HookSetStateParam} that
 * is either the new state value or a function that returns the new state value.
 *
 * @typedef {function(value:HookSetStateParam): void} HookSetStateFunction
 */


/**
 * @callback hookedChildRenderer
 * @param {(*|Array<*>)} hookReturnVal - Value returned from useMyHook()
 * @returns {React.Component} - Children to render using hookReturnVal
 */
/**
 * Component used when class components want to use hooks.
 *
 * @param {Object} props - Props for returned React.Component
 * @param {function} props.hook - Hook to use within class component
 * @param {hookedChildRenderer} props.children - Function that uses value from hook() to render children; passed as React.Component.props
 * @returns {React.Component} - Children rendered using the hook() return values
 */
export function Hooked({ hook, hookArgs, children }) {
    return children(hook(hookArgs));
}

/**
 * Reads and updates window's localStorage and sessionStorage while allowing
 * React components to re-render based on changes to the value of the stored
 * key.
 *
 * @param {String} key - Key used in storage.
 * @param {Object} options - Options for storage handling.
 * @param {(String|Number|Object|Array|boolean|null)} [options.initialValue=null] - Initial value to use if storage lacks the passed key.
 * @param {String} [options.type="local"] - Type of window storage to use ('local' or 'session').
 * @returns {[ JsonPrimitive, HookSetStateFunction ]} - Parsed state value and setState function.
 */
export function useStorage(key, { initialValue = null, type = 'local' } = {}) {
    const storage = window[`${type}Storage`];
    const functionType = typeof (() => {});

    const [ storedState, setStoredState ] = useState(() => {
        // use stored value in storage before using initial value
        const initialStoredState = storage.getItem(key);
        return initialStoredState ? JSON.parse(initialStoredState) : initialValue;
    });

    const setState = value => {
        let valueToStore = value;

        try {
            if (typeof value === functionType) {
                // normal setState functionality if function is passed
                valueToStore = value(storedState);
            }

            setStoredState(valueToStore);

            storage.setItem(key, JSON.stringify(valueToStore));
        } catch (e) {
            console.error(`Could not store value (${value}) to ${type}Storage. Error =`, e);
        }
    };

    return [ storedState, setState ];
}

/**
 * Hook to read URL query parameters and update a specific key-value pair.
 *
 * @returns {[ Object, function(key:(string|Object), val:string): void ]} -
 *          Query param key-value map, and respective setState(key, value) function.
 */
export function useQueryParams() {
    const functionType = typeof (() => {});
    const [ queryParamsObj, setQueryParamsObj ] = useState(getQueryParams());

    const setQueryParam = (key, value) => {
        if (typeof key === typeof {}) {
            const newQueryParams = { ...key };

            setQueryParamsObj(newQueryParams);
            pushQueryParamOnHistory(newQueryParams);

            return;
        }

        const newQueryParams = { ...queryParamsObj };
        let valueToStore = value;

        if (typeof value === functionType) {
            // normal setState functionality if function is passed
            valueToStore = value(queryParamsObj[key]);
        }

        newQueryParams[key] = valueToStore;
        setQueryParamsObj(newQueryParams);
        pushQueryParamOnHistory(key, valueToStore);
    };

    useEffect(() => {
        const updatedQueryParams = getQueryParams();

        if (!objEquals(queryParamsObj, updatedQueryParams)) {
            setQueryParam(updatedQueryParams);
        }
    }, [ window.location.search ]);

    return [ queryParamsObj, setQueryParam ];
}

/**
 * Custom state handler function for useWindowEvent()
 *
 * @callback handleWindowEvent
 * @param {*} prevState - Previous state
 * @param {function} setState - setState() React function
 * @param {*} newEvent - New event from window
 */
/**
 * Adds an event listener to the window and returns the associated eventState/setEventState fields.
 * Optional configurations include using a nested event field for state, setting the initial state,
 * and using a custom event handler instead of the standard setEventState(newEventState).
 *
 * @param {string} eventType - Type of event, passed to `window.addEventListener(eventType, ...)`
 * @param {string} [nestedEventField=null] - Nested event field to use as state instead of the event itself
 * @param {*} [initialEventState=null] - Initial state to use in event listener
 * @param {handleWindowEvent} [handleEvent=null] - Custom event handler to use instead of standard setEventState
 * @param {Array<*>} [useEffectInputs=[]] - useEffect optimization inputs: `useEffect(func, useEffectInputs)`
 * @returns {[ *, function ]} - event state and respective setState function
 */
export function useWindowEvent(
    eventType,
    {
        nestedEventField = null,
        initialEventState = null,
        handleEvent = null,
        useEffectInputs = [],
    } = {},
) {
    const [ eventState, setEventState ] = useState(initialEventState);
    const isUsingOwnEventHandler = typeof handleEvent === typeof (() => {});

    useEffect(() => {
        function eventListener(event) {
            const newEventState = nestedEventField ? event[nestedEventField] : event;

            if (isUsingOwnEventHandler) {
                handleEvent(eventState, setEventState, newEventState);
            } else {
                setEventState(newEventState);
            }
        }

        window.addEventListener(eventType, eventListener);

        return () => {
            window.removeEventListener(eventType, eventListener);
        };
    }, [ eventType, ...useEffectInputs ]);

    return [ eventState, setEventState ];
}

export function useKeyboardEvent(type = 'down') {
    return useWindowEvent(`key${type}`, { nestedEventField: 'key' });
}

/**
 * Get a hook state array containing the path from the clicked element to the root.
 *
 * @returns {[ [HTMLElement] | function ]} - The click path and setter function for said path
 */
export function useClickPath() {
    const [ event, setEvent ] = useWindowEvent('click');
    const clickPath = getClickPath(event);

    return [ clickPath, setEvent ]; // setEvent will be used as setClickPath
}

/**
 * A root-close hook that triggers closing an element based on if the user clicks outside the bounds
 * of the acceptable element or if they press the "Escape" key
 *
 * @param {ElementProps} acceptableElement - Element that marks the bounds of what is acceptable to click on
 * @param {ElementProps} closeElement - Element that marks the bounds of what should trigger the root close
 * @param {boolean} [escapeClosesModal=true] - If pressing the Escape key should trigger a root-close event
 * @returns {[boolean, function]} - If the user triggered the root close and the function to reset the trigger
 */
export function useRootClose(acceptableElement, closeElement, escapeClosesModal = true) {
    const [ keyDown, setKeyDown ] = useKeyboardEvent();
    const [ clickPath, setClickPath ] = useClickPath();

    const pressedEscape = escapeClosesModal && (keyDown === 'Escape');
    const clickedOnElementWithinBounds = elementIsInClickPath(acceptableElement, clickPath);
    const clickedOnElementOutsideBounds = elementIsInClickPath(closeElement, clickPath);
    const rootWasClosed = pressedEscape || (clickedOnElementOutsideBounds && !clickedOnElementWithinBounds);

    const resetRootClosed = () => {
        setKeyDown(null);
        setClickPath([]);
    };

    return [ rootWasClosed, resetRootClosed ];
}

export function useWindowResize() {
    const initialState = {
        wasResized: false,
        width: window.innerWidth,
        height: window.innerHeight,
    };

    function handleResize(prevState, setState) {
        setState({
            wasResized: true,
            width: window.innerWidth,
            height: window.innerHeight,
        });
    }

    const [ windowSizeState, setWindowSizeState ] = useWindowEvent('resize', {
        initialEventState: initialState,
        handleEvent: handleResize,
    });

    function resetWasSized() {
        setWindowSizeState(prevState => ({
            ...prevState,
            wasResized: false,
        }));
    }

    return { windowSizeState, setWindowSizeState, resetWasSized };
}

/**
 * Blocks the `document.body` from being scrollable as long as
 * the `shouldBlockScrolling` function returns true.
 *
 * @param {function(): boolean} shouldBlockScrolling - Function to determine if scrolling should be disabled
 */
export function useBlockDocumentScrolling(shouldBlockScrolling) {
    /**
     * Don't return a cleanup function to handle activating scrolling.
     *
     * React calls cleanup functions upon both component unmount
     * and component re-render.
     *
     * If re-activating scrolling were returned in the cleanup function,
     * then anytime the component re-rendered, document scrolling
     * would be re-activated, even if the `shouldBlockScrolling()` returned true.
     *
     * Thus, handle the cleanup manually in else-block.
     */
    useEffect(() => {
        if (shouldBlockScrolling()) {
            setDocumentScrolling(false);
        } else {
            setDocumentScrolling();
        }
    }, [ shouldBlockScrolling ]);
}

/**
 * Determines if the mouse is hovering over an element using JavaScript.
 * Useful for the times where JavaScript calculations need to be done,
 * where CSS `:hover` classes aren't enough.
 *
 * Optionally, `overrideBoundingClientRect` will allow the use of a different
 * `getBoundingClientRect()` object instead of the one from the returned React.ref.
 * This field will generally only be useful if you need to know if an element inside
 * an SVG is hovered over because `svgElement.getBoundingClientRect()` will return
 * a rect relative to the SVG, not the window. In this case, manual bounding-rect
 * calculations will need to be done on the SVG element to convert it from the SVG's
 * viewport to the window's.
 *
 * @param {Object} [overrideBoundingClientRect=null] - Optional `getBoundingClientRect()` result to use instead of the returned ref
 * @returns {[React.ref, boolean]} - The ref to attach to the element watching for a hover and the respective `isHovered` value
 */
export function useHover(overrideBoundingClientRect) {
    const ref = useRef(overrideBoundingClientRect);

    function handleMouseMove(prevIsHovered, setIsHovered, newEvent) {
        const { pageX, pageY } = newEvent;

        if (ref.current) {
            const { pageXOffset, pageYOffset } = window;
            let { top, bottom, left, right } = overrideBoundingClientRect || ref.current.getBoundingClientRect();

            top = top + pageYOffset;
            bottom = bottom + pageYOffset;
            left = left + pageXOffset;
            right = right + pageXOffset;

            if (pageX <= right && pageX >= left && pageY <= bottom && pageY >= top) {
                setIsHovered(true);
            } else {
                setIsHovered(false);
            }
        }
    }

    const [ isHovered ] = useWindowEvent('mousemove', {
        initialEventState: false,
        handleEvent: handleMouseMove,
        useEffectInputs: [ ref.current ],
    });

    return [ ref, isHovered ];
}

/**
 * Sets the scrolling ability of the whole `document.body`.
 * Useful for controlling the app's ability to scroll from any
 * component.
 *
 * Since `document.body` is outside of the control of React,
 * set the style manually. Default value is ''.
 *
 * @param allowScrolling
 */
export function setDocumentScrolling(allowScrolling = true) {
    document.body.style.overflow = allowScrolling ? 'auto' : 'hidden';
}
