import webpack from 'webpack';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import CopyWebpackPlugin from 'copy-webpack-plugin';
import TerserJSPlugin from 'terser-webpack-plugin';
import MiniCssExtractPlugin from 'mini-css-extract-plugin';
import CssMinimizerPlugin from 'css-minimizer-webpack-plugin';
import MockRequestsWebpackPlugin from 'mock-requests/bin/MockRequestsWebpackPlugin.js';

import {
    publicEnv,
    broadcastChannel,
} from './env.js';
import AlterFilePostBuildPlugin from './webpack/AlterFilePostBuildPlugin.mjs';
import {
    Paths,
    tsconfigPath,
    FileTypeRegexes,
    getOutputFileName,
    ImportAliases,
    LocalLanHostIpAddresses,
} from './utils/index.js';
import babelConfig from './babel.config.js';

import packageJson from '../package.json' with { type: 'json' };
import manifestJson from '../src/manifest.json' with { type: 'json' };

// TODO: https://webpack.js.org/guides/caching

// TODO Make import aliases available to npm scripts
//  Best option is likely through ts-node: https://www.npmjs.com/package/ts-node
//      Webpack docs explain how to do this:
//          https://webpack.js.org/configuration/configuration-languages/#typescript
//      Will it work with native npm scripts? See:
//          https://jonjam.medium.com/writing-npm-scripts-using-typescript-a09b8712dc6b
//          https://www.typescriptlang.org/tsconfig#module
//  Another options is through module-alias: https://www.npmjs.com/package/module-alias
//  See:
//      https://github.com/nodejs/node/pull/41552
//      https://github.com/nodejs/node/discussions/41711

const isProduction = process.env.IS_PRODUCTION;
const allowAccessFromAllOrigins = Boolean(process.env.ALLOW_CORS_ACCESS);
const useHttps = process.env.HTTPS;
const useCustomHttpsCert = process.env.CUSTOM_CERT;
const activateServiceWorker = isProduction || (publicEnv.SW_DEV === 'true');
// If this app is a library to be consumed by other apps instead of a standalone website
// TODO Update output configs to consider this option
const isLibrary = false;
const sourceMap = !isProduction; // allows for passing `sourceMap` directly by name to loaders/plugins options

const indexHtmlTitle = manifestJson.short_name;
// TODO See if index.html <link> entries can be moved here
const indexHtmlMetaTagData = {
    description: packageJson.description,
    keywords: packageJson.keywords?.join(', '),
    'theme-color': manifestJson.theme_color,
    // Could also include `minimum-scale=1`, `maximum-scale=1`, and `user-scalable=no` to prevent user scaling.
    // Note: `viewport-fit` isn't yet supported, but it has a similar effect to `width=device-width`, so add it preemptively for when support is added. See: https://udn.realityripple.com/docs/Web/CSS/@viewport/viewport-fit
    viewport: 'viewport-fit=cover, width=device-width, initial-scale=1, shrink-to-fit=no',
    'color-scheme': 'dark light',
    'apple-mobile-web-app-capable': 'yes',
    'apple-mobile-web-app-title': indexHtmlTitle,
    'apple-mobile-web-app-status-bar-style': 'black-translucent', // Maintain background color in top time/notification status bar. See: https://stackoverflow.com/questions/39749015/apple-mobile-web-app-status-bar-style-in-ios-10/40786240#40786240
};


let certConfig = {};
if (useHttps && useCustomHttpsCert) {
    // import selfSignedCert from '../.env.cert.json' assert { type: 'json' };
    const { importFile } = await import('./utils/ESM/index.mjs');
    /** @type {typeof import('./utils/Certs')} */
    const certs = await importFile('./config/utils/Certs.ts');

    certConfig = await certs.getServerHttpsCredentials();
}

const fileUrlsNotToCacheInPwa = [];

const {
    JavaScript,
    TypeScript,
    JsAndTs,
    Styles,
    Svg,
    Binaries,
    Text,
    Fonts,
} = FileTypeRegexes;

const hotReloading = process.env.HOT_RELOADING;

const svgDefaultExportReactComponent = false;


