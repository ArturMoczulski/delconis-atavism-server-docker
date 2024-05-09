## Atavism Server Docker Containers

Provides and example on how to create containers for a multi server setup.

### Pre-Requsists

- Docker
- Atavism Subscription
- Atavism Server Download
- 2-4GB of ram for Docker Engine (Untested at any load, 866mb on init)

### Getting started

- Download the atavism_server.zip to the root of this project `atavism_server_10_9_0_20231229_1523.zip`
- Update the `.env` file with your
  - ATAVISM_EMAIL with your email
  - ATAVISM_KEY with your liscense key
  - Update MySQL Credentails or create override file.
- ## Create or provide a OpenSSH Key `private.key` to the root of this project
  ```
  docker run -it -v .:/key mysql /bin/sh
  cd key
  openssl genrsa -des3 -out atavism.pem 2048
  ## Remember Password! it will prompt you a few times for it
  openssl rsa -in atavism.pem -outform PEM -pubout -out atavismkey.txt
  openssl rsa -in atavism.pem -out private.pem -outform PEM
  openssl pkcs8 -topk8 -inform PEM -outform DER -in private.pem  -nocrypt > private.key
  ```
  - `private.key` Store Securely, you'll want this in the project root when starting a server, it will be copyed to the approprate servers.
  - `atavismkey.txt` Store Securely, this file should be copyed to your Unity Project
  - `atavism.pem` Store Securely, this is your master file, you can create more private and public keys with this, store this secruly.
- Build the containers with docker compose `docker compose build`
- Run the containers with docker compose `docker compose up -d`
- Configure Unity Client with User/Passwords/PublicKey as IP information as needed
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

# Host mapping setup

1. Place your Atavism Server files in the `atavism_server` directory of the root of this repository. The `atavism_server` directory is ignored by git in this repo so pulling latest updates will not affect your server files. Your `atavism_server` directory can
   be another git repo if you store your server files in git.

2. Update the `docker/compose/.env` file with your

- ATAVISM_EMAIL with your email
- ATAVISM_KEY with your liscense key
- Update MySQL Credentails or create override file.

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

4. Build the containers with docker compose `docker-compose -f docker/compose/development.yml build`
