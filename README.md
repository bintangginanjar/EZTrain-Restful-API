# EZTrain Restful API

A robust, secure, and simple train ticket booking RESTful API built with Spring Boot.

## ✨ Features

-   User registration and JWT authentication
-   Manage train routes, stations, coaches, and seats
-   Role-based access control (User/Admin)
-   Swagger documentation (planned/future enhancement)

## 🛠️ Technologies Used

-   **Java 17**
-   **Spring Boot 3**
-   **Spring Security + JWT**: For secure authentication and authorization.
-   **H2 Database (Development)**: In-memory database for development and testing.
-   **PostgreSQL (Production)**: Relational database for production environments.
-   **Spring Data JPA**: For data persistence and ORM.

## 🚀 Getting Started

Instructions for setting up and running the project locally will be added here.

## 🧭 API Reference

> Base URL: `/api`

### 🔐 Auth

| Method | Endpoint | Description | Allowed User |
| ------ | -------- | ----------- | ------------ |
| POST   | `/auth/register` | Register new account | ALL |
| POST   | `/auth/login`    | Login & receive JWT | ALL |
| POST   | `/auth/forgot-password` | Request password reset token | ALL |
| POST   | `/auth/reset-password` | Reset password using token | ALL |

#### Register New Account

Endpoint : `POST /api/auth/register`

Request Body:

```json
{
    "email": "user@example.com",
    "password": "password123",
    "role": "USER"
}
```

Response Body:

```json
{
    "status": true,
    "messages": "User registration successful",
    "errors": null,
    "data": {
        "id": "generated-uuid",
        "email": "user@example.com",
        "role": "USER"
    },
    "paging": null
}
```

#### Login & Receive JWT

Endpoint : `POST /api/auth/login`

Request Body:

```json
{
    "email": "user@example.com",
    "password": "password123"
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Login successful",
    "errors": null,
    "data": {
        "email": "user@example.com",
        "token": "your-jwt-token",
        "tokenType": "Bearer ",
        "roles": [
            "USER"
        ]
    },
    "paging": null
}
```

#### Request Password Reset Token

Endpoint : `POST /api/auth/forgot-password`

Request Body:

```json
{
    "email": "user@example.com"
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Password reset token sent to email",
    "errors": null,
    "data": {
        "email": "user@example.com",
        "token": "generated-reset-token"
    },
    "paging": null
}
```

#### Reset Password Using Token

Endpoint : `POST /api/auth/reset-password`

Request Body:

```json
{
    "email": "user@example.com",
    "token": "generated-reset-token",
    "password": "new_password123"
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Password reset successful",
    "errors": null,
    "data": null,
    "paging": null
}
```

### 👤 Users

| Method | Endpoint | Description | Allowed User |
| ------ | -------- | ----------- | ------------ |
| POST   | `/users` | Create new user (Admin) | ADMIN |
| GET    | `/users` | Get current user | USER/ADMIN |
| PATCH  | `/users` | Update current user | USER/ADMIN |
| GET    | `/users/list` | List all users with pagination | ADMIN |

#### Create New User

Endpoint : `POST /api/users`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "email": "newuser@example.com",
    "password": "newpassword123",
    "role": "USER"
}
```

Response Body:

```json
{
    "status": true,
    "messages": "User registration success",
    "errors": null,
    "data": {
        "email": "newuser@example.com",
        "fullName": null,
        "phoneNumber": null,
        "isVerified": false,
        "isActive": true,
        "role": [
            "USER"
        ]
    },
    "paging": null
}
```

#### Get Current User

Endpoint : `GET /api/users`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "User fetching success",
    "errors": null,
    "data": {
        "email": "currentuser@example.com",
        "fullName": "Current User",
        "phoneNumber": "+628123456789",
        "isVerified": true,
        "isActive": true,
        "role": [
            "USER"
        ]
    },
    "paging": null
}
```

#### Update Current User

Endpoint : `PATCH /api/users`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body:

```json
{
    "password": "updatedpassword",
    "fullName": "Updated User Name",
    "phoneNumber": "+628123456780",
    "isVerified": true,
    "isActive": true
}
```

Response Body:

```json
{
    "status": true,
    "messages": "User successfully updated",
    "errors": null,
    "data": {
        "email": "currentuser@example.com",
        "fullName": "Updated User Name",
        "phoneNumber": "+628123456780",
        "isVerified": true,
        "isActive": true,
        "role": [
            "USER"
        ]
    },
    "paging": null
}
```

#### List All Users

