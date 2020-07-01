# Anime Atsume (back-end)

Simple search engine for anime that includes links for watching episodes

Notes:

### General
* To run on specific port, `server.port` option is necessary, either in application.properties or via command line
    * CMD: `java -jar anime-atsume.war --server.port=80`
    * Port 80 for HTTP, 443 for HTTPS
    * Usually, running on ports 80 or 443 will require `sudo`
* If running in a hosted VM, use the `nohup` command to allow the process to run even after your SSH session has ended. **Make sure to run in background**
    * e.g. `nohup java -jar anime-atsume.war --server.port=80 &`

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
* If all else fails, rewrite code with Selenium and allow it via [this method](https://medium.com/@CapitalTerefe/selenium-grid-in-docker-using-serenity-in-google-cloud-47b57deab5d)
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
* Cloud Compute (VM instance)
    * Needed:
        * `sudo apt install openjdk-8-jre`
        * Alternatively, try full JDK+JFX packages:
            * [Zulu "JDK FX"](https://www.azul.com/downloads/zulu-community/?architecture=x86-64-bit)
            * [Liberica "Full JDK"](https://bell-sw.com/pages/downloads/)
            * [Other options](https://stackoverflow.com/questions/61783369/install-openjdkopenjfx-8-on-ubuntu-20)
    * Not sure which of the following are needed, if any at all:
        * JavaFX: `sudo apt install openjfx=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2`
            * Didn't find the solution to run with monocle.platform=Headless until after installing openjfx
            * Gotten from [stackoverflow](https://stackoverflow.com/questions/56166267/how-do-i-get-java-fx-running-with-openjdk-8-on-ubuntu-18-04-2-lts)
        * `sudo apt install xvfb`
        * `sudo apt install xorg libgtk2.0-0`
    * Run with headless mode:
        * `java -Dglass.platform=Monocle -Dmonocle.platform=Headless -jar anime-atsume-1.0.war`
