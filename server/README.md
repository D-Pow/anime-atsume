# Anime Atsume (back-end)

Simple search engine for anime that includes links for watching episodes

Notes:

### Deploying to Heroku
* Heroku doesn't know how to handle nested folders.
    * Since the front-end is built to the back-end's resources/ directory,
      we need only deploy the server/ folder.
    * `git subtree push --prefix server/ heroku master`
* Heroku doesn't have any JavaFX support.
    * UI4J uses JavaFX so we need to add support manually.
    * Add buildpacks so Herokou finds all necessary classes in its pre-commit hook:
        * `heroku buildpacks:add -i 1 https://github.com/jkutner/heroku-buildpack-javafx` - add JavaFX to build path.
        * `heroku buildpacks:add heroku/gradle` - default gradle/Spring build process.
    * Alternatively (but possibly still mandatory), add JavaFX as a dependency in build.gradle.