const javascriptLoaderConfig = {
    loader: 'babel-loader',
    options: {
        ...babelConfig,
        cacheDirectory: true,
    },
};
const typescriptLoaderConfig = {
    loader: 'ts-loader',
    options: {
        configFile: tsconfigPath,
        /** @type {import('typescript').CompilerOptions} */
        compilerOptions: {
            // Ensure tsconfig's `outDir` is unset when using Webpack (output is piped to babel-loader)
            outDir: null,
            // Deactivate declaration output if this isn't a library meant to be consumed, e.g. a website to be deployed
            ...(isLibrary ? {} : {
                declaration: isLibrary,
                declarationDir: isLibrary,
            }),
        },
    },
};


// noinspection WebpackConfigHighlighting
/** @returns {import('webpack/types').WebpackOptionsNormalized} */
function getWebpackConfig(webpackArgs) {
    return {
        mode: isProduction ? 'production' : 'development',
        module: {
            /**
             * Webpack uses template strings when generating output files.
             *
             * Examples:
             * '[path][name]-[contenthash:8].[ext]'   ->   `src/assets/MyImage-991ec5ea.png`
             * '[name][ext]' or '[base]'   ->   `MyImage.png`
             * Even though [ext] is supposed to contain the preceding '.', it seems [name].[ext] and [name][ext] are the same
             *
             * @see [TemplateStrings]{@link https://webpack.js.org/configuration/output/#template-strings} for more information.
             */
            /**
             * Quick note on loaders:
             *
             * Webpack@<5   |  Webpack@>=5     |  result
             * file-loader  |  asset/resource  |  outputs the file; gives a URL reference to usages in src
             * url-loader   |  asset/inline    |  no file output; converts usage in src files to Base64 data URI string
             * raw-loader   |  asset/source    |  no file output; simply injects the file contents as a string to usages in src (not duplicated with multiple imports, though)
             *
             * @see {@link https://v4.webpack.js.org/loaders/file-loader/} and related loader URLs for more information.
             */
            rules: [
                {
                    test: JsAndTs,
                    exclude: /node_modules/,
                    include: new RegExp(Paths.SRC.REL),
                    use: [
                        javascriptLoaderConfig,
                        typescriptLoaderConfig,
                    ],
                },
                {
                    test: Styles,
                    // TODO: Hot reloading doesn't seem to work given these and/or `optimization` configs
                    use: [
                        MiniCssExtractPlugin.loader,
                        {
                            loader: 'css-loader',
                            options: {
                                modules: {
                                    // Don't default to CSS-Modules; parse as normal CSS
                                    mode: 'icss',
                                },
                                importLoaders: 2,
                                sourceMap,
                            },
                        },
                        {
                            loader: 'postcss-loader',
                            options: {
                                postcssOptions: {
                                    plugins: [
                                        [ 'postcss-preset-env', {
                                            stage: 0,
                                            browsers: babelConfig.presets
                                                .find(preset => preset[0]?.match(/babel\/preset-env/i))?.[1]
                                                .targets
                                                .browsers,
                                        }],
                                    ],
                                },
                                sourceMap,
                            },
                        },
                        {
                            loader: 'sass-loader',
                            options: {
                                sourceMap,
                            },
                        },
                    ],
                },

                /**
                 * Use [Asset Modules]{@link https://webpack.js.org/guides/asset-modules/}
                 * instead of (file|url|raw)-loader since those are being deprecated and
                 * Asset Modules are built-in with webpack@5
                 */

                {
                    test: Svg,
                    oneOf: [
                        {
                            // Non-JS files, e.g. CSS
                            issuer: {
                                not: JsAndTs,
                            },
                            type: 'asset/resource',
                        },
                        {
                            // JS files specifically requesting the SVG file's URL
                            resourceQuery: /url/,
                            type: 'asset/resource',
                        },
                        {
                            // JS files wanting to use the SVG as a React component and/or URL
                            issuer: JsAndTs,
                            resourceQuery: {
                                // Only output React component if not querying for the URL, i.e. *.svg?url
                                not: [ /url/ ],
                            },
                            use: [
                                // Parse resulting React components using our Babel config, not theirs, for better code-splitting/bundling
                                // Note: This has to be done here instead of in `SVGR.options.jsx.babelConfig` because that option disables calling the `template()` function
                                // Note: Don't add `javascriptLoaderConfig`, `babel: false`, or `jsxRuntime` if using NextJS.
                                javascriptLoaderConfig,
                                {
                                    loader: '@svgr/webpack',
                                    /**
                                     * @type {import('@svgr/core/dist').Config}
                                     *
                                     * @see [SVGR options]{@link https://react-svgr.com/docs/options}
                                     * @see [Source code]{@link https://github.com/gregberge/svgr}
                                     */
                                    options: {
                                        // TODO: Investigate options used in CRA: https://github.com/facebook/create-react-app/blob/67b48688081d8ee3562b8ac1bf6ae6d44112745a/packages/react-scripts/config/webpack.config.js#L391-L398
                                        babel: false, // Use our own (more optimized) Babel config instead of theirs
                                        jsxRuntime: 'automatic', // React >= v17 doesn't need `import React from 'react'` so don't inject it
                                        exportType: 'named', // `export const ReactComponent` instead of `export default ReactComponent`
                                        ref: true,
                                        memo: true,
                                        prettier: false, // No need to minify the React output
                                        svgo: false, // Don't force-remove SVG fields we care about (see below)
                                        /**
                                         * @type {import('@svgr/core/dist').Config.svgoConfig}
                                         *
                                         * @see [SVGO config options]{@link https://github.com/svg/svgo#built-in-plugins}
                                         */
                                        svgoConfig: {
                                            // removeDoctype: false, // <DOCTYPE>
                                            // removeXMLProcInst: false, // <?xml version="1.0" encoding="utf-8"?>
                                            // removeComments: false,
                                            removeXMLNS: false, // `xmlns` prop
                                            removeMetadata: false, // <metadata>
                                            removeTitle: false, // <title>
                                            removeDesc: false, // <desc>
                                            removeUselessDefs: false, // <defs> that don't contain an `id` prop
                                            removeEditorsNSData: false,
                                            removeEmptyAttrs: false,
                                            removeHiddenElems: false,
                                            removeEmptyText: false,
                                            removeEmptyContainers: false,
                                            removeViewBox: false,
                                        },
                                        /**
                                         * Template string for generating React component source code output.
                                         *
                                         * Customizing it here allows us to avoid having to use `resourceQuery: /url/` in our Webpack config,
                                         * meaning that source code won't have to specify `file.svg` to import the React component or `file.svg?url`
                                         * to import the file's URL. Now, both can be imported in the same statement just like `@svgr/webpack` v5 did.
                                         *
                                         * @type {import('@svgr/babel-plugin-transform-svg-component/dist').Template}
                                         *
                                         * @see [Default template source code]{@link https://github.com/gregberge/svgr/blob/755bd68f80436130ed65a491c101cf0441d9ac5e/packages/babel-plugin-transform-svg-component/src/defaultTemplate.ts}
                                         * @see [Working with TypeScript]{@link https://github.com/gregberge/svgr/issues/354}
                                         */
                                        template(componentInfo, svgrConfig) {
                                            const { tpl: babelTemplateBuilder } = svgrConfig;

                                            const svgSrcFilePath = svgrConfig.options.state.filePath;
                                            const svgSrcImportAliasPath = ImportAliases.getBestImportAliasMatch(svgSrcFilePath);

                                            /**
                                             * SVGR does its own AST parsing before giving the user access to it. This means:
                                             *
                                             * - The `componentInfo` entries are AST objects.
                                             * - We cannot call the template-builder as a function (with parentheses), it must use
                                             *   the template-string syntax (template`myTemplate`).
                                             * - We cannot add imports or exports because SVGR does a validation comparison with its own AST
                                             *   (technically we could but then we'd be manually editing AST objects, which is always a bad idea).
                                             *
                                             * Thus, use a simple, logic-only template string so that it coincides with the AST within SVGR,
                                             * and then append our own changes to the generated AST array afterwards so that our changes are
                                             * still parsed and injected into the resulting code.
                                             *
                                             * @see [Source code]{@link https://github.com/gregberge/svgr/blob/755bd68f80436130ed65a491c101cf0441d9ac5e/packages/babel-plugin-transform-svg-component/src/index.ts#L30}
                                             */

                                            /*
                                             * Add the ability to pass `children` through to the generated React component.
                                             * `@svgr/webpack` doesn't allow this by default, so we must add it ourselves.
                                             * However, since it uses a JSX AST tree, we can't just add it as a normal string
                                             * like we did for the double export of both asset URL and React component after
                                             * the SVG component's AST tree generation.
                                             *
                                             * After many attempts, I've found:
                                             *  - Babel will add a semicolon to the end of this specific expression no matter
                                             *    what. This is fine if done *outside* the generated code string injected
                                             *    because SVG DOM elements won't render it.
                                             *  - We can't pass in '{props.children}' as a plain string, otherwise the
                                             *    semicolon is added inside the curly braces, creating invalid JSX syntax.
                                             *  - We can't use `{componentInfo.props.children}` because that AST content is
                                             *    generated from the .svg file itself, so it won't read dynamically added
                                             *    children from usage in src code.
                                             *  - We can't use Babel's standard `%%foo%%` substitution pattern because
                                             *    `@svgr/webpack` disables it and can't process it even with using our own
                                             *    Babel AST template builder.
                                             *  - We possibly might be able to use a function but that's no better than
                                             *    this plain string.
                                             *  - The rules above apply to `<>{my-code}</>` as well. We choose not to use
                                             *    React.Fragment since it isn't required and for simpler usage in parent
                                             *    component logic that uses SVG React components from this loader.
                                             */
                                            componentInfo.jsx.children.push(babelTemplateBuilder`
                                                ${'{props.children}'}
                                            `);

                                            // Logic only AST template that uses the React component info content from SVGR.
                                            const astArray = babelTemplateBuilder`
                                                ${componentInfo.imports};

                                                ${componentInfo.interfaces};

                                                function ${componentInfo.componentName}(${componentInfo.props}) {
                                                    return (
                                                        ${componentInfo.jsx}
                                                    );
                                                }

                                                ${componentInfo.componentName}.displayName = '${componentInfo.componentName}';

                                                ${componentInfo.exports};
                                                `;

                                            // Our own logic containing custom imports/exports
                                            const customAstArray = babelTemplateBuilder(`
                                                import SvgAssetUrl from '${svgSrcImportAliasPath}?url';

                                                // URL of the actual SVG file
                                                export const SvgUrl = SvgAssetUrl;

                                                // Add default export for ease of use
                                                export default ${svgDefaultExportReactComponent ? componentInfo.componentName : 'SvgUrl'};
                                            `);

                                            astArray.push(...customAstArray);

                                            return astArray;
                                        },
                                    },
                                },
                            ],
                        },
                    ],
                },
                {
                    test: Binaries,
                    type: 'asset/resource',
                    /** @type {import('webpack/types').AssetResourceGeneratorOptions} */
                    generator: {
                        // Webpack docs don't include all these fields in any of its GeneratorOptionsByModuleTypeKnown
                        // entries so specify them manually in case they're needed in future use
                        filename: ({
                            /** @type {import('webpack/types').NormalModule} */
                            module,
                            /** @type {string} */
                            runtime,
                            /** @type {string} */
                            filename,
                            /** @type {import('webpack/lib/ChunkGraph.js').ChunkGraphChunk} */
                            chunkGraph,
                            /** @type {string} */
                            contentHash,
                        }) => {
                            /*
                             * Maintain nested directory structure when generating output file names while
                             * also removing the beginning `src/` from the output path.
                             *
                             * Exception: Favicon files, which should be in the root of the output directory
                             * and should not contain hashes.
                             * TODO Dynamically generate manifest.json's favicon entries from webpack hash so
                             *  that new favicon versions are served rather than the old/cached version.
                             */
                            const faviconFileNames = [ 'favicon', 'apple-touch-icon' ];
                            const faviconRegex = new RegExp(`(${faviconFileNames.join('|')})`);

                            if (faviconRegex.test(filename)) {
                                return getOutputFileName(filename, {
                                    hashLength: 0,
                                    maintainFolderStructure: false,
                                    nestInFolder: '',
                                });
                            }

                            return getOutputFileName(filename);
                        },
                    },
                },
                {
                    test: Fonts,
                    type: 'asset/resource',
                    generator: {
                        filename: ({ filename }) => {
                            /*
                             * Don't append hash to font file outputs so that the SCSS
                             * mixin can work with the direct file name.
                             */
                            return getOutputFileName(filename, { hashLength: 0 });
                        },
                    },
                },
                {
                    test: Text,
                    type: 'asset/source',
                },
            ],
        },
        resolve: {
            extensions: [ '.ts', '.tsx', '.js', '.jsx', '.*' ],
            modules: [
                // Paths.SRC.ABS, // allows treating src/* dirs/files as modules, i.e. `import X from 'dirUnderSrc/nested/File.ext';`. Unnecessary since src/* has been aliased to `/` and `@/`.
                'node_modules',
            ],
            alias: ImportAliases.toCustomObject({
                pathMatchModifier: (pathMatchRegexString, pathMatchArray) => pathMatchArray
                    .map(pathMatch => Paths.getFileAbsPath(Paths.ROOT.ABS, pathMatch)),
            }),
        },
        entry: {
            client: {
                import: [
                    // If supporting IE, ensure `core-js` polyfills are loaded before source/vendor code
                    ...(process?.env?.npm_package_config_supportIe ? [ 'core-js' ] : []),
                    Paths.getFileAbsPath(Paths.SRC.ABS, 'index.jsx'),
                ],
                dependOn: 'common',
            },
            common: [
                /*
                 * Polyfills not covered with core-js include:
                 * fetch, Proxy, BigInt, Intl, String.prototype.normalize, among others
                 * See: https://github.com/zloirock/core-js#missing-polyfills
                 */
                'isomorphic-fetch',
                'reflect-metadata',
                'proxy-polyfill',
            ],
        },
        output: {
            // library: {
            //     name: indexHtmlTitle.replace(/\s/g, ''),
            //     type: 'umd',
            //     umdNamedDefine: true, // Names the library for AMD modules
            //     export: [ 'default' ],
            // },
            path: Paths.BUILD_ROOT.ABS, // output path for webpack build on machine, not relative paths for index.html
            filename: `${Paths.BUILD_OUTPUT.REL}/js/[name].[contenthash:8].bundle.js`,
            chunkFilename: `${Paths.BUILD_OUTPUT.REL}/js/[name].[contenthash:8].chunk.js`,
            /**
             * Default output name for [Asset Modules]{@link https://webpack.js.org/guides/asset-modules/}.
             * Will be overridden by any `module.rule` that specifies `generator.filename`.
             *
             * @see [output.assetModuleFilename]{@link https://webpack.js.org/configuration/output/#outputassetmodulefilename}
             */
            assetModuleFilename: `${Paths.BUILD_OUTPUT.REL}/assets/[name].[contenthash:8][ext]`,
            sourceMapFilename: '[file].map',
            environment: {
                // toggle options for output JS target browsers; to target ES5, set all to false
                arrowFunction: false,
                bigIntLiteral: false, // BigInt as literal (123n)
                const: false, // const/let
                destructuring: false, // var { a, b } = obj;
                dynamicImport: false, // import()
                forOf: false,
                module: false, // import X from 'X';
            },
        },
        plugins: [
            // Makes environment variables available to source code through the specified key.
            // Use `webpack.DefinePlugin.runtimeValue()` to force re-compilation on file change, which
            // can be very useful for back-end file changes that aren't already in the compilation file-watch
            // list like source code is; See: https://webpack.js.org/plugins/define-plugin/#runtime-values-via-runtimevalue
            new webpack.DefinePlugin({ 'process.env': JSON.stringify(publicEnv) }),
            // injects tags like <script> into index.html
            new HtmlWebpackPlugin({
                title: indexHtmlTitle,
                template: Paths.getFileAbsPath(Paths.SRC.ABS, 'index.html'),
                meta: indexHtmlMetaTagData,
            }),
            // Adds specific matcher regex(es) for dynamic imports to tell them where to look when string
            // variables, template strings, and related non-static strings are used as args for dynamic imports.
            // In this case, allows the `src/assets/` directory to be searched for dynamic imports passed by
            // filename instead of import path.
            new webpack.ContextReplacementPlugin(
                /([./\\]*)|(.*\/src)|(@)\/assets\/.*/i,
                true,
            ),
            // Adds `mock-requests` as an entry file for automatic network mocks from CLI and in tests
            new MockRequestsWebpackPlugin(
                Paths.MOCKS.REL,
                'MockConfig.js',
                publicEnv.MOCK === 'true',
            ),
            // splits CSS out from the rest of the code
            new MiniCssExtractPlugin({
                filename: hotReloading ? '[name].css' : `${Paths.BUILD_OUTPUT.REL}/css/[name].[contenthash:8].css`,
                chunkFilename: hotReloading ? '[id].css' : `${Paths.BUILD_OUTPUT.REL}/css/[name].[contenthash:8].chunk.css`,
            }),
            // manually copies files from src to dest
            new CopyWebpackPlugin({
                patterns: [
                    {
                        from: `${Paths.ROOT.ABS}/package.json`,
                        to: '[name].[ext]',
                    },
                    {
                        from: `${Paths.SRC.REL}/manifest.json`,
                        to: '[name].[ext]',
                    },
                    {
                        from: `${Paths.SRC.REL}/ServiceWorker.js`,
                        to: '[name].[ext]',
                    },
                    // Use this if using Ionic or similar that doesn't automatically copy favicons from `module.rules.Binaries`
                    // {
                    //     from: `${Paths.SRC.REL}/assets/favicon*`,
                    //     to: '[name].[ext]',
                    // },
                    {
                        // Ensures CNAME is copied to the build-output dir for gh-pages and similar deployments
                        // CopyWebpackPlugin uses globs, so make CNAME optional via `?(filename)`
                        from: `${Paths.ROOT.ABS}/CNAME`,
                        to: '[name].[ext]',
                        noErrorOnMissing: true,
                    },
                ],
            }),
            new AlterFilePostBuildPlugin(
                'ServiceWorker.js',
                /urlsToCache ?= ?\[\]/g,
                relativeEmittedFilePaths => {
                    // CNAME (and similar files) aren't accessible via URL, and `cache.addAll(urls)` will fail if any
                    // of the URLs isn't available, so remove them from the build output file list.
                    // Likewise, service workers ignore `fetch()` requests to themselves, so no need to cache it either.
                    const pathsWithoutUncacheableFiles = relativeEmittedFilePaths.filter(path => (
                        !path.includes('ServiceWorker.js')
                        && !path.match(/CNAME|LICENSE/)
                    ));
                    const fileUrlsToCache = pathsWithoutUncacheableFiles.map(path => `"./${path}"`); // ServiceWorker exists at root level

                    // `/` isn't a file but is routed to /index.html automatically.
                    // Add it manually so the URL can be mapped to a file.
                    fileUrlsToCache.push('"./"');

                    return `urlsToCache=[${fileUrlsToCache.join(',')}]`;
                },
                activateServiceWorker,
            ),
            new AlterFilePostBuildPlugin(
                'ServiceWorker.js',
                /urlsNotToCache ?= ?\[\]/g,
                `urlsNotToCache=[${fileUrlsNotToCacheInPwa
                    .map(url => url instanceof RegExp ? url : `"./${url}"`)
                    .join(',')
                }]`,
                activateServiceWorker,
            ),
            new AlterFilePostBuildPlugin(
                'ServiceWorker.js',
                'VERSION',
                packageJson.version,
                activateServiceWorker,
            ),
            new AlterFilePostBuildPlugin(
                'ServiceWorker.js',
                'BRD_CHANNEL',
                broadcastChannel,
                activateServiceWorker,
            ),
        ],
        optimization: {
            moduleIds: 'deterministic', // Prevent arbitrary moduleId incrementing, i.e. if the content hasn't changed, don't change the file's hash due to moduleId++. See: https://webpack.js.org/guides/caching/#module-identifiers
            minimize: isProduction,
            usedExports: true, // Tree-shaking of unused exports based on Terser's ability to parse exported functions and their usage. True by default in 'production' mode.
            minimizer: [
                new TerserJSPlugin(),
                new CssMinimizerPlugin({
                    minimizerOptions: {
                        preset: 'default', // discards non-important comments, removes duplicates, etc.
                        discardComments: {
                            // removeAll: true // also remove /*! comments
                        },
                    },
                }),
            ],
            splitChunks: {
                chunks: 'all',
                // maxSize: 700000,  // Max file size of any chunk unless overridden below -- NOTE: Test multiple values since sometimes setting this can ironically bloat the output file sizes
                cacheGroups: {
                    /*
                     * Splits node_modules packages (as 'vendor') from src (as 'client').
                     * Without `splitChunks[cacheGroups[i]]?.maxSize`, this would merge all dependency packages into one
                     * single `vendor.js` file. `maxSize` keeps the file from getting too big.
                     *
                     * See:
                     *  - https://stackoverflow.com/questions/65858859/how-to-code-split-webpacks-vendor-chunk/70627948#70627948
                     */
                    vendor: {
                        test: /[\\/]node_modules[\\/]/,
                        name: 'vendor',
                        chunks: 'all',
                        // Set smaller max file size for dependencies since they change less frequently.
                        // This way, upon change to >= 1, the unchanged ones are more likely to be served from cache rather
                        // than being re-built.
                        maxSize: 200000,
                    },
                    // Split up stylesheets
                    styles: {
                        test: Styles,
                        type: 'css/mini-extract', // Suggested by the docs, though `type` technically only has value within individual plugins' use of the string. See: https://github.com/webpack-contrib/mini-css-extract-plugin#extracting-css-based-on-entry
                        name: 'styles',
                        chunks: 'all',
                        // Collect all CSS into a single file since the separated CSS files contained only duplicate code.
                        // This is either a bug or was originally caused by the old format of using `@import` in SCSS files
                        // instead of `@use`, where the latter supposedly prevents output CSS from being copy-pasted into
                        // each file that uses it.
                        // Requires more testing to find out.
                        // See bug: https://github.com/webpack-contrib/mini-css-extract-plugin/issues/52
                        enforce: true,
                    },
                },
            },
            runtimeChunk: {
                name: 'runtime',
            },
        },
        /**
         * Cache builds on the filesystem.
         * Dramatically speeds up builds, but not necessarily dev-server compilations.
         *
         * @see [Cache]{@link https://webpack.js.org/configuration/cache/}
         */
        cache: {
            type: 'filesystem',
            compression: 'gzip',
        },
        performance: {
            hints: false, // disable "entrypoint size limit" warning
        },
        /**
         * @see [Stats structure]{@link https://webpack.js.org/api/stats}
         * @see [Stats configuration]{@link https://webpack.js.org/configuration/stats}
         */
        stats: {
            builtAt: true, // Show final time build completed
            assets: true, // Show assets generated/output from other plugins, e.g. HtmlWebpackPlugin, CopyWebpackPlugin, etc.
            modules: true, // Show generated modules by runtime, orphan, path, etc., e.g. node_modules/, src/, src/path/to/MyStyles.scss, etc.
            children: false, // Not completely sure what this is exactly, but appears to be sub-compilers like HtmlWebpackPlugin
            nestedModules: true, // Show nested modules in addition to top-level modules
            moduleAssets: true, // Show assets inside of modules
            entrypoints: true, // Show entrypoint name, size, num assets included, and similar information
            groupAssetsByChunk: true, // Group assets by the parent chunk file

            // Allow SCSS debugging output: https://webpack.js.org/loaders/sass-loader/#how-to-enable-debug-output
            loggingDebug: [ 'sass-loader' ],
        },
        devtool: sourceMap ? 'source-map' : false,
        /** @type {import('webpack-dev-server').Configuration} */
        devServer: {
            port: LocalLanHostIpAddresses.port,
            open: true, // open browser window upon build
            hot: hotReloading, // for `module.hot` hot-reloading block in index.js
            historyApiFallback: true, // Fall back to index.html upon 404
            /** @type {import('https').ServerOptions} */
            server: { // HTTPS configs if not using HTTP
                type: useHttps ? 'https' : 'http',
                options: {
                    ...certConfig,
                },
            },
            devMiddleware: {
                // Specify Webpack output only for the dev-server "emitted" output
                stats: {
                    preset: 'minimal', // Greatly reduce output info by extending 'minimal' preset, overwriting options in root-level `stats`
                    builtAt: true,
                },
            },
            client: {
                overlay: true, // show full-screen display of compiler errors
                // progress: true, // show compilation progress in browser console when webpack is (re-)compiling
            },
            proxy: [{
                context: '/**',
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            }],
        },
    };
}

export default getWebpackConfig;
