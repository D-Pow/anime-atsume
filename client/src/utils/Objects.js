/**
 * A class that provides basic initialization, getters, setters, and overridden `Object.prototype` methods for better
 * support/representation of internal fields, both when the class is instantiated and extended.
 *
 * Note: Functions (regardless of static or instance) aren't included in `Object.(keys|values|entries)` and object spreads,
 * meaning the class/object itself can be used directly in spreads/Object functions instead of having to filter out the class' functions.
 *
 * i.e. Class (static and instance) functions mimic:
 * ```
 * Object.defineProperty(obj, key, {
 *     configurable: false,
 *     enumerable: false,
 *     writable: false,
 *     value: 'someValue',
 * });
 * ```
 *
 * @example <caption>Set some entries and get actually-useful results when using `Object` methods.</caption>
 * const obj = new CustomizableObject({ a: 'A' });
 * obj.has('a'); // true
 * obj.set('b', 'B'); // { a: 'A', b: 'B' }
 * obj.append('b', 'BB'); // { a: 'A', b: [ 'B', 'BB' ] }
 * obj.append('c', 'C'); // { a: 'A', b: [ 'B', 'BB' ], c: 'C' }
 * obj.set('b', 'BBB'); // { a: 'A', b: 'BBB', c: 'C' }
 * obj.(keys|entries|length|map|reduce)(); // Same as `Object.(func)()`
 * obj.toString(); // [object ${this.constructor.name}]
 * obj.toJSON([key]); // {...obj}[key] || {...obj}
 */
export class CustomizableObject {
    constructor(init = {}) {
        Object.entries(init).forEach(([ key, value ]) => this[key] = value);
    }


    /* Getters and setters */

    has(key) {
        return this.hasOwnProperty(key);
    }

    get(key) {
        return this[key];
    }

    set(key, value) {
        this[key] = value;

        return this;
    }

    append(key, value) {
        if (this[key]) {
            if (Array.isArray(this[key])) {
                this[key].push(value);
            } else {
                this[key] = [ this[key], value ];
            }
        } else {
            this[key] = value;
        }

        return this;
    }

    delete(key) {
        delete this[key];

        return this;
    }

    clear() {
        this.keys().forEach(key => {
            delete this[key];
        });

        return this;
    }

    key(index) {
        return Object.keys(this)[index] || null;
    }

    get length() {
        return Object.keys(this).length;
    }


    /* Shorthands for array/util methods */

    keys() {
        return Object.keys(this);
    }

    values() {
        return Object.values(this);
    }

    entries() {
        return Object.entries(this);
    }

    forEach(func) {
        this.entries().forEach((keyValueArrayPair, index) => {
            func(keyValueArrayPair, index, this);
        });

        return this;
    }

    map(func) {
        return this.entries().map((keyValueArrayPair, index) => {
            return func(keyValueArrayPair, index, this);
        });
    }

    reduce(func, initialValue) {
        return this.entries().reduce((prevValue, keyValueArrayPair, index) => {
            return func(prevValue, keyValueArrayPair, index, this);
        }, initialValue);
    }

    // TODO Extract `sortObjects()` internal logic to separate function for customization here
    sort(compareFunc = (key1, key2) => `${key1}`.localeCompare(`${key2}`)) {
        const sortedEntries = this.entries().sort(([ key1, val1 ], [ key2, val2 ]) => compareFunc(key1, key2));

        this.clear();

        sortedEntries.forEach(([ key, value ]) => {
            this.set(key, value);
        });
    }


    /* Primitives, iterators, etc. */

    toString() {
        return this[Symbol.toPrimitive](typeof '');
    }

