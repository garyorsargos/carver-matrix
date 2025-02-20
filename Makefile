TLS_FOLDER=dev/tls
APP_NAME=carvermatrix
APP_DOMAIN=org


.PHONY: create-override delete-override docker-start docker-stop-volumes \
				start start-dev start-dev-rancher stop-dev ssl write-etc-hosts

create-override:
  # Creates a docker override file needed for working with Rancher Desktop
	@touch docker-compose.override.yaml
	@echo "services:\n  api:\n    environment:" > docker-compose.override.yaml
	@echo "      APP_FRONTEND_ADDR: host.lima.internal" >> docker-compose.override.yaml
	@echo "  nginx:\n    environment:" >> docker-compose.override.yaml
	@echo "      APP_FRONTEND_ADDR: host.lima.internal" >> docker-compose.override.yaml
	@echo "      APP_BACKEND_ADDR: host.lima.internal" >> docker-compose.override.yaml

delete-override:
  # Deletes the docker override file when working with Docker Desktop
	@if [ -f docker-compose.override.yaml ]; then \
	  rm docker-compose.override.yaml; \
	fi

docker-start:
  # Builds the docker images based on the `docker-compose.yaml` file, and starts the containers
	@docker compose build
	@docker compose watch ui

docker-start-no-cache:
  # Builds the docker images based on the `docker-compose.yaml` file with no cache, and starts new containers
	@docker-compose build --no-cache
	@docker-compose down
	@docker-compose up -d --force-recreate
	@docker compose watch ui

docker-stop:
  # Stops the running containers associated with the `docker-compose.yaml` file
	@docker compose down

docker-stop-volumes:
  # Stops the running containers associated with the `docker-compose.yaml` file, and deletes the associated volumes
	@docker compose down -v

start:
	@${MAKE} \
		write-etc-hosts \
		ssl \
		docker-start

start-dev:
	@${MAKE} \
		delete-override \
		start

start-dev-no-cache:
	@${MAKE} \
		delete-override \
		docker-start-no-cache

start-dev-rancher:
	@${MAKE} \
	  create-override \
		start

restart-dev:
	@${MAKE} \
		delete-override \
		docker-stop \
		start

stop-dev:
	@${MAKE} \
		docker-stop

ssl:
  # Create the TLS_FOLDER if it does not exist
	@if [ ! -d ${TLS_FOLDER} ]; then \
		mkdir dev/tls; \
	fi

	@mkcert -install
	@mkcert --key-file ${TLS_FOLDER}/server.key --cert-file ${TLS_FOLDER}/server.crt "*.org"
	@cp "${shell mkcert -CAROOT}/rootCA.pem" ${TLS_FOLDER}

write-etc-hosts:
	@${MAKE} app-etc-hosts keycloak-etc-hosts

app-etc-hosts:
  # Writes the proper /etc/hosts entry for the application
	@if [ -z "$(shell grep '${APP_NAME}.${APP_DOMAIN}' /etc/hosts)" ]; then \
		 echo "- An entry for '${APP_NAME}.${APP_DOMAIN}' was not found in the /etc/hosts file"; \
		 echo "- Adding '0.0.0.0	${APP_NAME}.${APP_DOMAIN}' to the /etc/hosts file"; \
		 echo "0.0.0.0		${APP_NAME}.${APP_DOMAIN}" | sudo tee -a /etc/hosts; \
	 else \
		 echo "- An entry for '${APP_NAME}.${APP_DOMAIN}' was found in the /etc/hosts file"; \
	 fi

keycloak-etc-hosts:
  # Writes the proper /etc/hosts entry for the application
	@if [ -z "$(shell grep 'keycloak.${APP_DOMAIN}' /etc/hosts)" ]; then \
		 echo "- An entry for 'keycloak.${APP_DOMAIN}' was not found in the /etc/hosts file"; \
		 echo "- Adding '0.0.0.0	keycloak.${APP_DOMAIN}' to the /etc/hosts file"; \
	 	 echo "0.0.0.0		keycloak.${APP_DOMAIN}" | sudo tee -a /etc/hosts; \
	 else \
		 echo "- An entry for 'keycloak.${APP_DOMAIN}' was found in the /etc/hosts file"; \
	 fi

help: