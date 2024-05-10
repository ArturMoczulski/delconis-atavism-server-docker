## Atavism Server Docker Containers

Provides and example on how to create containers for a multi server setup.

### Pre-Requsists

- [Docker Engine](https://www.docker.com/)
  - Recommended one of the following Docker Engine UI's:
    - [OrbStack](https://orbstack.dev/)
    - [Docker Desktop](https://www.docker.com/products/docker-desktop/)
  - 2-4GB of ram for Docker Engine (Untested at any load, 866mb on init)
- [Atavism](https://atavismonline.com/) Subscription
- Atavism Server Download
- [Gradle](https://gradle.org/)
- [Java Development Kit 8](https://adoptium.net/temurin/releases/?version=8)
- [Visual Studio Code](https://code.visualstudio.com/)
- Visual Studio Code extensions:
  - [Language Support for Java(TM) by Red Hat](https://marketplace.visualstudio.com/items?itemName=redhat.java)
  - [Trigger Task on Save](https://marketplace.visualstudio.com/items?itemName=Gruntfuggly.triggertaskonsave)
  - [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

### Getting started

1. Download the atavism_server.zip to the root of this project `atavism_server_10_9_0_20231229_1523.zip`. If you have your Atavism Server in a git repo you can also just clone it to the root as `atavism_server` directory. `atavism_server` directory is ignored in this repo's `.gitignore` to make updates easy.

2. Update the `docker/compose/development/.env` and `docker/compose/production/.env` file with your

- ATAVISM_EMAIL with your email
- ATAVISM_KEY with your liscense key
- Update MySQL Credentails or create override file.

There are example templates provided in the same directory `env.example`

3. Create or provide a OpenSSH Key `private.key` to the root of this project

```
docker run -it -v ./ssl/:/key mysql /bin/sh
cd key
openssl genrsa -des3 -out atavism.pem 2048
## Remember Password! it will prompt you a few times for it
openssl rsa -in atavism.pem -outform PEM -pubout -out atavismkey.txt
openssl rsa -in atavism.pem -out private.pem -outform PEM
openssl pkcs8 -topk8 -inform PEM -outform DER -in private.pem  -nocrypt > private.key
```

#### For development setup

4. Build the containers with docker compose `docker-compose -f docker/compose/development/single.yml build`
5. Run `docker-compose -f docker/compose/development/single.yml up -d`

_Note:_ Currently the services do not have a docker healthcheck implemented, so they tend to fail connect to the database at first start. It's recommended to restart the `world` and `master` service after a couple of seconds so it reattempts to connect to mysql once it's accepting connections.

#### For production setup

4. Build the containers with docker compose `docker-compose -f docker/compose/production/single.yml build`
5. Run `docker-compose -f docker/compose/production/single.yml up -d`

#### Unity client

6. Configure Unity Client with User/Passwords/PublicKey as IP information as needed

- `docker inspect atavismonline-server-docker-world-1` (Network Section) might be of use

### Goals

- [x] Easy `docker compose up -d` Setup for Demo server
- [ ] Build Containers for each Demo service
  - [x] MySQL Admin
  - [x] MySQL Atavism
  - [x] MySQL master
  - [x] MySQL world_content
  - [x] All in One
  - [ ] Auth Server
  - [ ] Proxy Server
  - [ ] Prefab Server
  - [ ] Auction Server
  - [ ] ETC.
- [ ] Add Intergration, such as Wordpress.
- [ ] Add Atavism Editor Webapp
- [ ] Build Containers for each Core Serice
  - [ ] Same as demo
  - [ ] Add Client Build
    - [ ] Export Prefabs/Nav mesh to server
  - [ ] Create scalable deployment solution on orstration platform
  - [ ] Create Scale Module to manage creating and destroying instances on orstration plaform

### VS Code + Gradle workflow

#### Setup

1. Make sure the Atavism Server is in `atavism_server` directory. It can be plain files, a Git repo or a symlink.

2. Unzip your AGIS source files to `src/lib`. The full path should be `src/lib/atavism/agis`.

3. Install VS code extension [Trigger Task on Save](https://marketplace.visualstudio.com/items?itemName=Gruntfuggly.triggertaskonsave) to enable auto building and deployment of `agis.jar` to the Atavism Server on saving `*.java` and `*.gradle` files.

4. Install [Gradle](https://gradle.org/install/)

5. Set up the path to your JDK8 Runtime for VS Code in `.vscode/settings.js`. This will provide you with Java language server support while coding compliant with the AGIS server compatible Java version.

6. If VS Code is not providing Java autocompletions and suggestions, reload your Java Language Server and Java Project workspace in VS Code: `Cmd/Ctrl + ,` --> _Java: Restart Java Language Server_ and then `Cmd/Ctrl + ,` --> _Java: Clean Java Language Server Workspace_.

#### Commands

- Development:Build tasks

  - `gradle dev.reload` - Builds agis.jar, deploys it and restarts the Atavism Server process in the world container. Triggered automatically in VS code on save of any \*.java files in your `src/` directory.

- Development:Docker tasks
  - `gradle dev.up` - Starts Docker containers for development
  - `gradle dev.down` - Stops Docker containers for development
  - `gradle dev.logs` - Display logs for the dev containers
  - `gradle dev.restart` - Restarts Docker containers for development

#### Structure

- `.gradle`: temporary gradle working directory. Don't modify and don't commit.

- `.vscode`: Visual Studio code settings. Includes automations, task triggers, etc.

- `atavism_server`: your original unzipped Atavism Server copy

- `build`: temporary gradle working directory. Don't modify.

- `buildSrc`: gradle build files used for this repo. Used for gradle build tasks, code generation, reload triggers, etc.

- `docker`: docker compose setup for containerization

- `src`: your server code. This is the main directory for your work.

  - `src/lib`: contains dependency packages for your source code. Mostly, AGIS. Those are referenced during JAR gradle builds. You should unzip your AGIS archive here.

  - `src/examples`: contains templates for quick starting new projects

  - `src/plugins`: this is where your plugins source code lives, i.e. `src/plugins/MyNewPlugin`

- `ssl`: store your SSL keys here

#### Adding a new plugin to All in one

1. In `ServerStart` add a plugin start method to register your plugin:

```java
public static void startMyNewPlugin() {;
    Engine.registerPlugin("atavism.agis.plugins.MyNewPlugin");
}
```

2. In `AllInOne::postScript` invoke your plugin registration:

```java
private static void postScript() {
    ...
    // My New Plugin
    startMyNewPlugin();
}
```

3. In `AllIneOne::main` add your plugin's camelcased name to the list of registered plugins:

```java
AdvertisementFileMerger.merge(..., "your_new_plugin");
```

4. In `MessageInitializer::init` add all your plugins new messages:

```java
public static void init() {
    ...
    // Real Health Plugin
    aoMessageCatalog.addMsgTypeTranslation(MyNewPluginClient.MSG_TYPE_MY_NEW_MESSAGE);
}
```
