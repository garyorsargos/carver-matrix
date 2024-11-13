# Contents

- [About](#about)
- [Local Development Setup](#local-development-setup)
- [Working on the application](#working-on-the-application)
- [Micro-services in Development Environment](#micro-services-in-development-environment)

# About

Problem Statement: I am a team member working on a project which has user facing elements. There isn’t an established front-end UI pattern library, so I am confused about what the things I am working on should look like and what code I should use.

Vision: A starter app that reduces the amount of time it takes for a team member to work on projects which have front-end elements. This will be a long-term initiative which takes place over the course of 2024

Q1 2024 Goals:

- Confirm brand standards
-
- Establish theme with React
-
- Pipeline YAML and security dependencies
-
- Docker Compose – app name, instance, etc.

Ongoing work and documentation

- Material UI documentation library: https://mui.com/material-ui/getting-started/
-
- Material UI Theme Creation interface: https://bareynol.github.io/mui-theme-creator/
-
- Branding Options: https://www.figma.com/file/wH4HE08xR0sBiujUJUb1DB/AIDivision-Starter-App?type=design&node-id=0%3A1&mode=design&t=6oEADa1zZYZuLmJL-1

# Local Development Setup

## Install Local Development Dependencies

### Docker Login to Iron Bank

- If you haven’t already, create a Docker Account and Verify it. This allows you to access the Docker Registry to set up our application.
- Afterward, create an account with Iron Banks through `Login with P1 SSO`. https://registry1.dso.mil/harbor/projects.
  - This utilizes OAuth 2.0, so you will have to move to the Iron Bank Harborauth page.
- Create a new account and fill out the required fields. For the Login Fields, fill in as descriptive as possible.
- Once the account is created, log back into the Iron Bank Harbor and use an Authorizer App to get the first Login.
- Wait for a little to get approval from an anonymous OIDC Provider
- Go back to https://registry1.dso.mil/harbor/projects since the OAuth 2.0 is completed, and click `Login Via Local DB` with the credentials you made previously
  - Note: If you can’t log in, you may not have been approved yet.
- Once you have logged in, click your User Profile (Top-Right), and you will find your Username and CLI Secret for Iron Bank.
- Ensure that the Docker Engine is Running
  - For Docker Desktop, you can see this on the bottom of the screen.

5. Run this command `echo "your_password_here" | docker login registry1.dso.mil -u your_username --password-stdin`

- Example: `echo "mysupersecretcred" | docker login registry1.dso.mil -u Jonathan_Diquattro --password-stdin`

### WSL 2 Setup for Windows Machine with VSCode

- Open Windows PowerShell or Windows Command Prompt in Administrator Mode.
- In the Shell/Command Line: `wsl --install`. By default, It should install Ubuntu for WSL2
- Restart your computer to allow WSL to be set up correctly.
- Go to VSCode and install the following Extension: WSL
- Once installation is completed, select the option to open a remote window on the bottom left corner of the VSCode window.
- On the Center of the Top Screen, it will ask which remote windows to open.
  - Select `Connect to WSL to Distro` > `Ubuntu`.
- Once the new Window is finished loading, check the Bottom Left Corner to confirm you are using WSL.

**ALL OPERATIONS** for the project are done faster through WSL with Docker. If you do not see `WSL: Ubuntu`, then you are editing using Windows, which is significantly slower than using WSL (Especially when running the development application). 8. Open the Explorer Tab (Found on the Left Taskbar) and click `Open Folder`. and open Folder to just default `/home/UNIXUsername/`.

You are now in the home file directory of your Linux Environment.
Additionally, you can navigate to your WSL folder on the bottom of your file explorer.
Your Home directory should be `\\wsl.localhost\Ubuntu\home\UNIXUsername`
This should also be where you place your Local Repository to help project consistency.

#### Potential Errors:

- “Failed to attach disk 'LocalState\ext4.vhdx' to WSL2: The system cannot find the file specified.” To resolve this:
  ```
  wsl --unregister ubuntu
  wsl --install
  ```
  This helps reset pointers in WSL for Ubuntu, allowing installation to use new pointers for Ubuntu.
- If WSL is already installed, to ensure that Ubuntu is installed, paste the following: `wsl - install -d Ubuntu`
  - This will lead to removing Ubuntu, so be cautious of the files already present within the Distro.

### Install mkcert

- Linux install command:
  ```
  sudo apt update
  sudo apt install libnss3-tools
  sudo apt install mkcert
  ```
- Mac install command:
  ```
  brew install mkcert
  brew install nss # if you use Firefox
  ```
- If you run into errors, refer to the [mkcert documentation](https://github.com/FiloSottile/mkcert)

### Install a docker daemon

- Use one of the methods below to run Docker on your development computer :
  - `Docker Desktop`
    - Docker Desktop is the easiest way to turn on your docker daemon.
    - Download and install Docker Desktop from: [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
    - Start Docker Desktop
  - `Docker Engine`
    - Follow the directions found here: [https://docs.docker.com/engine/install](https://docs.docker.com/engine/install/)

## Startup Procedures

- The development environment is conatinerized with several micro-services running in parallel to provide a development experience to the developer that is as close to production as possible.

### Start your docker daemon

- `Docker Desktop`
  - Start the Docker Desktop application from your desktop
- `Docker Engine`
  - Start the Docker service from your command line:
    ```
    sudo systemctl start docker
    ```
  - You may have different commands to start this process depending on your flavor of Linux OS

### Verify that the Docker daemon is running

- Try running a docker command
  ```
  docker ps
  ```
- You chould see:
  ```
  CONTAINER ID    IMAGE     COMMAND     CREATED     STATUS
  ```

## Use the Makefile sripts to start your local development environment

- Navigate to the root project directory: `starter-app/`
- Setup your local development environment by running:

  ```
  make start-dev
  ```

  - This command will automatically:

    1. Add the following entries to you `/etc/hosts` file:
       ```
       0.0.0.0   keycloak.zeus.socom.dev
       0.0.0.0   starter-app.zeus.socom.dev
       ```
    2. Use the [mkcert](https://github.com/FiloSottile/mkcert) package to create and install a local CA in the system root store, and generate locally-trusted certificates
    3. Build all of you docker containers based on the `docker-compose.yaml` file configurations
    4. Watch the `ui/` directory for any code changes and automatically rebuild the `ui` container. Docker compose watch (which is the command being used) is a unique way we can harness the power of the hot-reloading development server that is provided by Vite. It watches for changes in your host machine `starter-app/ui/src` directory and syncs those changes to the `ui` container, which allows Vite to hot-reload your lastest additions.

  - The `make start-dev` command may take several minutes to build all of your containers and start them depending on the speed of your internet connection
  - Once the `make start-dev` command has completed you should see:
    ```
    [+] Running 7/7
    ✔ Container redis_dev        Healthy       30.3s
    ✔ Container postgres_dev     Healthy        5.7s
    ✔ Container api              Started        5.8s
    ✔ Container ui               Started        0.2s
    ✔ Container keycloak_dev     Healthy       36.3s
    ✔ Container pgadmin          Started        1.0s
    ✔ Container oauth2proxy_dev  Started       36.5s
    ✔ Container nginx_dev        Started       36.5s
    ```

### Certificate Approval for Windows Machines

After Starting up the local host, Window Users may see that starter-app.zeus.socom.dev & keycloak.zeus.socom.dev cannot be accessed.
This is due to the differences between certificate approvals by Windows compared to MacOS and Linux OS.

- If not already, start local host to generate certificates: `make start-dev`
- Utilizing Windows File Explorer, Navigate to `C:\Windows\System32\drivers\etc\hosts`
- Right-Click `hosts` > `Properties` > `Security` > `Users` > `Edit`
- Allow Write Permissions for Users
- Open hosts and append & save

  ```
  127.0.0.1   keycloak.zeus.socom.dev
  127.0.0.1   starter-app.zeus.socom.dev
  ```

  12.7.0.0.1 is typically Localhost for Windows <br>
  You can then disallow Write permissions if desired.

- The above allows connection from localhost to the above URLs, which they make start-dev initially do, but since the appending is done in WSL, it does not work as intended.
- Create a copy of rootCA.pem and rename it as `rootCA.crt` for Windows use.
  This is found in `carver-matrix/dev/tls/rootCA.pem`
- Open Run through `Win + R` and type `certmgr.msc`
  (Certificate Manager)
- Navigate to Certificates - `Current User` > `Trusted Root Certification Authorities` > `Certificates`.
- On the top nav, `Action` > `All Tasks` > `Import`
- Follow the Certificate Import Wizard's Default and import rootCA.crt <br>
  This registers the app as trusted, allowing any browser to access it as intended.

#### Fixing Caching

If you attempted to visit the starter app before this, there may be incorrect caches.

- Check Browser Caching and DNS Issues
- Clear any old DNS records or cache from your browser
  - For Chrome: chrome://net-internals/#dns & Click on Clear host cache.
  - Also, verify that HSTS is cleared by visiting:
    - chrome://net-internals/#hsts
  - Delete the domain security policy for starter-app.zeus.socom.dev.

#### Fixing Certificates

If the Certificate ever changes, you will have to repeat these steps again

# Working on the application

## App Login

- At this point the application is up and running
- Login to the starter-app user interface
  - Navigate to: [https://starter-app.zeus.socom.dev](https://starter-app.zeus.socom.dev)
  - You will be redirected to a keycloak login page that says `"ZEUS-APPS"`
  - Login with:
    - Username: `user`
    - Password: `password`
  - You will then be redirected to the starter app user interface

## Keycloak Login

- There is also access to the keyloak_dev instance by:
  - Navigating to the address: [https://keycloak.zeus.socom.dev](https://keycloak.zeus.socom.dev)
  - Click on `Administration Console`
  - Login with:
    - Username: `admin`
    - Password: `password`

# Stopping the development environment

- From the same terminal that you ran `make start-dev` press the `CTRL + C` to break out of the docker watch command
- Stop all the containers with the command:
  ```
  make stop-dev
  ```
- You should see all of the containers stop running:
  ```
  [+] Running 8/7
  ✔ Container api                        Removed     0.0s
  ✔ Container ui                         Removed     0.2s
  ✔ Container nginx_dev                  Removed     0.2s
  ✔ Container oauth2proxy_dev            Removed     0.1s
  ✔ Container redis_dev                  Removed     0.1s
  ✔ Container keycloak_dev               Removed     0.2s
  ✔ Container pgadmin                    Removed     0.1s
  ✔ Container postgres_dev               Removed     0.1s
  ✔ Network starter-app_starter-network  Removed     0.1s
  ```

# Exporting Keycloak JSON

- To export Keycloak configuration, the following operations need to be performed
- Provide Keycloak Privlleges to allow Keycloak to edit a persistant volume:
  ```
  docker run --rm -v keycloak_export:/opt/keycloak/data alpine sh -c "chmod -R 777 /opt/keycloak/data"
  ```
- Temporarily open a keycloak_dev instance and perform realm export to a persistant volume.

  ```
  # Export with users and move to zeus-apps-realm.json
  docker exec --user root keycloak_dev /opt/keycloak/bin/kc.sh export --realm=zeus-apps --dir=/opt/keycloak/data/export/with-users --users realm_file
  docker exec --user root keycloak_dev mv /opt/keycloak/data/export/with-users/zeus-apps-realm.json /opt/keycloak/data/export/with-users/zeus-apps-realm.json


  # Export without users and move to zeus-apps-realm.json.bak
  docker exec --user root keycloak_dev /opt/keycloak/bin/kc.sh export --realm=zeus-apps --dir=/opt/keycloak/data/export/without-users
  docker exec --user root keycloak_dev mv /opt/keycloak/data/export/without-users/zeus-apps-realm.json /opt/keycloak/data/export/without-users/zeus-apps-realm.json.bak

  ```

- Access `carver-matrix_keycloak_export` in Docker Volumes to retrive zeus-apps-realm.json and zeus-apps-realm.json.bak.
  - zeus-apps-realm.json can be used to update keycloak configuration by overriding dev/keycloak/zeus-apps.json and dev/zeus-apps.json.bak

# Micro-services in Development Environment

| Service                                 | Function                                                                                                                                                                                       |
| --------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **NGINX**                               | Provides SSL termination and reverse proxy services to other micro-services                                                                                                                    |
| **Oath2Proxy**                          | Provides Oauth redirection for the application UI and API to the Keycloak OIDC endpoint                                                                                                        |
| **Keycloak**                            | An OIDC endpoint that provides SSO, RBAC, and signed JWTs for application access and authorization                                                                                             |
| **Redis**                               | Provides a caching mechanism to Oauth2Proxy                                                                                                                                                    |
| **Postgres**                            | Relational database used for storing application data                                                                                                                                          |
| **pgAdmin 4**                           | Provides a graphical user interface for managing and interacting with the Postgres database, allowing users to execute queries, visualize data, and perform database administration tasks data |
| **User Interface (UI)**                 | Containerized version of the starter-app application's front-end running as a Single Page Application (SPA) served from an NGINX container (static file server)                                |
| **Application Program Interface (API)** | Containerized version of the starter-app application's back-end                                                                                                                                |
