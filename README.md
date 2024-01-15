## Atavism Server Docker Containers
Provides and example on how to create containers for a multi server setup. 

### Pre-Requsists
- Docker
- Atavism Subscription
- Atavism Server Download

### Getting started
- Download the atavism_server to this projects root directory. 
example. `atavism_server_10_9_0_20231229_1523.zip`
- Configure you credentails root password, and username/password for your services to connect with in each of the `mysql-{service}/sql-{service}.env` files. 
  - These are senstive, do not commit or share these with anyone!
  - All SQL default to 
    - MYSQL_ROOT_PASSWORD: r007pa55w0rd
    - USER: atavism
    - PASSWORD: A7AV15M
- Configure `all-in-one/all.env` with your email and licence key
- Build the containers with docker compose `docker compose build`
- Run the containers with docker compose `docker compose up -d`
  - Deploys 4 MySQL Servers
    - atavismonline-server-docker_admim-sql
    - atavismonline-server-docker_atavism-sql
    - atavismonline-server-docker_master-sql
    - atavismonline-server-docker_world-sql
  - Deploys All-In-One server
    - atavismonline-server-docker_world-1

### Goals
- Easy `docker compose up -d` Setup for Demo server
- Build Containers for each service (MySql, Auth, World, Etc)
- Integration (Wordpress or other) for Website Example on Account Creation
- Build Client in Unity Build Container, provide all configuration and export resources to prefab server?