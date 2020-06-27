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
* For [deploying war file](https://devcenter.heroku.com/articles/war-deployment#deployment-with-the-heroku-cli):
    * `heroku plugins:install java`
    * `heroku war:deploy <path_to_war_file> --app <app_name>`

### Deploying to gcloud
* Only deploying .war file has worked so far.
    * Requires app.yaml file to be present in the root dir of where you're deploying
        * Will mean app.yaml needs to be in same dir as .war file:
        ```
        runtime: java
        env: flex

          handlers:
          - url: /.*
            script: this field is required, but ignored
        ```
    * Deploy (in server/build/libs/): `gcloud app deploy app.yaml`
    * Logs: `gcloud app logs tail -s default`
    * Open in browser: `gcloud app browse`
