# Infinite Synergy test task

## Running the app
Application uses Java 17, Maven and Docker

To create executable JAR archive run:
```console
mvn clean package
```

Then run docker containers with the following command:
```consioe
docker-compose up
```

The serer will be accessible on http://localhost:8080.

## Endpoints
### POST /signup
```json
{
  "login": "login",
  "password": "password"
}
```

Creates new user and signs them in. Response will contain JWT access and refresh tokens:
```json
{
    "accessToken": "token string",
    "refreshToken": "token string"
}
```
<br>

### POST /signin
```json
{
  "login": "login",
  "password": "password"
}
```

Signs user in. Response will contain JWT access and refresh tokens:
```json
{
    "accessToken": "token string",
    "refreshToken": "token string"
}
```
<br>

### POST /token
```json
{
    "refreshToken": "token string"
}
```

Creates new access token for user. Response will contain JWT access and refresh tokens:
```json
{
    "accessToken": "token string",
    "refreshToken": null
}
```
<br>

### POST /refresh
```json
{
    "refreshToken": "token string"
}
```

Creates new access and refresh tokens for user. Response will contain JWT access and refresh tokens:
```json
{
    "accessToken": "token string",
    "refreshToken": "token string"
}
```
<br>

### GET /money
Requires authorization.
Requests user's current balance. Response will contain balance fixed point value:
```json
{
    "balance": 0.0
}
```
<br>

### POST /money
```json
{
    "to": "receiver username",
    "amount": 0.0
}
```

Requires authorization.
Transfers the specified amount of money to another user. Response updated balance fixed point value:
```json
{
    "balance": 0.0
}
