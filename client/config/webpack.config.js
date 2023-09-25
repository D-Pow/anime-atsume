const path = require('path');
const webpack = require('webpack');
const dotenv = require('dotenv');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const TerserJSPlugin = require('terser-webpack-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const MockRequestsWebpackPlugin = require('mock-requests/bin/MockRequestsWebpackPlugin');

const tsconfig = require('../tsconfig.json');
const packageJson = require('../package.json');
const babelConfig = require('./babel.config.js');

const paths = { // resolved relative to root dir since that's where the initial npm script is run
    root: path.resolve('./')
};

const indexHtmlTitle = 'Anime Atsume';
const indexHtmlMetaTagData = {
    description: packageJson.description,
    keywords: packageJson.keywords.join(', '),
    'theme-color': '#007bff'
};

// output path for webpack build on machine, not relative paths for index.html
const relativeBuildOutputPaths = {
    development: '',
    production: packageJson.config.buildOutputDir,
};
const relativeBuildOutputPath = process.env.NODE_ENV === 'production' ? relativeBuildOutputPaths.production : relativeBuildOutputPaths.development;
const absoluteBuildOutputPath = path.resolve(paths.root, relativeBuildOutputPath);
const transpiledSrcOutputPath = 'static'; // directory of build output files relative to index.html

const env = dotenv.config({
    path: paths.root + '/.env'
}).parsed;

process.env = {
    ...process.env,
    ...env,
    NODE_ENV: process.env.NODE_ENV || 'development',
    PUBLIC_URL: transpiledSrcOutputPath,
    NODE_PATH: 'src/'
};

const publicEnv = {
    NODE_ENV: process.env.NODE_ENV,
    NODE_PATH: process.env.NODE_PATH,
    PUBLIC_URL: process.env.PUBLIC_URL,
    MOCK: process.env.MOCK
};

const isProduction = process.env.NODE_ENV === 'production';
const sourceMap = !isProduction; // allows for passing `sourceMap` directly by name to loaders/plugins options

const jsRegex = /\.jsx?$/;
const tsRegex = /\.tsx?$/;
const jsAndTsRegex = /\.[jt]sx?$/;
const scssRegex = /\.s?css$/;
const assetRegex = /\.(png|gif|jpe?g|ico|pdf|tex)$/;
const svgRegex = /\.svg$/;
const fontRegex = /\.(ttf|woff2?|eot)$/;

const hotReloading = false; // process.env.NODE_ENV === 'development';

module.exports = {
    module: {
        rules: [
            {
                test: jsRegex,
                include: /src/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: babelConfig
                }
            },
            {
                test: tsRegex,
                include: /src/,
                exclude: /node_modules/,
                use: [
                    {
                        loader: 'babel-loader',
                        options: babelConfig
                    },
                    {
                        loader: 'ts-loader',
                        options: {
                            configFile: '../tsconfig.json'
                        }
                    }
                ]
            },
            {
                test: scssRegex,
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
                ]
            },
            {
                test: [ assetRegex, fontRegex ],
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
                            /**
                             * `[path]` == relative path from src folder,
                             * e.g. `src/assets/my-image.png` or `src/assets/images/my-image.png`.
                             *
                             * Don't append `[path]` for favicon files since they
                             * need to be in the output root directory.
                             *
                             * This, mixed with the removal of `static/` in the
                             * `outputPath` function results in outputting favicon
                             * files in output root directory.
                             */
                            return `[name].[ext]`;
                        }

                        if (fontRegex.test(filename)) {
                            // Don't append hash to font file outputs
                            // so that the SCSS mixin can work with the direct file name
                            return '[path][name].[ext]';
                        }

                        return '[path][name]-[contenthash:8].[ext]';
                    },
                },
            },
            {
                test: svgRegex,
                oneOf: [
                    {
                        // Non-JS files, e.g. CSS
                        issuer: {
                            not: jsAndTsRegex,
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
                        issuer: jsAndTsRegex,
                        resourceQuery: {
                            // Only output React component if not querying for the URL, i.e. *.svg?url
                            not: [ /url/ ],
                        },
                        use: [
                            {
                                loader: 'babel-loader',
                                options: babelConfig
                            },
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
                                        const svgSrcFilePathRelative = path.relative('.', svgSrcFilePath);
                                        const svgSrcImportAliasPath = path
                                            .relative(
                                                Object.entries(tsconfig.compilerOptions.paths)[0][0],
                                                svgSrcFilePathRelative,
                                            );
                                            // .replace(/^\.\.\//, '');

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
                                                export default ${'SvgUrl'};
                                            `);

                                        astArray.push(...customAstArray);

                                        return astArray;
                                    },
                                },
                            },
                        ],
                    },
                ],
            }
        ]
    },
    resolve: {
        extensions: [ '.ts', '.tsx', '.js', '.jsx', '.*' ],
        modules: [
            'node_modules'
        ],
        alias: Object.entries(tsconfig.compilerOptions.paths).reduce((aliases, [ alias, relPaths ]) => {
            alias = alias.replace(/\/?\*\/?/, '');

            const relPathsNormalized = relPaths.map(relPath => {
                const relPathWithoutSuperfluousSlashes = relPath.replace(/\/?\*\/?/, '');
                const relPathNormalized = path.normalize(path.resolve('.', relPathWithoutSuperfluousSlashes));

                return relPathNormalized;
            });

            // Absolute paths
            aliases[alias] = relPathsNormalized;

            return aliases;
        }, {}),
    },
    entry: {
        client: [ 'core-js', 'isomorphic-fetch', paths.root + '/src/index.js' ],
    },
    output: {
        path: absoluteBuildOutputPath, // output path for webpack build on machine, not relative paths for index.html
        filename: `${transpiledSrcOutputPath}/js/[name].[contenthash:8].bundle.js`,
        chunkFilename: `${transpiledSrcOutputPath}/js/[name].[contenthash:8].chunk.js`,
        assetModuleFilename: `${transpiledSrcOutputPath}/assets/[name].[contenthash:8][ext]`,
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
        // makes env available to src
        new webpack.DefinePlugin({ 'process.env': JSON.stringify(publicEnv) }),
        // injects tags like <script> into index.html
        new HtmlWebpackPlugin({
            title: indexHtmlTitle,
            template: paths.root + '/src/index.html',
            meta: indexHtmlMetaTagData
        }),
        // replaces %PUBLIC_URL% in index.html with env entry
        new webpack.DefinePlugin({ 'process.env': JSON.stringify(publicEnv) }),
        // splits CSS out from the rest of the code
        new MiniCssExtractPlugin({
            filename: `${transpiledSrcOutputPath}/css/[name].[contenthash:8].css`,
            chunkFilename: `${transpiledSrcOutputPath}/css/[name].[contenthash:8].chunk.css`,
        }),
        // Adds mocks automatically
        new MockRequestsWebpackPlugin(
            'mocks',
            'MockConfig.js',
            process.env.MOCK === 'true'
        ),
        // manually copies files from src to dest
        new CopyWebpackPlugin({
            patterns: [
                {
                    from: 'src/manifest.json',
                    to: '[name].[ext]'
                },
                {
                    from: 'src/ServiceWorker.js',
                    to: '[name].[ext]'
                }
            ]
        })
    ],
    optimization: {
        moduleIds: 'deterministic',
        minimize: process.env.NODE_ENV === 'production',
        usedExports: true,
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
            cacheGroups: {
                vendor: { // split node_modules (as vendor) from src (as client)
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendor',
                    chunks: 'all',
                    maxSize: 200000,
                },
                styles: {
                    test: scssRegex,
                    type: 'css/mini-extract',
                    name: 'styles',
                    chunks: 'all',
                    enforce: true // collect all CSS into a single file since the separated CSS files contained only duplicate code
                }
            }
        }
    },
    performance: {
        hints: false // disable "entrypoint size limit" warning
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
    devServer: {
        port: 3000,
        proxy: {
            '/**': {
                target: 'http://localhost:8080',
                secure: false,
                changeOrigin: true
            }
        },
        devMiddleware: {
            // Specify Webpack output only for the dev-server "emitted" output
            stats: {
                preset: 'minimal', // Greatly reduce output info by extending 'minimal' preset, overwriting options in root-level `stats`
                builtAt: true,
            },
        },
        open: true, // open browser window upon build
        hot: hotReloading, // for `module.hot` hot-reloading block in index.js
        historyApiFallback: true // For React Router
    }
};
