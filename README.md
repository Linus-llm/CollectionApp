## Vision

> This project is a backend API for managing collection of items, so a collection tracker.
> The system allows the user to track various items, their state, what they are and so on.

---

## Links

Portfolio website:
https://linus-llm.github.io/portfolio/

Project overview video:
https://youtu.be/UKMt0CBDMnY

Project overview video updated:
https://youtu.be/VBFetYs8JJo

Deployed application: 
https://collectionapp.viskode.dk/

Source code repository: 
https://github.com/Linus-llm/CollectionApp

User Stories:
https://docs.google.com/document/d/1oqn2AmLk_DcwgcQjMemViZKaxjYk5F6qZpMbKdkntn4/edit?usp=sharing

---

# Architecture

- Controller layer (REST endpoints)
- Service layer (business logic)
- Persistence layer (DAOS)

The technologies I have used:

- Java 17.0.16
- Javalin 6.7.0
- JPA/Hibernate 7.1.0
- PostgreSQL 16.2
- Maven 4.0.0
- JWT authentication 1.0.4 (TokenSecurity - Hartmannsolution)
- Hamcrest 2.2
- Restassured 6.0.0
- Bcrypt 0.4

The overview: 


Client --> Controller (security layer) --> Controllers (REST) --> DAOs (Persistence layer) --> Database 
--> DAOs (Persistence layer) --> Controllers (REST) --> Client

This below is for my service class it is meant to be like a guide for me when frontend begins.

Developer/User --> Service --> external API --> Service --> Developer/User --> Service --> persistence layer --> database

---

## Key Design Decisions

Structure:
I choose this structure for my project since it provices a lot of overview and scaleability. It is very layered which makes it easier to maintain and swap if needed. It is close to the MVC architecture just without the view for now. 

Authentication:
The authentication is implemented using JWT. 
Clients either register and login or login if they already have a registered user.
After login clients receive a token which must be included to gain access to the other endpoints of the system. 

Error handling:
REST Errors are handled inside the controllers. If the input they get is null or empty it will throw either a validationException or an ApiException or a third if that has managed to bubble its way up from the persistence layer. Because I have three exception handlers in my Main class on my app instance those three will be the one putting the exception into the context object as JSON.

The security layer will usually throw a ApiException which has a status code and a message as JSON to the client. If its not an ApiException then it is a ValidationException which will return a code 400 with a message in JSON to the client.


API:

I have chosen the OpenLibrary api because it fits my project perfectly, I can take the information from they provide and use it in mine to fill out a specific type of item. The plan is to drag more API's into the project so I can make more specific type of items like I can with books at the moment.

---

## Data model

# ERD

![ERD](images/ERD%20portfolio.png)

---

## Important Entities

### users

Represents a registered user in the system.

Fields:

- id
- email
- password
- username

### role

Represents a user role

Fields:

- id
- role_name

### collections

Represents a collection owned by a user

Fields:

- id
- user_id
- createdat
- description
- name

### items

Represents an item in a collection owned by a user

Fields:

- id
- collection_id
- releaseYear
- createdAt
- condition
- description
- name
- status
- type

### books

Represents a specific item in a collection owned by a user

Fields:

- id
- author

---

# API Documentation

## Overview

Everything is gathered under /api and for my register, login, authorization and authentication that is gathered under /api/auth.
I have tried to maintain a hierarchy. Most endpoints are protected from other users. See the routes below:

/api/auth/register
/api/auth/login
/api/user
/api/user/{userId} (get, put, delete)
/api/user/{userId}/collection (get, post)
/api/collection/{collectionId} (get, put, delete)
/api/item/{itemId} (get, put, delete)
/api/collection/{collectionId}/item (get, post)
/api/collection/{collectionId}/book (post)

## Example Endpoints

### Register user

POST /api/auth/register
Content-Type: application/json

Request body:
{
  "username": "xxx",
  "password": "xxx",
  "email": "xxx"
}
Response:
201 Created
{
  "msg": "xxx",
  "id": xx,
  "username": "xxx"
}

### Login
POST /api/auth/login
Content-Type: application/json

Request body:
{
  "username": "VideoUser",
  "password": "TestPassword123"
}
Response:
200 OK
{
  "token": "123434546346456.eyJpc3MiOiJMaW51cyBMb2htYW5uIE1vbGdquwefhqwihfiqhwheifqwe.2-eJKFYp_apcINGzykLmNUw_i0LqvTEj7Nt58Nb7r9U",
  "username": "xxx"
}
### Get collections for user
GET {{baseUrl}}/api/user/{userId}/collection
Authorization: Bearer {{token}}

Request body:


Response:
200 OK
[
  {
    "id": x,
    "name": "xxx",
    "description": "xxx"
  }
]

### Create collection for user
POST {{baseUrl}}/api/user/{userId}/collection
Authorization: Bearer {{token}}

Request body:
{
  "name": "xxx",
  "description": "xxx"
}

Response:
201 Created
{
  "id": x,
  "name": "xxx",
  "description": "xxx"
}

### Create book for collection
POST {{baseUrl}}/api/collection/{collectionId}/book
Authorization: Bearer {{token}}

Request body:
{
  "title": "xxx",
  "description": "xxx",
  "authors": ["xxx","xxx"],
  "releaseYear": xxxx,
  "status": "XXXXX",
  "condition": "XXXX"
}

Response:
{
  "id": x,
  "name": "xxx",
  "description": "xxx",
  "createdAt": [
    xxxx,
    x,
    x,
    xx,
    xx,
    xx,
    xxxxxxxx
  ],
  "releaseYear": xxxx,
  "type": "XXXX",
  "status": "XXXX",
  "condition": "XXXX",
  "collectionId": x
}

### Create item for collection
POST {{baseUrl}}/api/collection/{collectionId}/item
Authorization: Bearer {{token}}

Request body:
{
  "title": "xxx",
  "description": "xxx",
  "authors": ["xxx","xxx"],
  "releaseYear": xxxx,
  "status": "XXXXX",
  "condition": "XXXX"
}

Response:
{
  "id": x,
  "name": "xxx",
  "description": "xxx",
  "createdAt": [
    xxxx,
    x,
    xx,
    xx,
    x,
    x,
    xxxxxx
  ],
  "releaseYear": xxxx,
  "type": "xxxxxx",
  "status": "xxxx",
  "condition": "xxxx",
  "collectionId": x
}

---

# User Stories

My most important User Stories:

- As a user, I want to be able to register a user.
- As a user, I want to log in and receive a token.
- As system owner, I want to protect API endpoints with roles, so only authorized users can access
- As a user, I want to create, see, update and delete items, so I can maintain my collection
- As a user, I want to create, see, update and delete collections, so I can organize my items.
- As a user, I want to create a book as an item in my collection. 
- As a user, I want to create an item in my collection.

---

# Development Notes

- Testing the API turned out to be more difficult than I first imagine and I used a significant amount of time doing so, but it brought me a lot of learning and experience.
- I had a hard time settling on where to take my idea, and I did so late weeks into the project which might have halted how far I have gone. Next time I will use more time planning and being sure on where to take the project.
- JWT and the whole authentication and authorization part turned out to be really fun and interesting, might be my favourite part of the project.
- In order ot run the Main class make sure that Config.Properties have DB_NAME, DB_USERNAME and DB_PASSWORD.

---

# Things I have not incoporated into the project yet

- Admin role is still missing and still contemplating on what I want to do with it. This means the SecurityDao.createRole and addUserRole is not being used and has not been coded.
- I have yet to introduce more API's like OpenLibrary into the project