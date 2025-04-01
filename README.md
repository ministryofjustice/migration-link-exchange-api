# migration-link-exchange-api

[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fmigration-link-exchange-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/migration-link-exchange-api "Link to report")
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/migration-link-exchange-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://migration-link-exchange-api-dev.hmpps.service.justice.gov.uk/webjars/swagger-ui/index.html?configUrl=/v3/api-docs)

`migration-link-exchange-api` is a Kotlin + Spring Boot service for ingesting, transforming, 
and serving file migration metadata between Google Drive and Microsoft OneDrive/SharePoint as
part of the Google Migration project.

The API ingests a unified dataset that links Google files to one or more Microsoft files — allowing users 
to trace how files have been migrated across platforms, even in non-linear or 
graph-like cases (e.g. Google shortcuts and shared folders).

---

## Features

- **Handle data migration mapping**
    - Supports one Google file to many Microsoft file relationships
- **CSV import from remote URL**
    - Download and ingest file mappings from external systems
    - Imports CSV migration data only if the file contents have changed (via SHA‑256 checksum)
- **JPA-backed persistence**
    - Store relationships between files
    - Tables/Schema created automatically on startup
---

## Running the application
### Prerequisites
- Docker - This service and all of its dependencies are run in Docker containers.
- make - Make is used for building and developing locally

**Note:** Every command can be printed using `make`

### Production
1. To start a production version of the application, run `make up`
    - The service will start on http://localhost:8080
    - To check the health status, go to http://localhost:8080/health
2. To update all containers, run `make down update up`

### Development
1. To start a development version of the application, run `make dev-up`
    - The service will start on http://localhost:8080
    - A debugger session will be accessible on http://localhost:5005
    - To check the health status, go to http://localhost:8080/health
2. To enable live-reload, run `make watch`, the API will now restart each time you change the code.

You can connect to the remote debugger session on http://localhost:5005 like so
[![API docs](https://github.com/ministryofjustice/hmpps-strengths-based-needs-assessments-api/blob/main/.readme/debugger.png?raw=true)]()

### Testing
The test suite can be ran using `make test`

### Linting
Linting can be ran using `make lint` and `make lint-fix`