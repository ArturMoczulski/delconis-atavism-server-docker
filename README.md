## Atavism Server Docker 

Development workflow and Docker setup for Atavism Server

Watch quick preview on YouTube
<a href="https://youtu.be/8QwwByLSrZ4"><img width="1712" alt="Screenshot 2024-05-14 at 5 02 30 AM" src="https://github.com/ArturMoczulski/delconis-atavism-server-docker/assets/2106631/699b2775-0d7c-4f45-9706-bc72413b57c1" target="_blank"></a>


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

2. Set up your email and Atavism license key in `docker/compose/development/.env` using `.env.example` boilerplate in the same directory.

3. `./gradlew dev.up`

_Note:_ Currently the services do not have a docker healthcheck implemented, so they tend to fail connect to the database at first start. It's recommended to run `./gradlew dev.restart` again after a couple of seconds if you're experiencing issues connecting to your Atavism Server. 

4. Wait for `DONE INITIALIZING, you can log in now` message in `world` service logs.

5. Connect with your Unity client.

### Creating a new plugin

1. `./gradlew -PpluginName=Superhero newPlugin`

2. `./gradlew -PpluginName=Superhero -PmessageName=ShootLaserFromEyes newGenericMessage`

3. `./gradlew dev.restart`

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

#### Separate Processes vs All In One mode

Atavism Server with this repo will run each module (including your custom plugins) as separate processes. This is good for development as each plugin will generate it's own log file in `atavism_server/logs/world`. If you wish to swtich to _All In One_ mode you can adjsut `ATAVISM_ALL_IN_ONE_ENABLED` variable in your env file in `docker/compose/{environmentName}/.env`.

#### SSL

1. Create or provide a OpenSSH Key `private.key` to the root of this project

```
docker run -it -v ./ssl/:/key mysql /bin/sh
cd key
openssl genrsa -des3 -out atavism.pem 2048
## Remember Password! it will prompt you a few times for it
openssl rsa -in atavism.pem -outform PEM -pubout -out atavismkey.txt
openssl rsa -in atavism.pem -out private.pem -outform PEM
openssl pkcs8 -topk8 -inform PEM -outform DER -in private.pem  -nocrypt > private.key
```

2. Copy your public key `atavismkey.txt` to the root of your Unity project.

### Development with VS Code + Gradle workflow

This repo sets you up with automatic reload of the Atavism Server on File save in VS Code for ease of development.

#### Setup

1. Install VS code extension [Trigger Task on Save](https://marketplace.visualstudio.com/items?itemName=Gruntfuggly.triggertaskonsave) to enable auto building and deployment of `agis.jar` to the Atavism Server on saving `*.java` and `*.gradle` files.

2. Set up the path to your JDK8 Runtime for VS Code in `.vscode/settings.js`. This will provide you with Java language server support while coding compliant with the AGIS server compatible Java version.

3. If VS Code is not providing Java autocompletions and suggestions, reload your Java Language Server and Java Project workspace in VS Code: `Cmd/Ctrl + ,` --> _Java: Restart Java Language Server_ and then `Cmd/Ctrl + ,` -> _Java: Clean Java Language Server Workspace_.

#### Reload on Save

1. Make changes in your `src/plugins/` files.

2. Save your file in VS code.

3. The Atavism Server will automatically reload.

4. Check if you changes are working correctly.

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

### Migrating existing plugins

If you want to migrate your existing plugins to use the `atavism-server-docker` structure it's easy to do.

1. Create a new plugin with the same name using the boilerplate: `./gradlew -PpluginName=MyExistingPluginName newPlugin`

2. Move your plugin source code to `/src/plugins/MyExistingPluginName/src/atavism/agis/plugins`.

3. `./gradlew dev.restart`

### How this repo works

#### Atavism Server and AGIS installation

On any `./gradlew` command the repo will check if you already have your `atavism_server` and `src/lib/atavism` directories set up. If not it will set them up using ZIPs placed in the `archives` directory. It will unzip Atavism Server ZIP into `atavism_server` and AGIS ZIP into `src/lib/`.

#### Atavism Server plugins registrations

On any `./gradlew` command the repo will inspect all your directories under `src/plugins` and look for Java classes extending `EnginePlugin`, `{pluginName}Plugin.java` and `{pluginName}Client.java` files. For each plugin found it will:

* generate `{pluginName}-ads.txt` file in `atavism_server/config/world/` with your plugin's client messages
* generate server start boilerplate code in server's `ServerStarter.java`
* generate server start boilerplate code in server's `AllInOne.java`
* generate boilerplate Python code in `atavism_server/config/world/worldmessages.py` for your client messages
* generate boilerplate Python code in `atavism_server/config/world/extensions_proxy.py` for your client messages
* generate boilerplate code in `atavism_server/config/world/all_in_one-ads.txt` for your client messages
* generate boilerplate code in `atavism_server/config/world/proxy-ads.txt` for your client messages
* generate boilerplate code in `atavism_server/config/world/worldmarshallers.txt` for your client messages
* generate boilerplate code in `MessageInitializer.java` for your client messages
* generate your plugin's properties file from `src/plugins/{pluginName}/plugin.properties` to `atavism_server/config/world/{pluginName}.py`
* generate boierplate startup code for your plugin in `atavism_server/bin/world.sh`

#### Docker setup

On `./gradlew dev.up` it will create a Docker compose project with containers for your databases (one container per database) and your `master` (authentication` and `world` services. By default repo runs each module as a separate JAVA process inside the `world` container which will result in a separate log file for each module in `atavism_server/logs/world`. You can change this to run in `AllInOne` mode in your `docker/compose/development/.env` file.

#### Reload

On `./gradlew dev.reload`, which is also triggered by VS Code file save, it will follow the above described process to rebuild your plugins and server files and trigger Java processes restart in the `world` container.

#### Code generation commands `newPlugin` and `newGenericMessage`

These commands will use Handlebars template files from `buildSrc/templates` to generate contents of the newly generated code. The files inside `buildSrc/templates` follow the same directory structure as it will be in the files produced.

One addition to the standard Handlebars processing logic are the injection directives, like:

```
{{!-- @Inject("class", "{{pluginCamelCase}}Client", "end") --}}
```

which instruct the code injector where to inject newly generated code. Thanks to this last part, `newGenericMessage` is able to inject newly generated client messages boilerplate code into existing plugins.

#### Removing plugins

As of now, the repo does not have support for removing plugins. Once the plugins have been registered, manually removing plugin directories from `src/plugins` will cause future `./gradlew` commands to fail due to missing classes. 

So if you have to remove a plugin, you need to first manually remove autogenerated registration code from files described above in section _Atavism Server plugins registrations_ and then remove the plugin directory. Make sure to not run any `./gradlew` commands in between as they will fail.

I'm planning to add `removePlugin` command in the future that will take care or this without manual steps.

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
