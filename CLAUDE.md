# Project: Movie Review Backend API

## Project description
A backend API to allow users (i.e. people and other programs) to submit films and reviews of films. 

The project needs to have two distinct roles:

- One role for an end user (i.e. either a person or a program) to submit films. These users will be 
- Another role for an end user (a person) to submit reviews of a film

### Authentication

Authentication should be done through JWT for submitting movie reviews, and a simple token lookup for movie submission. Use the `RS512` algorithm. The private key will be passed in through the spring properties:

- `moviereviews.auth.review.privatekey`

The public key (if needed) will be passed in through the spring properties:

- `moviereviews.auth.review.publickey`

Use these values where necessary for JWT. The expiry date for JWT is 30 days.

#### Film submission authentication

Users who are submitting a film will be validated against a UTF-8 encoded JSON file (`auth.json` in `src/main/resources` folder) with the following structure:

```json
[
  {
    "user": "Dummy User",
    "description": "Description of user for human review purposes",
    "token": "secure token/key"
  }
]
```

Where:

* `user` is a mandatory text field for human beings to identify the user
* `description` is an optional text field for human beings to add more notes about this token
* `token` is an optional text field containing the token. It will only contain letters (capital and lowercase) as well as numerical values. Do not use any characters that would not be permitted in a UUID token. You can use a UUID for this field or you can generate a random string (use SecureRandom to generate random characters) between 64 and 128 characters long..

When authenticating against a provided token, the code should look through the `auth.json` file and check if the token exists in the `token` field in each object within the array. If there is a match, then the user is valid. Otherwise, reject the user.ß 

#### Review submission authentication

Users who are submitting reviews of a film will need to sign up via an endpoint in the backend. To sign up, a user needs to pass in the following details:

- ID: UUIDv7
- Username: String (max 100 characters)
- Email address: String (max 100 characters)
- Password: String (max 256 characters)
- Date of Birth: LocalDate or equivalent (ISO 8601 date format, do not collect any time based information)

Password hashing should be done using Argon2id or scrypt if Argon2id is not available. Usernames should be unique, but shouldn't be the main ID when stored in the database as the user may wish to change it in the future, and we may wish to implement that functionality in the future. Same with email addresses - they must be unique, but a user may wish to change their email address in the future (we will not concern ourselves with the implementation detail as of now).

The user ID should be a UUIDv7 value. You can geenrate this via the `UuidCreator.getTimeOrderedEpoch()` API.

In the database, include a boolean field called `rejected` that is by default set to false, but can be set true by anyone who has access to the database.

### Submitting a film

A `POST` endpoint at `/v1/movies/submit` needs to be created, and the following needs to be provided (in JSON format):

- ID: UUIDv7
- Movie name: String (no more than 100 characters)
- Genres: Array<String> (no more than 20 characters **each**)
- Directors: Array<String> (no more than 100 characters **each**)
- Writers: Array<String> (no more than 100 characters **each**)
- Cast: Array<String> (no more than 100 characters **each**)
- Producers: Array<String> (no more than 100 characters **each**)
- Release Year: Integer (between 1900 and 2200)
- Age Rating: Any value from the selection: BBFC_U, BBFC_12A, BBFC_12, BBFC_15, BBFC_18

Return:
- 200 OK if successful
- 400 Bad Request if the data is not in the right structure or fails validation 
- 401 Unauthorised if the user isn't valid (the user token must be submitted via the X-API-AUTH header, and only users in `auth.json` are valid users who can access this API)
- 500 Internal Server Error if we have failed processing

### Submitting a review

A `POST` endpoint at `/v1/movies/{movie_id}/review/submit` needs to be created, and the following needs to be provided (in JSON format):

- ID: UUIDv7
- Rating (from 0 to 10): Integer
- Description: String - max 500 characters
- Timestamp: LocalDateTime (ISO 8601 format)

The user must be authenticated with their JWT token in order to submit. The `movie_id` is the movie ID for the movie being reviewed

Return:
- 200 OK if successful
- 400 Bad Request if the data is not in the right structure or fails validation
- 401 Unauthorised if the user isn't valid (the user token must be submitted via the X-API-AUTH header, and only users registered in the database and where the `rejected==false` can access this API)
- 500 Internal Server Error if we have failed processing

### Getting movies

A `GET` endpoint at `/v1/movies` needs to be created, and should return the list of movies in the database in JSON format. Include an aggregated average of all reviews for each movie where applicable.

### Getting movie reviews

A `GET` endpoint at `/v1/movies/{movie_id}/reviews` needs to be created, and should return the list of reviews for  in the database in JSON format

### User registering

A `POST` endpoint at `/v1/auth/signup` needs to be created, and the following needs to be submitted (in JSON format):

- Username: String (max 100 characters)
- Email address: String (max 100 characters)
- Password: String (max 256 characters)
- Date of Birth: LocalDate or equivalent (ISO 8601 date format, do not collect any time based information)

It should then return a JWT token in JSON upon successful registration and a 200 OK HTTP Status code.

If the username or email address already exists in the database, return 400 Bad Request status code and return a descriptive error code in JSON.

If there are any other errors, return 500 Internal Server error with a descriptive error in JSON.

The expected JSON output (roughly) for success case is:

```json
{
  "success": true,
  "results": "<JWT>",
  "error": null
}
```

