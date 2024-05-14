## Atavism Server Docker 

Development workflow and Docker setup for Atavism Server

### Prerequisites

- [Docker Engine](https://www.docker.com/)
  - Recommended one of the following Docker Engine UI's:
    - [OrbStack](https://orbstack.dev/)
    - [Docker Desktop](https://www.docker.com/products/docker-desktop/)
  - 2-4GB of ram for Docker Engine (Untested at any load, 866mb on init)
- [Atavism](https://atavismonline.com/) Subscription
- Atavism Server Download
- [Java Development Kit 8](https://adoptium.net/temurin/releases/?version=8)
- [Visual Studio Code](https://code.visualstudio.com/)
- Visual Studio Code extensions:
  - [Language Support for Java(TM) by Red Hat](https://marketplace.visualstudio.com/items?itemName=redhat.java)
  - [Trigger Task on Save](https://marketplace.visualstudio.com/items?itemName=Gruntfuggly.triggertaskonsave)
  - [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

### Getting started

1. Download Atavism Server ZIP and Agis ZIP from [Atavism Online APanel](https://apanel.atavismonline.com/) into `archives/`.

2. `./gradlew dev.up`

3. Connect with your Unity client.

### Creating a new plugin

1. `./gradlew -PpluginName=Superhero newPlugin`

2. `./gradlew -PpluginName=Superhero -PmessageName=ShootLaserFromEyes newGenericMessage`

3. `./gradlew dev.restart`

_Note:_ Currently the services do not have a docker healthcheck implemented, so they tend to fail connect to the database at first start. It's recommended to run `./gradle dev.restart` again after a couple of seconds if you're experiencing issues connecting to your Atavism Server. 

4. Call the Atavim Server with your new message from the client:

```C#
NetworkAPI.SendExtensionMessage(
  ClientAPI.GetPlayerOid(), // object id of the subject
  false, 
  "superhero.MSG_TYPE_SUPERHERO_SHOOT_LASER_FROM_EYES", // your client message
  new Dictionary<string, object>() // your client message props (empty is fine)
);
```

5. Inspect your `atavism_server/logs/world/all_in_one.log` for your message being received by the server.

_Note:_ Remember to set `atavism.log_level=X` to an appropriate level in `atavism_server/bin/world.properties` to see your log messages.

### Configuring your environment

#### Environment files

There are separate environment files for `development` and `production`. You can place them in `docker/compose/{environmentName}/.env` and use `.env.example` file as a boilerplate. This is where you can change your database credentials, etc. 

#### SSL

Create or provide a OpenSSH Key `private.key` to the root of this project

```
docker run -it -v ./ssl/:/key mysql /bin/sh
cd key
openssl genrsa -des3 -out atavism.pem 2048
## Remember Password! it will prompt you a few times for it
openssl rsa -in atavism.pem -outform PEM -pubout -out atavismkey.txt
openssl rsa -in atavism.pem -out private.pem -outform PEM
openssl pkcs8 -topk8 -inform PEM -outform DER -in private.pem  -nocrypt > private.key
```

### Development with VS Code + Gradle workflow

#### Setup

1. Install VS code extension [Trigger Task on Save](https://marketplace.visualstudio.com/items?itemName=Gruntfuggly.triggertaskonsave) to enable auto building and deployment of `agis.jar` to the Atavism Server on saving `*.java` and `*.gradle` files.

2. Set up the path to your JDK8 Runtime for VS Code in `.vscode/settings.js`. This will provide you with Java language server support while coding compliant with the AGIS server compatible Java version.

3. If VS Code is not providing Java autocompletions and suggestions, reload your Java Language Server and Java Project workspace in VS Code: `Cmd/Ctrl + ,` --> _Java: Restart Java Language Server_ and then `Cmd/Ctrl + ,` --> _Java: Clean Java Language Server Workspace_.

#### Commands

- Development:Build tasks

  - `./gradlew build` - Builds agis.jar and deploys it to `atavism_server`. It will register your custom plugins and messages automatically.

  - `./gradlew dev.reload` - Builds agis.jar, deploys it and restarts the Atavism Server process in the world container. Triggered automatically in VS code on save of any \*.java files in your `src/` directory.

- Development:Docker tasks
  - `./gradlew dev.up` - Starts Docker containers for development
  - `./gradlew dev.down` - Stops Docker containers for development
  - `./gradlew dev.logs` - Display logs for the dev containers
  - `./gradlew dev.restart` - Restarts Docker containers for development

- Code Generation
  - `./gradlew -PpluginName=Superhero newPlugin` - Creates a new plugin in `src/plugins/` from a boilerplate
  - `./gradlew -PpluginName=Superhero -PmessageName=ShootLaserFromEyes newGenericMessage` - Creates a new client to server message boilerplate in your `Superhero` plugin named `ShootLaserFromEyes`

#### Structure

- `.gradle`: temporary gradle working directory. Don't modify and don't commit.

- `gradle`: temporary gradle wrapper working directory. Don't modify and don't commit.

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
