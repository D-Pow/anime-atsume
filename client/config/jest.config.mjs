import fs from 'node:fs';
import path from 'node:path';

import { defaults } from 'jest-config';

import tsconfig from '../tsconfig.json' assert { type: 'json' };


const allAppDirectories = fs.readdirSync('..', { withFileTypes: true })
    .filter(directoryEntry => directoryEntry.isDirectory())
    .map(directory => directory.name);
const allAppDirsFormattedForJest = allAppDirectories.map(dir => `<rootDir>/${dir}`);
const nonSrcJestDirs = allAppDirsFormattedForJest.filter(directory => !directory.includes('src'));

/** @type {import('@jest/types').Config.InitialOptions} */
const jestConfig = {
    ...defaults,
    rootDir: path.resolve('..'),
    testEnvironment: 'jsdom',
    setupFilesAfterEnv: [
        '<rootDir>/config/jestSetup.js',
        path.resolve('mocks/', 'MockConfig.js'), // Mock network requests using default MockRequests configuration in mocks/MockConfig.js
    ],
    moduleNameMapper: {
        '\\.(s?css|png|gif|jpe?g|svg|ico|pdf|tex)$': '<rootDir>/config/jestFileMock.js',
        ...Object.entries(tsconfig.compilerOptions.paths).reduce((aliases, [ alias, paths ]) => {
            const aliasGlob = `^${alias}/(.*)$`.replace(/(?<!\.)\*\/?/g, '');
            const pathsGlobs = paths.map(aliasPath => `<rootDir>/${aliasPath}/$1`.replace(/\*\/?/g, ''));

            aliases[aliasGlob] = pathsGlobs;

            return aliases;
        }, {}),
    },
    modulePathIgnorePatterns: [
        'dist',
        'node_modules',
        process.env.npm_package_config_buildOutputDir,
    ],
    transform: {
        '\\.[tj]sx?$': [
            'babel-jest',
            {
                configFile: path.resolve('config/', 'babel.config.js'),
            },
        ],
    },
    coveragePathIgnorePatterns: nonSrcJestDirs,
};

export default jestConfig;
