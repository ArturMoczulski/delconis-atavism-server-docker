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
- Create or provide a OpenSSH Key `private.key` to the root of this project
  - 
   ```
   docker run -it -v .:/key mysql /bin/sh
   cd key
   openssl genrsa -des3 -out atavism.pem 2048
   ## Remember Password! it will prompt you a few times for it
   openssl rsa -in atavism.pem -outform PEM -pubout -out atavismkey.txt
   openssl rsa -in atavism.pem -out private.pem -outform PEM
   openssl pkcs8 -topk8 -inform PEM -outform DER -in private.pem  -nocrypt > private.key
   ```
  - `atavismkey.key` Store Securely, you can use this in this project for subsequent builds, you should not commit this file
  - `atavismkey.txt` Store Securely, this file should be copyed to your Unity Project
  - `atavismkey.pem` Store Securely, this is your master file, you can create more private and public keys with this, store this secruly. 
- Build the containers with docker compose `docker compose build`
- Run the containers with docker compose `docker compose up -d`
- Configure Unity Client with User/Passwords/PublicKey as IP information as needed
  - `docker inspect atavismonline-server-docker-world-1` (Network Section) might be of use
### Goals
- [x] Easy `docker compose up -d` Setup for Demo server
- [ ] Build Containers for each service (MySql, Auth, World, Etc)
- [ ] Integration (Wordpress or other) for Website Example on Account Creation, Or Atavism Editor
- [ ] Build Client in Unity Build Container, provide all configuration and export resources to prefab server?