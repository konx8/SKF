# SKF - Movie Ranking System

![Build](https://github.com/konx8/SKF/actions/workflows/ci.yml/badge.svg)
[![codecov](https://codecov.io/gh/konx8/SKF/branch/main/graph/badge.svg?token=TWÓJ_TOKEN)](https://codecov.io/gh/konx8/SKF)

## Project Description
This project calculates movie rankings based on external data (DigiKat) and analysis.  
The application is built with Java 21 and Spring Boot 3.5.4, using Feign Client for communication.

## About the Project

**System Kategoryzacji Filmów** is a web application written in Java (Spring Boot), designed to enable users to:

- add and edit movie information (title, director, year, file size),
- automatically categorize and calculate movie rankings based on data retrieved from the external **DigiKat** service,
- sort and filter movies by ranking and file size,
- download movies to the local disk,
- maintain a high test coverage (95%) and run entirely in Docker containers.

The project integrates with an external service **DigiKat** (REST API) to retrieve production type, platform availability, and user ratings for each movie.

## API Endpoints

- `GET /skf/movies` – list movies with filters and pagination  
  **Query parameters:**
    - `sort` (optional) – sort criteria, e.g. `size`, `ranking`
    - `page` (optional) – page number for pagination
    - `size` (optional) – number of items per page

- `POST /skf/movies` – add a new movie  
  **Consumes:** `multipart/form-data`  
  **Request parts:**
    - `movie` (JSON) – movie details (title, director, year, etc.)
    - `file` (file) – movie file upload
    - `userId` (number) – ID of the uploading user

- `GET /skf/movies/{id}/ranking` – get movie ranking by ID

- `PATCH /skf/movies/{id}` – update movie details  
  **Consumes:** `application/json`  
  **Body:** partial movie data to update

- `POST /skf/movies/{id}/download` – download movie to disk


## Quick Start

1. Clone the repo
2. Start Docker services: docker-compose up -d
3. Run the app: mvn spring-boot:run
4. Open http://localhost:8080


## Technologies
- Java 21
- Spring Boot 3.5.4
- Maven
- JUnit 5 + Mockito
- H2 (in-memory test database)
- JaCoCo (test coverage measurement)
- GitHub Actions (CI/CD)
- Codecov (test coverage reporting)
- Docker + Docker Compose (for database and mocked services)

## Architecture & Design

- **Adapter Pattern** – used to transform data retrieved from the external DigiKat service into our internal movie ranking model.
- **Factory Pattern** – allows dynamic creation of ranking calculation objects based on the type of input data.
- **Feign Client** – used for communication with the external DigiKat API, enabling clean and declarative code.
- **Multithreading** – rankings for multiple movies are calculated in parallel using a thread pool (`ExecutorService`) to improve performance.
- **Pagination** – the API supports pagination for efficient handling of large movie datasets.
- **Mocked DigiKat Service** – the external DigiKat service is mocked and runs in a dedicated Docker container, allowing independent and repeatable testing.
- **Database in Docker** – the main database (e.g., PostgreSQL) runs in a Docker container for easy local environment setup.
- **In-memory H2 Database for Tests** – unit tests use an in-memory H2 database for fast and isolated testing.
- **JaCoCo + Codecov** – test coverage is measured locally using JaCoCo and reported to Codecov for online tracking.
- **GitHub Actions CI/CD** – automatic build, test execution, and code coverage reporting on every commit and pull request.



## Requirements
- Java 21 or higher
- Maven 3.6 or higher
- Docker and Docker Compose (for running dependencies)

## Running Locally

### Start required services with Docker Compose:
```bash
    docker-compose up -d
```
This will start:

- The database container
    
- The mocked DigiKat service container

### Run the application using Maven:
```bash
    mvn spring-boot:run
```
Then access the application at the configured port (default: http://localhost:8080).

### Stopping Docker services and remove stored data:

```bash
    docker-compose down -v
```

### Testing and Coverage

Run tests and generate coverage reports with:
```bash
    mvn clean verify
```

The CI pipeline (GitHub Actions) uploads coverage data to Codecov for online visualization.

## Continuous Integration / Continuous Deployment (CI/CD)

- The project uses GitHub Actions workflows for building, testing, and reporting code coverage automatically on each push or pull request.

- JaCoCo generates coverage reports, which are then uploaded to Codecov.

- Codecov provides an online dashboard to track coverage trends and identify untested code.

## Customization
- You can modify the Docker setup by editing the docker-compose.yml file.

- Update Maven settings or dependencies in the pom.xml.

- Configure application properties in src/main/resources/application.properties or as environment variables.

## Useful Links

- [Codecov Dashboard](https://app.codecov.io/github/konx8/SKF)
- [GitHub Actions Workflow](https://github.com/konx8/SKF/actions)