Endpoint : `GET /api/users/list`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All users successfully fetched",
    "errors": null,
    "data": [
        {
            "email": "admin@example.com",
            "fullName": "Admin User",
            "phoneNumber": "+628111111111",
            "isVerified": true,
            "isActive": true,
            "role": [
                "ADMIN"
            ]
        },
        {
            "email": "user1@example.com",
            "fullName": "User One",
            "phoneNumber": "+628222222222",
            "isVerified": true,
            "isActive": true,
            "role": [
                "USER"
            ]
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

### 🚂 Trains

| Method | Endpoint | Description | Allowed User |
| ------ | -------- | ----------- | ------------ |
| POST   | `/trains` | Create new train | USER/ADMIN |
| GET    | `/trains/{trainId}` | Get train by ID | USER/ADMIN |
| PATCH  | `/trains/{trainId}` | Update train by ID | USER/ADMIN |
| DELETE | `/trains/{trainId}` | Delete train by ID | ADMIN |
| GET    | `/trains` | List all trains with pagination | USER/ADMIN |
| GET    | `/trains/search` | Search trains with pagination | USER/ADMIN |
| POST   | `/trains/{trainId}/coaches/{coachId}` | Assign coach to train | ADMIN |
| DELETE | `/trains/{trainId}/coaches/{coachId}` | Remove coach from train | ADMIN |

#### Create New Train

Endpoint : `POST /api/trains`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body:

```json
{
    "name": "Argo Wilis",
    "trainType": "EXECUTIVE",
    "operator": "KAI"
}
```

Response Body:

```json
{
    "status": true,
    "messages": "train registration success",
    "errors": null,
    "data": {
        "id": 1,
        "name": "Argo Wilis",
        "trainType": "EXECUTIVE",
        "operator": "KAI",
        "isActive": true,
        "coaches": []
    },
    "paging": null
}
```

#### Get Train by ID

Endpoint : `GET /api/trains/{trainId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Train fetching success",
    "errors": null,
    "data": {
        "id": 1,
        "name": "Argo Wilis",
        "trainType": "EXECUTIVE",
        "operator": "KAI",
        "isActive": true,
        "coaches": []
    },
    "paging": null
}
```

#### Update Train by ID

Endpoint : `PATCH /api/trains/{trainId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body:

```json
{
    "name": "Argo Lawu",
    "trainType": "BUSINESS",
    "operator": "KAI",
    "isActive": true
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Train update success",
    "errors": null,
    "data": {
        "id": 1,
        "name": "Argo Lawu",
        "trainType": "BUSINESS",
        "operator": "KAI",
        "isActive": true,
        "coaches": []
    },
    "paging": null
}
```

#### Delete Train by ID

Endpoint : `DELETE /api/trains/{trainId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "train delete success",
    "errors": null,
    "data": null,
    "paging": null
}
```

#### List All Trains

Endpoint : `GET /api/trains`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All trains successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "name": "Argo Lawu",
            "trainType": "BUSINESS",
            "operator": "KAI",
            "isActive": true,
            "coaches": []
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

#### Search Trains

Endpoint : `GET /api/trains/search`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `name`: (Optional) Train name to search for
*   `trainType`: (Optional) Train type to search for
*   `operator`: (Optional) Operator name to search for
*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All trains successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "name": "Argo Lawu",
            "trainType": "BUSINESS",
            "operator": "KAI",
            "isActive": true,
            "coaches": []
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

#### Assign Coach to Train

Endpoint : `POST /api/trains/{trainId}/coaches/{coachId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Train assigning coach success",
    "errors": null,
    "data": {
        "id": 1,
        "name": "Argo Lawu",
        "trainType": "BUSINESS",
        "operator": "KAI",
        "isActive": true,
        "coaches": [
            "Executive A"
        ]
    },
    "paging": null
}
```

#### Remove Coach from Train

Endpoint : `DELETE /api/trains/{trainId}/coaches/{coachId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Train removing coach success",
    "errors": null,
    "data": {
        "id": 1,
        "name": "Argo Lawu",
        "trainType": "BUSINESS",
        "operator": "KAI",
        "isActive": true,
        "coaches": []
    },
    "paging": null
}
```

### 🚉 Stations

| Method | Endpoint | Description | Allowed User |
| ------ | -------- | ----------- | ------------ |
| POST   | `/stations` | Create new station | ADMIN |
| GET    | `/stations/{stationId}` | Get station by ID | USER/ADMIN |
| PATCH  | `/stations/{stationId}` | Update station by ID | ADMIN |
| DELETE | `/stations/{stationId}` | Delete station by ID | ADMIN |
| GET    | `/stations` | List all stations with pagination | USER/ADMIN |
| GET    | `/stations/search` | Search stations with pagination | USER/ADMIN |

#### Create New Station

Endpoint : `POST /api/stations`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "code": "GMR",
    "name": "Gambir",
    "city": "Jakarta Pusat",
    "province": "DKI Jakarta"
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Station registration success",
    "errors": null,
    "data": {
        "id": 1,
        "code": "GMR",
        "name": "Gambir",
        "city": "Jakarta Pusat",
        "province": "DKI Jakarta",
        "isActive": true
    },
    "paging": null
}
```

#### Get Station by ID

Endpoint : `GET /api/stations/{stationId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Station fetching success",
    "errors": null,
    "data": {
        "id": 1,
        "code": "GMR",
        "name": "Gambir",
        "city": "Jakarta Pusat",
        "province": "DKI Jakarta",
        "isActive": true
    },
    "paging": null
}
```

#### Update Station by ID

Endpoint : `PATCH /api/stations/{stationId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "code": "GMI",
    "name": "Gambir International",
    "city": "Central Jakarta",
    "province": "DKI Jakarta",
    "isActive": true
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Station update success",
    "errors": null,
    "data": {
        "id": 1,
        "code": "GMI",
        "name": "Gambir International",
        "city": "Central Jakarta",
        "province": "DKI Jakarta",
        "isActive": true
    },
    "paging": null
}
```

#### Delete Station by ID

Endpoint : `DELETE /api/stations/{stationId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Station delete success",
    "errors": null,
    "data": null,
    "paging": null
}
```

#### List All Stations

Endpoint : `GET /api/stations`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All stations successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "code": "GMI",
            "name": "Gambir International",
            "city": "Central Jakarta",
            "province": "DKI Jakarta",
            "isActive": true
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

#### Search Stations

Endpoint : `GET /api/stations/search`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `code`: (Optional) Station code to search for
*   `name`: (Optional) Station name to search for
*   `city`: (Optional) City to search for
*   `province`: (Optional) Province to search for
*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Station search executed successfully",
    "errors": null,
    "data": [
        {
            "id": 1,
            "code": "GMI",
            "name": "Gambir International",
            "city": "Central Jakarta",
            "province": "DKI Jakarta",
            "isActive": true
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

### 🛤️ Routes

| Method | Endpoint | Description | Allowed User |
| ------ | -------- | ----------- | ------------ |
| POST   | `/routes` | Create new route | ADMIN |
| GET    | `/routes/origin/{originId}/destination/{destId}` | Get route by origin and destination ID | USER/ADMIN |
| PATCH  | `/routes/{routeId}` | Update route by ID | USER/ADMIN |
| DELETE | `/routes/{routeId}` | Delete route by ID | ADMIN |
| GET    | `/routes` | List all routes with pagination | USER/ADMIN |
| GET    | `/routes/search` | Search routes with pagination | USER/ADMIN |

#### Create New Route

Endpoint : `POST /api/routes`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "originId": 1,
    "destId": 2,
    "tripDistance": 150.5,
    "tripDuration": 2.5
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Route registration success",
    "errors": null,
    "data": {
        "id": 1,
        "originId": 1,
        "origin": "Gambir",
        "destId": 2,
        "destination": "Bandung",
        "tripDistance": 150.5,
        "tripDuration": 2.5
    },
    "paging": null
}
```

#### Get Route by Origin and Destination ID

Endpoint : `GET /api/routes/origin/{originId}/destination/{destId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Route fetching success",
    "errors": null,
    "data": {
        "id": 1,
        "originId": 1,
        "origin": "Gambir",
        "destId": 2,
        "destination": "Bandung",
        "tripDistance": 150.5,
        "tripDuration": 2.5
    },
    "paging": null
}
```

#### Update Route by ID

Endpoint : `PATCH /api/routes/{routeId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body:

```json
{
    "originId": 1,
    "destId": 3,
    "tripDistance": 200.0,
    "tripDuration": 3.0
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Route update success",
    "errors": null,
    "data": {
        "id": 1,
        "originId": 1,
        "origin": "Gambir",
        "destId": 3,
        "destination": "Yogyakarta",
        "tripDistance": 200.0,
        "tripDuration": 3.0
    },
    "paging": null
}
```

#### Delete Route by ID

Endpoint : `DELETE /api/routes/{routeId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Route delete success",
    "errors": null,
    "data": null,
    "paging": null
}
```

#### List All Routes

Endpoint : `GET /api/routes`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All routes successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "originId": 1,
            "origin": "Gambir",
            "destId": 2,
            "destination": "Bandung",
            "tripDistance": 150.5,
            "tripDuration": 2.5
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

#### Search Routes

Endpoint : `GET /api/routes/search`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `originCode`: (Optional) Origin station code to search for
*   `destCode`: (Optional) Destination station code to search for
*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All routes successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "originId": 1,
            "origin": "Gambir",
            "destId": 2,
            "destination": "Bandung",
            "tripDistance": 150.5,
            "tripDuration": 2.5
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

### 💵 Route Prices

| Method | Endpoint | Description | Allowed User |
| ------ | -------- | ----------- | ------------ |
| POST   | `/routeprices` | Create new route price | ADMIN |
| GET    | `/routeprices/{routePriceId}` | Get route price by ID | USER/ADMIN |
| PATCH  | `/routeprices/{routePriceId}` | Update route price by ID | ADMIN |
| DELETE | `/routeprices/{routePriceId}` | Delete route price by ID | ADMIN |
| GET    | `/routeprices` | List all route prices with pagination | USER/ADMIN |
| GET    | `/routeprices/search` | Search route prices with pagination | USER/ADMIN |

#### Create New Route Price

Endpoint : `POST /api/routeprices`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "coachTypeId": 1,
    "routeId": 1,
    "price": 150000.0
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Route price registration success",
    "errors": null,
    "data": {
        "id": 1,
        "price": 150000.0,
        "coachTypeId": 1,
        "coachType": "Executive",
        "routeId": 1,
        "origin": "Gambir",
        "destination": "Bandung"
    },
    "paging": null
}
```

#### Get Route Price by ID

Endpoint : `GET /api/routeprices/{routePriceId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Route price fetching success",
    "errors": null,
    "data": {
        "id": 1,
        "price": 150000.0,
        "coachTypeId": 1,
        "coachType": "Executive",
        "routeId": 1,
        "origin": "Gambir",
        "destination": "Bandung"
    },
    "paging": null
}
```

#### Update Route Price by ID

Endpoint : `PATCH /api/routeprices/{routePriceId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "coachTypeId": 1,
    "routeId": 1,
    "price": 175000.0
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Route price update success",
    "errors": null,
    "data": {
        "id": 1,
        "price": 175000.0,
        "coachTypeId": 1,
        "coachType": "Executive",
        "routeId": 1,
        "origin": "Gambir",
        "destination": "Bandung"
    },
    "paging": null
}
```

#### Delete Route Price by ID

Endpoint : `DELETE /api/routeprices/{routePriceId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Route price deletion success",
    "errors": null,
    "data": null,
    "paging": null
}
```

#### List All Route Prices

Endpoint : `GET /api/routeprices`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All route prices successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "price": 175000.0,
            "coachTypeId": 1,
            "coachType": "Executive",
            "routeId": 1,
            "origin": "Gambir",
            "destination": "Bandung"
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

#### Search Route Prices

Endpoint : `GET /api/routeprices/search`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `originCode`: (Optional) Origin station code to search for
*   `destination`: (Optional) Destination station name to search for
*   `coachType`: (Optional) Coach type to search for
*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All route prices successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "price": 175000.0,
            "coachTypeId": 1,
            "coachType": "Executive",
            "routeId": 1,
            "origin": "Gambir",
            "destination": "Bandung"
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

### 🚄 Coaches

| Method | Endpoint | Description | Allowed User |
| ------ | -------- | ----------- | ------------ |
| POST   | `/coaches` | Create new coach | ADMIN |
| GET    | `/coaches/{coachId}` | Get coach by ID | ADMIN |
| PATCH  | `/coaches/{coachId}` | Update coach by ID | ADMIN |
| DELETE | `/coaches/{coachId}` | Delete coach by ID | ADMIN |
| GET    | `/coaches` | List all coaches with pagination | ADMIN |
| GET    | `/coaches/search` | Search coaches with pagination | USER/ADMIN |
| POST   | `/coaches/{coachId}/seats/{seatId}` | Assign seat to coach | ADMIN |
| DELETE | `/coaches/{coachId}/seats/{seatId}` | Remove seat from coach | ADMIN |

#### Create New Coach

Endpoint : `POST /api/coaches`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "coachName": "Executive A",
    "coachNumber": 1,
    "coachTypeId": 1
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Coach registration success",
    "errors": null,
    "data": {
        "id": 1,
        "coachName": "Executive A",
        "coachNumber": 1,
        "coachTypeId": 1,
        "coachTypeName": "Executive",
        "seats": []
    },
    "paging": null
}
```

#### Get Coach by ID

Endpoint : `GET /api/coaches/{coachId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Coach fetching success",
    "errors": null,
    "data": {
        "id": 1,
        "coachName": "Executive A",
        "coachNumber": 1,
        "coachTypeId": 1,
        "coachTypeName": "Executive",
        "seats": []
    },
    "paging": null
}
```

#### Update Coach by ID

Endpoint : `PATCH /api/coaches/{coachId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "coachName": "Business B",
    "coachNumber": 2,
    "coachTypeId": 2
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Coach update success",
    "errors": null,
    "data": {
        "id": 1,
        "coachName": "Business B",
        "coachNumber": 2,
        "coachTypeId": 2,
        "coachTypeName": "Business",
        "seats": []
    },
    "paging": null
}
```

#### Delete Coach by ID

Endpoint : `DELETE /api/coaches/{coachId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Coach delete success",
    "errors": null,
    "data": null,
    "paging": null
}
```

#### List All Coaches

Endpoint : `GET /api/coaches`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All coaches successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "coachName": "Business B",
            "coachNumber": 2,
            "coachTypeId": 2,
            "coachTypeName": "Business",
            "seats": []
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

#### Search Coaches

Endpoint : `GET /api/coaches/search`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `coachName`: (Optional) Coach name to search for
*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All coaches successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "coachName": "Business B",
            "coachNumber": 2,
            "coachTypeId": 2,
            "coachTypeName": "Business",
            "seats": []
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

#### Assign Seat to Coach

Endpoint : `POST /api/coaches/{coachId}/seats/{seatId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Coach assigning seat success",
    "errors": null,
    "data": {
        "id": 1,
        "coachName": "Business B",
        "coachNumber": 2,
        "coachTypeId": 2,
        "coachTypeName": "Business",
        "seats": [
            "A1"
        ]
    },
    "paging": null
}
```

#### Remove Seat from Coach

Endpoint : `DELETE /api/coaches/{coachId}/seats/{seatId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Coach removing seat success",
    "errors": null,
    "data": {
        "id": 1,
        "coachName": "Business B",
        "coachNumber": 2,
        "coachTypeId": 2,
        "coachTypeName": "Business",
        "seats": []
    },
    "paging": null
}
```

### 💺 Seats

| Method | Endpoint | Description | Allowed User |
| ------ | -------- | ----------- | ------------ |
| POST   | `/seats` | Create new seat | ADMIN |
| GET    | `/seats/{seatId}` | Get seat by ID | USER/ADMIN |
| PATCH  | `/seats/{seatId}` | Update seat by ID | ADMIN |
| DELETE | `/seats/{seatId}` | Delete seat by ID | ADMIN |
| GET    | `/seats` | List all seats with pagination | USER/ADMIN |
| GET    | `/seats/search` | Search seats with pagination | USER/ADMIN |

#### Create New Seat

Endpoint : `POST /api/seats`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "seatNumber": "A1"
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Seat registration success",
    "errors": null,
    "data": {
        "id": 1,
        "seatNumber": "A1"
    },
    "paging": null
}
```

#### Get Seat by ID

Endpoint : `GET /api/seats/{seatId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Seat fetching success",
    "errors": null,
    "data": {
        "id": 1,
        "seatNumber": "A1"
    },
    "paging": null
}
```

#### Update Seat by ID

Endpoint : `PATCH /api/seats/{seatId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body:

```json
{
    "seatNumber": "B2"
}
```

Response Body:

```json
{
    "status": true,
    "messages": "Seat update success",
    "errors": null,
    "data": {
        "id": 1,
        "seatNumber": "B2"
    },
    "paging": null
}
```

#### Delete Seat by ID

Endpoint : `DELETE /api/seats/{seatId}`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Allowed User : ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "Seat delete success",
    "errors": null,
    "data": null,
    "paging": null
}
```

#### List All Seats

Endpoint : `GET /api/seats`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All seats successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "seatNumber": "B2"
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

#### Search Seats

Endpoint : `GET /api/seats/search`

Request Header :

*   Authorization : "Bearer " + Token (mandatory)

Request Parameters:

*   `seatNumber`: (Optional) Seat number to search for
*   `page`: Page number (default: 0)
*   `size`: Number of items per page (default: 10)

Allowed User : USER/ADMIN

Request Body: None

Response Body:

```json
{
    "status": true,
    "messages": "All seats successfully fetched",
    "errors": null,
    "data": [
        {
            "id": 1,
            "seatNumber": "B2"
        }
    ],
    "paging": {
        "currentPage": 0,
        "totalPage": 1,
        "size": 10
    }
}
```

Full API specification can be accessed through the Swagger UI (e.g., `http://localhost:8080/swagger-ui.html`) once the application is running.