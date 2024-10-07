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
- This application uses ironbank images and will require a docker login with iron bank creds.
- To create an Iron Bank account go here: https://registry1.dso.mil/harbor/projects
- Once you have created an account login and click your profile to see your creds.
- Run this command ```docker login registry1.dso.mil -u your_username -p yourcredshere```
- Example: ```docker login registry1.dso.mil -u Jonathan_Diquattro -p mysupersecretcred```

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
### Use the Makefile sripts to start your local development environment
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
    ✔ Container oauth2proxy_dev  Started       36.5s 
    ✔ Container nginx_dev        Started       36.5s
    ```

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
  ✔ Container postgres_dev               Removed     0.1s 
  ✔ Network starter-app_starter-network  Removed     0.1s
  ```

# Micro-services in Development Environment
  Service | Function
  -- | --
  **NGINX** | Provides SSL termination and reverse proxy services to other micro-services
  **Oath2Proxy** | Provides Oauth redirection for the application UI and API to the Keycloak OIDC endpoint
  **Keycloak** | An OIDC endpoint that provides SSO, RBAC, and signed JWTs for application access and authorization
  **Redis** | Provides a caching mechanism to Oauth2Proxy
  **Postgres** | Relational database used for storing application data
  **User Interface (UI)** | Containerized version of the starter-app application's front-end running as a Single Page Application (SPA) served from an NGINX container (static file server)
  **Application Program Interface (API)** | Containerized version of the starter-app application's back-end
