## Atavism Server Docker Containers
Provides and example on how to create containers for a multi server setup. 

### Pre-Requsists
- Docker
- Atavism Subscription
- Atavism Server Download

### Getting started
- Download the atavism_server to this projects root directory. 
example. `atavism_server_10_9_0_20231229_1523.zip`
- Configure credentails in the mysql.env with values unique to you
- Configure credentsils in the server.env with values unique to you, keep them safe!
- Build the containers with docker compose `docker compose build`
- Run the containers with docker compose `docker compose up -d`

### Goals
- Easy `docker compose up -d` Setup for Demo server
- Build Containers for each service (MySql, Auth, World, Etc)
- Integration (Wordpress or other) for Website Example on Account Creation
- Build Client in Unity Build Container, provide all configuration and export resources to prefab server?