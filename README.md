# Prime-Finder

This Spring Boot application searches for prime numbers in coroutines and lists them via REST endpoints.

## Endpoints:
OpenAPI description available after starting the application at: `http://localhost:8080/api-docs`<br/>
SwaggerUI available after starting the application at: `http://localhost:8080/swagger-ui/index.html`
- **Start** <br/>
Starts searching for prime numbers in the background. Previous results are deleted.<br/>
    - Endpoint: `/api/start`
    - Query parameters:
        - `threads`: number, specifies how many threads to use to search for prime numbers.
    - Response:
        - 200 OK: Searching has successfully started.
        - 400 BAD REQUEST: there is a validation error.
    - Possible validation errors:
        - Parameter `threads` is smaller than 1.
        - Parameter `threads` is greater than the maximum threshold defined in the property file as `maxThreadsToUse`.
        - Search is already in progress.
- **Stop** <br/>
Stops running threads searching for prime numbers.<br/>
    - Endpoint: `/api/stop`
    - Response:
        - 200 OK: Stopping threads has successfully started.
        - 400 BAD REQUEST: there is a validation error.
    - Possible validation errors:
        - Search is not in progress.
- **List** <br/>
Lists found prime numbers. Usable while search is running, or after stopped.<br/>
    - Endpoint: `/api/list`
    - Query parameters:
        - `min`: number, specifies the start of the interval to retrieve prime numbers from.
        - `max`: number, specifies the end of the interval to retrieve prime numbers up to.
    - Response:
        - 200 OK: Returns a list of prime numbers in the given interval.
        - 400 BAD REQUEST: there is a validation error.
    - Possible validation errors:
        - Parameter `min` is smaller than 1.
        - Parameter `max` is smaller than 1.
        - Parameter `min` is greater than parameter `max`.
        - Searching hasn't progressed up to `max` yet.

## Data storage
Found prime numbers are stored in memory in H2 database. To acccess the database directly while the application is running, visit `http://localhost:8080/h2-console/`. <br/> 
Login details are stored in the property file.
- Username: `sa`
- Password: `password`
- JDBC URL: `jdbc:h2:mem:primedb`

The database consist of one table, named `PRIME_NUMBERS` which has one column named `PRIME_NUMBER` which holds all the prime numbers the application has found.

## Executing
To run the Spring Boot application go to the root directory of the project and run `./gradlew bootRun`<br/>
To run tests run `./gradlew cleanTest test`. Run with `-i` flag for more information.<br/>
Test report file can be found at `{projectRoot}/build/reports/tests/test/index.html`

