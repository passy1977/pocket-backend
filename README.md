# pocket-web-backend
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/passy1977/pocket-lib)
## Purpose and Scope
This document provides a technical introduction to the Pocket backend system, a secure data management and synchronization platform. The Pocket backend enables clients to store, retrieve, and synchronize structured data through encrypted communication channels. This overview explains the system's architecture, key components, and functionality.
Purpose and Scope

The Pocket backend serves as the server-side component for client applications requiring secure storage and synchronization of hierarchical data structures. It provides:
* Secure authentication and session management
* Data storage and retrieval with change tracking
* Cross-device data synchronization
* Encryption of sensitive information
* REST API and IPC socket interfaces

For detailed information about the API interfaces, see API Reference, and for the authentication system, see Authentication and Session Management.

## How build
Make sure you have maven and a java sdk 21 installed.
```bash
git clone https://github.com/passy1977/pocket-backend.git
cd pocket-backend
```
Make sure you have changed all the passwords in the file _src/main/resources/application.yaml_.  

Create a Mariadb instance with Docker 
```bash
docker run --detach --name db -p 3306:3306  --env MARIADB_ROOT_PASSWORD=passwd_to_change mariadb:latest
```
Create new schema on Mariadb
```bash
docker exec -it db /usr/bin/mariadb -u root -ppasswd_to_change < scripts/pocket5.sql
```
Build 
```bash
mvn clean install
```
Execute
```bash
mvn  exec:java -Dexec.mainClass="it.salsi.pocket.Application"
```
## Docker
If you want a containerized solution you can use the following script and follow the installation instruction
```bash
./build_docker_image.sh
```
# Handle user and device
In order to access the server, you need to register a user with _pocket-user_ and every device linked to the user created with _pocket-user_.

You will need to use the generated code generate from _pocket-user_ to activate a client like [pocket-ios](https://github.com/passy1977/pocket-ios).  

_pocket-user_ and _pocket-user_ you can obtain from [pocket-cli](https://github.com/passy1977/pocket-cli) repo

```bash
usage: pocket-user command [options]

commands:
    add                             add new user options mandatory: email, passwd, name  
    mod                             modify user options mandatory: email, passwd, name
    rm                              remove user options mandatory: email
    get                             get user information options mandatory: email

options:
    -P, --server-passwd <passwd>    set pocket server password, once the password is provided the system will remember it
    -e, --email <email>             set user email
    -p, --passwd <passwd>           set user passwd
    -n, --name <name>               set user name
    -h, --help <command>            show help
```

```bash
usage: pocket-user command [options]

commands:
    add                             add new user options mandatory: email, passwd, name  
    mod                             modify user options mandatory: email, passwd, name
    rm                              remove user options mandatory: email
    get                             get user information options mandatory: email

options:
    -P, --server-passwd <passwd>    set pocket server password, once the password is provided the system will remember it
    -e, --email <email>             set user email
    -p, --passwd <passwd>           set user passwd
    -n, --name <name>               set user name
    -h, --help <command>            show help
```
