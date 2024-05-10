## Atavism Server Docker Containers

Provides and example on how to create containers for a multi server setup.

### Pre-Requsists

- Docker
- Atavism Subscription
- Atavism Server Download
- 2-4GB of ram for Docker Engine (Untested at any load, 866mb on init)

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