    /**
     * Returns the object to be passed to `JSON.stringify()`.
     *
     * `key` represents the key the object is nested in when called by the parent,
     * e.g.
     *   - JSON.stringify(myClass); // key == null
     *   - JSON.stringify({ myKey: myClass }); // key == 'myKey'
     *   - JSON.stringify([ 'a', myClass ]); // key == 1
     *
     * Thus, return the specified key only if it matches a key stored by the
     * calling parent.
     *
     * @param {(string|number)} key - Key this class is nested under when called by the
     *                                the parent's `JSON.stringify()`.
     * @returns {Object} - The class' contents.
     *
     * @see [MDN docs]{@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/JSON/stringify#tojson_behavior}
     * @see [Related StackOverflow post]{@link https://stackoverflow.com/questions/20734894/difference-between-tojson-and-json-stringify}
     */
    toJSON(key) {
        if (this.get(key)) {
            return this.get(key);
        }

        return this[Symbol.toPrimitive]();
    }

    /**
     * Returns the requested primitive representation of the class based on
     * the USAGE of the class, not the methods called on it.
     *
     * `Symbol.toPrimitive` gives you fine-grained control over what is returned
     * based on how it's used by the parent, i.e. operations using the class, not
     * specific methods called on your class.
     * How the parent uses your class is informed by the JSON primitive value passed
     * into the `requestedType` parameter, e.g.:
     *     console.log(`${myClass}`); // requestedType === 'string'
     *     console.log(3 + myClass);  // requestedType === 'number'
     *
     * Functions like `toString()` are sort of wrappers around this method, but
     * with two caveats:
     *     - If the method is called directly (`console.log(myClass.toString())`),
     *       then the explicit `toString()` method would be called, even if it's not
     *       defined (in which case, it'd travel up the inheritance chain, up to `Object`).
     *     - If the method is not defined, then `Symbol.toPrimitive` is defaulted to,
     *       again, only if the method isn't called explicitly.
     *
     * Thus, this only works for class usage, not for specific class function calls.
     * If the parent explicitly calls `myClass.toString()`, then this function won't
     * be called at all.
     *
     * Can also be used to overwrite other custom functions, like [valueOf()]{@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/valueOf}.
     * (Note that `valueOf()` should probably not be overridden since it will usually
     * add the class name followed by a representation of its internal structure, as told
     * by `Object.getOwnPropertyDescriptors()`)
     *
     * @param {string} requestedType - Type the class was casted to by the parent.
     * @returns {*} - Casted type of the class.
     * @see [Symbol.toPrimitive]{@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Symbol/toPrimitive}
     */
    [Symbol.toPrimitive](requestedType) {
        if (requestedType === typeof '') {
            return `[object ${this.constructor.name}]`;
        }

        if (requestedType === typeof 0) {
            return this.length;
        }

        return JSON.parse(JSON.stringify(this));
    }

    /**
     * Returns a generator/iterator for use in array spreading.
     *
     * Note:
     *   - function* myGenerator() {...}  --  Returns an iterator
     *                                      (wrapper around `return { next: ..., done: bool };`).
     *   - yield <X>  --  Returns `X` as the next value of the iterator's `.next().value` call.
     *   - yield* <iterable>  --  Forwards the `yield` return to another iterable.
     *
     * Final result:
     *   - Use `Symbol.iterator` to mark that this method is to be called when iterating over the class instance.
     *   - Mark it as a generator so we don't have to manually implement `next()`/`done` values.
     *   - Forward the iterator values for each iteration to `Object.entries()` to handle class field iteration automatically.
     *   - Use `return` to automatically signal the end of the `yield` sequence, i.e. mark `done: true`.
     *
     * Note: It seems it's not possible to override object spreading logic at this time (see [this SO post]{@link https://stackoverflow.com/questions/68631046/how-to-override-object-destructuring-for-es-class-just-like-array-destructuring}).
     * As such, object spreading will simply enumerate over the public class instance variables,
     * including arrow functions (since they're bound in the constructor under the hood) but not normal functions.
     *
     * @returns {Generator<string, string>}
     * @see [Iterators and Generators]{@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Iterators_and_Generators}
     * @see [yield* delegation to other iterables]{@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/yield*}
     */
    *[Symbol.iterator]() {
        return yield* Object.entries(this);
    }