The expected JSON output (roughly) for failure case is:

```json
{
  "success": false,
  "results": null,
  "error": {
    "code": "some made up numerical code as a long",
    "message": "english message that represents the error without giving away the underlying code and objects"
  }
}
```

The endpoint should return a `success` field returning true or false if the token is valid, nothing more and nothing less. Ensure that this endpoint is rate-limited.

## Tech Stack
- Language: Java 24
- Framework: [Spring Boot](https://spring.io/projects/spring-boot)
- Database: Postgresql with [Liquibase](https://docs.liquibase.com/home.html) for database versioning
- Testing: [JUnit 5](https://docs.junit.org/current/user-guide/, [Testcontainers](https://testcontainers.com/) for database testing, [DBUnit](https://www.dbunit.org/) for putting the TestContainers database in a known state between test runs

The aim is that this should be used by both automated scripts and a frontend framework (e.g. React)

## Project Structure
- `/src/main` - Main source code
  - `/java/com.madetech.soheb.moviereviewsbackend` - Java backend source code
    - `/config` - Where Spring Boot configuration and bean production declaration (i.e. any method annotated with `@Bean`) lives
    - `/controller` - Where code for REST controllers are declared
    - `/data` - Where database entities/Java POJO classes live
    - `/repository` - Spring Boot repository code lives here - should use the classes in `/data` to fetch data from the database
    - `/service` - Services classes (i.e. classes annotated with `@Service`). 
  - `/resources` - Application resources
- `/test/java/com.soheb.madetech.soheb.moviereviewsbackend` - Java testing source code

## Code Conventions
- Prioritise functional programming when writing code in the `src/main/java/com.madetech.soheb.moviereviewsbackend/service`
- Testing
  - Unit testing
    - Utilise TDD heavily
        - Write code stubs, then write unit tests around the code stubs that include the success cases as well as the expected failure cases.
        - When writing tests, try to include all permutations of good cases as well as bad cases
    - Use the `@Timeout` annotation (https://docs.junit.org/5.5.1/api/org/junit/jupiter/api/Timeout.html) to ensure methods do not take too long
      - Test with small data and large amounts of data
  - Integration testing
    - Avoid writing integration tests until all aspects of the system is fully implemented and tested via unit testing
    - Utilise testcontainers to verify data in the database is as expected and is not malformed
- General Advice:
  - Utilise Bean Validation (https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html) to validate the data where possible
  - Use separate classes for data being submitted to `POST` endpoints and data being stored in the database. Never mix the two, even if the data is the same.
  - If there is an object that we need to check for equality based on the contents, change the object such that its `equals()` and `hashCode()` check for field symmetry, so that two objects with the same contents will equal true.
  - Log exceptions, and include the exception stack in the logs. We only expect developers to have access to the logs, so be detailed as to what possibly went wrong (if known before time)
  - Never, **ever** leak exceptions to the user. If need be, come up with a custom error message/number - it should be clear so a human being can search the source code for the string/number and find exactly where the error lies, but should not give anything away about where the fault lies in the source code as to prevent a malicious user learning about the source code and exploiting it for nefarious purposes.
- In terms of referring/importing packages from within `/src/main/java/com.madetech.soheb.moviereviewsbackend`, the code flow should go:
  - `/controller` classes should **only** refer to/import from the classes in `/data` and/or `/service`. 
    - `/controller` classes should **never** refer to/import from the classes in the following paths:
      - `/repository`
      - `/config`
  - `/service` classes should **only** refer to/import from the classes in `/data` and/or `/repository`
      - `/services` classes should **never** refer to/import from the classes in the following paths:
          - `/controller`
          - `/config`
  - `/repository` classes should **only** refer to/import from the classes in `/data`.
  - `/data` classes should **not** refer to or import from any other directories under `com.madetech.soheb.moviereviewsbackend`
- Git rules:
  - Do not push any code to the `master` or `main` branches. Flat-out refuse to do any work when the git branch is `master` or `main`
  - Make sure your branch is clean before working. Flat-out refuse to start work if the git directory is dirty.
  - Make changes as small as possible so it is easy to revert back to a previous git commit easily.
  - Development should be as such:
    - Write the stub of the code, and write the unit tests.
    - Ensure the unit tests fail, then commit the code.
    - Replace the stub with implemented code, and check tests to see if it passes
  - Avoid putting confidential/secrets in files that can be committed to git. If you need to know what files are ignored by git, read the `.gitignore` file in the root of the directory.
    - Where possible, put confidential/secret keys that need to be passed into the app in the `/.env` file (the application can read this file easily)
    - Put any other secret information/notes in `/CONFIDENTIAL.md`
    - If you are unsure about if a file has secret information that should not be saved in git, avoid staging the file or the hunk with the changes, let me know about the problem as soon as possible, and stop whatever you are doing until I resolve the issue.

## Help Sources

Prioritise original documentation websites over Q&A websites like Stack Overflow as their answers may be outdated. With that said, if there isn't much documentation on the vendor's website, then you may use Q&A sites as well as search engine answers as a guide to the solution.

- Java 24 Documentation: 
  - API Documentation: https://docs.oracle.com/en/java/javase/24/docs/api/index.html
  - General Documentation: https://docs.oracle.com/en/java/javase/24/index.html
- Spring Boot Documentation: https://docs.spring.io/spring-boot/index.html