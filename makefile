SHELL = '/bin/bash'
DEV_COMPOSE_FILES = -f docker/docker-compose.yml -f docker/docker-compose.dev.yml
LOCAL_COMPOSE_FILES = -f docker/docker-compose.yml
PROJECT_NAME = migration-link-exchange
SERVICE_NAME = api

export COMPOSE_PROJECT_NAME=${PROJECT_NAME}

default: help

help: ## The help text you're reading.
	@grep --no-filename -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

up: ## Starts/restarts the API in a production container.
	docker compose ${LOCAL_COMPOSE_FILES} down ${SERVICE_NAME}
	docker compose ${LOCAL_COMPOSE_FILES} up ${SERVICE_NAME} --wait --no-recreate

down: ## Stops and removes all containers in the project.
	docker compose ${DEV_COMPOSE_FILES} down
	docker compose ${LOCAL_COMPOSE_FILES} down

build-api: ## Builds a production image of the API.
	docker-compose ${LOCAL_COMPOSE_FILES} build ${SERVICE_NAME}

dev-up: ## Starts/restarts the API in a development container. A remote debugger can be attached on port 5005.
	docker compose ${DEV_COMPOSE_FILES} down ${SERVICE_NAME}
	# Log the minio-init output for GitHub workflow debugging.
	docker compose ${DEV_COMPOSE_FILES} up minio-init --no-recreate
	docker compose ${DEV_COMPOSE_FILES} up ${SERVICE_NAME} --wait --no-recreate ${SERVICE_NAME}

dev-build: ## Builds a development image of the API.
	docker compose ${DEV_COMPOSE_FILES} build ${SERVICE_NAME}

dev-down: ## Stops and removes the API container.
	docker compose down

watch: ## Watches for file changes and live-reloads the API. To be used in conjunction with dev-up e.g. "make dev-up watch"
	docker compose ${DEV_COMPOSE_FILES} exec ${SERVICE_NAME} gradle compileKotlin --continuous --parallel --build-cache --configuration-cache

rebuild: ## Re-builds and reloads the API.
	docker compose ${DEV_COMPOSE_FILES} exec ${SERVICE_NAME} gradle compileKotlin --parallel --build-cache --configuration-cache

test: ## Runs all the test suites.
	docker compose ${DEV_COMPOSE_FILES} exec ${SERVICE_NAME} gradle test --parallel

lint: ## Runs the Kotlin linter.
	docker compose ${DEV_COMPOSE_FILES} exec ${SERVICE_NAME} gradle ktlintCheck --parallel

lint-fix: ## Runs the Kotlin linter and auto-fixes.
	docker compose ${DEV_COMPOSE_FILES} exec ${SERVICE_NAME} gradle ktlintFormat --parallel

clean: ## Stops and removes all project containers. Deletes local build/cache directories.
	docker compose down
	docker volume ls -qf "dangling=true" | xargs -r docker volume rm
	rm -rf .gradle build

update: ## Downloads the latest versions of containers.
	docker compose ${LOCAL_COMPOSE_FILES} pull

save-logs: ## Saves docker container logs in a directory defined by OUTPUT_LOGS_DIR=
	mkdir -p ${OUTPUT_LOGS_DIR}
	docker logs ${PROJECT_NAME}-api-1 > ${OUTPUT_LOGS_DIR}/api.log

psql: ## Opens a bash shell in the postgres container.
	docker compose ${DEV_COMPOSE_FILES} exec postgres psql -U postgres -d migration-link-exchange-db