    static *[Symbol.iterator]() {
        // `this == CustomizableObject` when static and `this == CustomizableObject.prototypeInstance` when not
        return yield* Object.entries(this);
    }
}

/**
 * Checks if all fields passed into the function exist nested
 * inside each other. This does not check if multiple
 * fields exist in a given level inside the object, only that fields
 * exist inside other fields.
 * If a field exists, but is null or undefined, then this will
 * return false.
 *
 * @param {Object} obj - Object to check validity of nested fields, e.g. network response or ref
 * @param {...string} nestedFields - Spread of nested fields to check in order
 * @returns {boolean} If the obj contains all given nested fields and they are not null/undefined
 */
export function validateObjNestedFields(obj, ...nestedFields) {
    const fieldsArray = (nestedFields[0] instanceof Array) ? nestedFields[0] : nestedFields;
    const responseExists = obj != null;

    if (fieldsArray.length === 0) {
        return responseExists;
    }

    const nextField = fieldsArray[0];

    return (
        responseExists
        && obj.hasOwnProperty(nextField)
        && validateObjNestedFields(obj[nextField], fieldsArray.slice(1))
    );
}

/**
 * Attempts to parse an object into a vanilla JavaScript object literal.
 *
 * @param {*} obj - Any type of object
 * @returns {(Object|*)} - Vanilla JavaScript object literal or original object on failure
 */
export function attemptParseObjLiteral(obj) {
    try {
        return JSON.parse(JSON.stringify(obj));
    } catch {
        return obj;
    }
}

/**
 * Deep-copies an object. Prevents pointers from being reused/changes being shared
 * between the passed object and returned obj.
 *
 * Only works for objects comprised of JSON standard variables
 * (booleans, strings, numbers, objects, arrays, and null).
 *
 * @param {Object} obj - Object to copy
 * @returns {Object} - Deep-copied object
 */
export function deepCopyObj(obj) {
    return JSON.parse(JSON.stringify(obj));
}

/**
 * Determines the differences between two objects, returning all nested
 * paths of differences.
 *
 * Ignores prototypes and inheritance.
 *
 * If arguments are not objects or arrays, then '.' will be returned if they differ.
 *
 * @param {Object} obj1 - 1 of 2 objects to be compared
 * @param {Object} obj2 - 2 of 2 objects to be compared
 * @param {boolean} [showArrayIndex=true] - If the index of arrays should be included in diff set
 * @returns {Set<string>} - Set showing paths to nested differences
 */
export function diffObjects(obj1, obj2, showArrayIndex = true) {
    // object literals, class instances, arrays, and functions
    const areBothRealObjects = (a, b) => ((a instanceof Object) && (b instanceof Object));
    const functionType = typeof (diffObjects);
    const areBothFunctions = (a, b) => ((typeof a === functionType) && (typeof b === functionType));
    const areBothArrays = (a, b) => (Array.isArray(a) && Array.isArray(b));

    const differences = [];

    function handleDifferentTypes(a, b, key) {
        if (typeof a !== typeof b) {
            differences.push(key);

            return true;
        }

        return false;
    }

    function handleNonObjects(a, b, key) {
        if (!areBothRealObjects(a, b)) {
            // anything not a "real" object:
            // strings, numbers, booleans, null, undefined, and symbols
            if (a !== b) {
                differences.push(key);
            }

            return true;
        }

        return false;
    }

    function handleFunctions(a, b, key) {
        if (areBothFunctions(a, b)) {
            if (a.toString() !== b.toString()) {
                differences.push(key);
            }

            return true;
        }

        return false;
    }

    function handleArrays(a, b, key) {
        if (areBothArrays(a, b)) {
            for (let i = 0; (i < a.length || i < b.length); i++) {
                const indexKey = `[${i}]`;
                let nestedKey = `${key}` + (showArrayIndex ? indexKey : '');

                if (key === '.') {
                    // force top-level call to show root index if array
                    nestedKey = indexKey;
                }

                handleAllValues(a[i], b[i], nestedKey);
            }

            return true;
        }

        return false;
    }

    function handleObjects(a, b, key) {
        const parentKeyPath = key === '.'
            ? '' // don't add '.' to top-level call for objects, and clear original '.'
            : `${key}.`; // add '.' after previous path to show it's a key from a parent object
        const allKeysForBothObjects = new Set(Object.keys(a).concat(Object.keys(b)));

        allKeysForBothObjects.forEach(nestedKey => {
            const value1 = a[nestedKey];
            const value2 = b[nestedKey];
            const nestedKeyPath = `${parentKeyPath}${nestedKey}`;

            handleAllValues(value1, value2, nestedKeyPath);
        });

        return true;
    }

    function handleAllValues(a, b, key) {
        return (
            handleDifferentTypes(a, b, key)
            || handleNonObjects(a, b, key)
            || handleFunctions(a, b, key)
            || handleArrays(a, b, key)
            || handleObjects(a, b, key)
        );
    }

    handleAllValues(obj1, obj2, '.'); // '.' is safety check in case non-(array|object) args are passed

    return new Set(differences);
}

/**
 * Determines if two objects are equal.
 * Ignores prototypes and inheritance.
 *
 * @param {Object} obj1 - 1 of 2 objects to be compared
 * @param {Object} obj2 - 2 of 2 objects to be compared
 * @returns {boolean} - If the stringified version of obj1 equals that of obj2
 */
export function objEquals(obj1, obj2) {
    return diffObjects(obj1, obj2).size === 0;
}

/**
 * Determines if a given variable is an object.
 *
 * @param {*} variable - Variable to check if it's an object
 * @param {{}} options - What to include in is-object check
 * @param {boolean} [options.includeClasses=true] - If native/custom JavaScript class instances should return true.
 * @param {boolean} [options.includeArrays=false] - If arrays should return true.
 *                                                  If this is true, classes will be included.
 * @param {boolean} [options.includeFunctions=false] - If functions should return true.
 *                                                     If this is true, classes will be included.
 * @param {boolean} [options.includeNull=false] - If null should return true.
 * @returns {boolean} - If the variable is an object as described by the passed options.
 */
export function isObject(variable, {
    includeClasses = true,
    includeArrays = false,
    includeFunctions = false,
    includeNull = false,
} = {}) {
    /**
     * JS variable breakdown:
     *
     *   Variable      |  typeof     |  obj.toString.call()  |  JSON.stringify  |  instanceof Object
     *   --------------|-------------|-----------------------|------------------|-------------------
     *   string        |  string     |  [object String]      |  variable        |  false
     *   number        |  number     |  [object Number]      |  variable        |  false
     *   boolean       |  boolean    |  [object Boolean]     |  variable        |  false
     *   null          |  object     |  [object Null]        |  variable        |  false
     *   undefined     |  undefined  |  [object Undefined]   |  variable        |  false
     *   symbol        |  symbol     |  [object Symbol]      |  undefined       |  false
     *   object        |  object     |  [object Object]      |  variable        |  true
     *   array         |  object     |  [object Array]       |  variable        |  true
     *   function      |  function   |  [object Function]    |  undefined       |  true
     *   native class  |  object     |  [object ClassName]   |  {varies}        |  true
     *   custom class  |  object     |  [object Object]      |  {varies}        |  true
     */
    if (variable == null) {
        return (includeNull && variable !== undefined);
    }

    const isObjectLike = variable instanceof Object;
    const isObjectLiteral = Object.getPrototypeOf(variable) === Object.getPrototypeOf({});
    const isFunction = typeof variable === typeof (() => {});
    const isArray = Array.isArray(variable);

    const checks = [ isObjectLike ];

    if (!includeFunctions) {
        checks.push(!isFunction);
    }

    if (!includeArrays) {
        checks.push(!isArray);
    }

    const objectLikesAreAcceptable = (includeFunctions || includeArrays);

    if (!objectLikesAreAcceptable && !includeClasses) {
        checks.push(isObjectLiteral);
    }

    return checks.every(bool => bool);
}
