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

### Docker
* Build image: `docker build -t app .`
    * Builds new image named "app" using the Dockerfile at root dir "."
    * Needs to be run in same directory where the Dockerfile is (in this case, server/).
* Run app in image: `docker run -p 8080:8080 app`
    * Runs the newly built image, "app"
    * `-p host_port:container_port`
* Run bash in image: `docker run -it app /bin/bash`
* Notes on Dockerfile:
    * It renames the .war file to `anime-atsume.war` so version number isn't needed (and CMD can just be this renamed filename).
    * Since ports are mapped in `docker run`, there's no need to force `--server.port=80`.
    * SQLite3 was added as an installation dependency (since it's not guaranteed that the docker image will support it out of the box).
    * Allow DB to be writeable via `chmod`.
* Other docker commands:
    * List images or running containers: `docker [container|image] ls`
    * List running containers: `docker ps`
    * New bash terminal on running container: `docker exec -it <id_from_ps> /bin/bash`
    * Delete image: `docker rmi <image_id>`

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
    * Make sure to include the .db file with `--includes anime_atsume.db`
    * `heroku plugins:install java`
    * `heroku war:deploy <path_to_war_file> --app <app_name> --includes anime_atsume.db`
* For [deploying with Dockerfile](https://medium.com/@urbanswati/deploying-spring-boot-restapi-using-docker-maven-heroku-and-accessing-it-using-your-custom-aa04798c0112)
    * Luckily, Heroku doesn't need you to build the image, nor to push the subtree, nor do anything else to deploy the container. It will build the image using any required files from your filesystem (e.g. .war, .db) and push it to its own container registry.
        * Note: You might need `server.port=${PORT}` in application.properties so Heroku's PORT var will overwrite Spring's port.
        * Likely not needed since Spring and Heroku both default to port 8080.
    * Make sure you're in the server/ directory. Then run:
        * `heroku container:login`
        * `heroku container:push web`
        * `heroku container:release web`
    * Heroku's "free" and "hobby" tiers only allow a maximum of 512 MB.
        * Force JVM to not exceed this cap by adding `-Xmx512m` to `CMD`.
        * `-Xmx` is to set maximum memory usage, `m` specifies megabytes.

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
        * Java 8: `sudo apt install openjdk-8-jre`
        * JavaFX: `sudo apt install openjfx=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2`
            * Didn't find the solution to run with monocle.platform=Headless until after installing openjfx
            * Gotten from [stackoverflow](https://stackoverflow.com/questions/56166267/how-do-i-get-java-fx-running-with-openjdk-8-on-ubuntu-18-04-2-lts)
            * Make sure to prevent them from updating:
            `sudo apt-mark hold openjfx libopenjfx-jni libopenjfx-java`
        * Alternatively, try full JDK+JFX packages:
            * [Zulu "JDK FX"](https://www.azul.com/downloads/zulu-community/?architecture=x86-64-bit)
            * [Liberica "Full JDK"](https://bell-sw.com/pages/downloads/)
            * [Other options](https://stackoverflow.com/questions/61783369/install-openjdkopenjfx-8-on-ubuntu-20)
    * Not sure which of the following are needed, if any at all:
        * `sudo apt install xvfb`
        * `sudo apt install xorg libgtk2.0-0`
    * Run with headless mode:
        * `java -Dglass.platform=Monocle -Dmonocle.platform=Headless -jar anime-atsume-1.0.war`
    * Final command to run with headless mode, port 80, and as background process:
        * `sudo nohup java -Dglass.platform=Monocle -Dmonocle.platform=Headless -jar anime-atsume-1.0.war --server.port=80 &`
