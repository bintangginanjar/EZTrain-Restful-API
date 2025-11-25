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
| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register new account |
| POST | `/auth/login` | Login & receive JWT |
| POST | `/auth/forgot-password` | Request password reset token |
| POST | `/auth/reset-password` | Reset password using token |

### 👤 Users
| Method | Endpoint | Description |
|---|---|---|
| GET | `/users` | List all users (Admin) |
| GET | `/users/{id}` | Get user by ID (Admin) |
| PUT | `/users/{id}` | Update user by ID (Admin) |
| DELETE | `/users/{id}` | Delete user by ID (Admin) |

### 🚂 Trains
| Method | Endpoint | Description |
|---|---|---|
| GET | `/trains` | List all trains |
| POST | `/trains` | Create new train (Admin) |
| GET | `/trains/{id}` | Get train by ID |
| PUT | `/trains/{id}` | Update train by ID (Admin) |
| DELETE | `/trains/{id}` | Delete train by ID (Admin) |

### 🚉 Stations
| Method | Endpoint | Description |
|---|---|---|
| GET | `/stations` | List all stations |
| POST | `/stations` | Create new station (Admin) |
| GET | `/stations/{id}` | Get station by ID |
| PUT | `/stations/{id}` | Update station by ID (Admin) |
| DELETE | `/stations/{id}` | Delete station by ID (Admin) |

### 🛤️ Routes
| Method | Endpoint | Description |
|---|---|---|
| GET | `/routes` | List all routes |
| POST | `/routes` | Create new route (Admin) |
| GET | `/routes/{id}` | Get route by ID |
| PUT | `/routes/{id}` | Update route by ID (Admin) |
| DELETE | `/routes/{id}` | Delete route by ID (Admin) |

### 💵 Route Prices
| Method | Endpoint | Description |
|---|---|---|
| GET | `/route-prices` | List all route prices |
| POST | `/route-prices` | Create new route price (Admin) |
| GET | `/route-prices/{id}` | Get route price by ID |
| PUT | `/route-prices/{id}` | Update route price by ID (Admin) |
| DELETE | `/route-prices/{id}` | Delete route price by ID (Admin) |

### 🚄 Coaches
| Method | Endpoint | Description |
|---|---|---|
| GET | `/coaches` | List all coaches |
| POST | `/coaches` | Create new coach (Admin) |
| GET | `/coaches/{id}` | Get coach by ID |
| PUT | `/coaches/{id}` | Update coach by ID (Admin) |
| DELETE | `/coaches/{id}` | Delete coach by ID (Admin) |

### 💺 Seats
| Method | Endpoint | Description |
|---|---|---|
| GET | `/seats` | List all seats |
| POST | `/seats` | Create new seat (Admin) |
| GET | `/seats/{id}` | Get seat by ID |
| PUT | `/seats/{id}` | Update seat by ID (Admin) |
| DELETE | `/seats/{id}` | Delete seat by ID (Admin) |

Full API specification can be accessed through the Swagger UI (e.g., `http://localhost:8080/swagger-ui.html`) once the application is running.
