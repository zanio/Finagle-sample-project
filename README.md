# Finagle sample project
A sample project to demonstrate how to use finagle-http in communicating with external services. This application is a REST API that communicates with an external service called nytimes

## Requirements
* Java 11
* SBT
* Docker
* Docker Compose

## Running the application
1. Start a Redis server  
    ```
    $ docker-compose up -d    
    ```
2. Check the `env.example` file for the required environment variables and make sure they are set
   
3. Start the API server

    ```sbt run```
4. Run the tests

    ```sbt test```

## API Documentation
The application has 2 endpoints, the health endpoint and the fetch books endpoint

| Method | Endpoint                            | Description                                           |
|--------|-------------------------------------|-------------------------------------------------------|
| GET    | `//me/books/list?author={author}&year={TheYearThatBookWasPublished}` | Fetches books based on author and year of publication |
| GET    | `/health`                           | Health endpoint to check the status of the application |

NB: The `year` query parameter is optional, and it can be a single year or comma separated years
