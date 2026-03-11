# ISC - International Student Community

![Status](https://img.shields.io/badge/status-in%20development-blue)
![License](https://img.shields.io/badge/license-MIT-lightgrey)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)

ISC (International Student Community) is a platform for students from all over the world.  
Its main purpose is to help students **find support, share knowledge, and connect with others**.

The project combines social features, messaging, notifications, and student-oriented interaction in one place.

---

## Overview

ISC is designed as a community platform where students can:

- ask for and offer help on different topics
- build connections with other students
- share posts and updates
- chat in real time
- receive notifications

In short, it is a student community hub.

---

## Tech Stack

- **Java 21**
- **Maven 3.9+**
- **Spring Boot 4**
- **Spring Security**
- **Hibernate / JPA**
- **PostgreSQL**
- **WebSocket + STOMP**
- **OpenAPI 3 / Swagger UI**
- **JUnit 5**

---

## Security

The application uses **Spring Security** to handle authentication and access control.

- Secure login and authorization flow
- Password hashing with **BCrypt**
- Centralized security configuration via `SecurityConfig`

---

## Features

### Social System
- Friend requests with statuses:
  - `PENDING`
  - `ACCEPTED`
  - `DECLINED`
- Subscriptions and subscribers
- Basic user-to-user social interactions

### Posts
- Create text posts
- Upload photos
- Audio and video support is planned
- Posts can appear on a profile page or in the feed based on friendships and subscriptions

### Messenger
- Implemented with **WebSocket** and **STOMP**
- Real-time messaging
- Direct chats
- Group conversations
- Channels

### Notifications
Users receive notifications when someone:

- sends a message
- adds or removes them from a conversation
- likes or comments on a post
- subscribes to their profile
- sends a friend request

### User Status
- `ONLINE`
- `IDLE`
- `OFFLINE`

User status is displayed in profiles and in the messenger.

### OpusCore
A planned rating platform for:

- music
- games
- films

It is intended to provide a more thoughtful scoring system focused on meaningful criteria.

---

## Project Status

The project is currently **in development**.

Some core modules are already implemented, while others are being improved or are still planned.

### Current focus
- improving existing social features
- extending messaging and notifications
- polishing API structure
- preparing additional platform modules such as **OpusCore**

---

## Getting Started

### Requirements

Make sure you have installed:

- **Java 21**
- **Maven 3.9+**
- **PostgreSQL**
- an IDE such as **IntelliJ IDEA**

---

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/your-username/isc.git
cd isc
```
### 2. Configure the database

Create a PostgreSQL database, for example:

```bash
CREATE DATABASE isc_db;
```

Edit the provided `.env.properties` in the project root and fill in your real credentials.

Example:

```properties
DB_URL=jdbc:postgresql://localhost:5432/isc_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
APP_SECRET=your-app-secret
```

`application.properties` reads these values automatically, and `.env.properties` is ignored by git.

### 3. Build the project

```bash
mvn clean install
```

### 4. Run the application

```bash
mvn spring-boot:run
```

Or run the main Spring Boot class directly from your IDE.

## Possible Future Improvements

- media support for audio and video posts

- better recommendation/feed logic

- richer profile customization

- moderation tools

- expanded OpusCore rating system

- internationalization / multilingual support

## Author

Developed as a personal backend project focused on building a real student community platform with modern Java and Spring technologies.
