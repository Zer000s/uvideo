# 📺 uVideo

uVideo is a video-sharing platform similar to YouTube.  
It allows users to register, upload videos, watch content, and interact with other users.  
The backend is built with **Spring Boot**, **Spring Security (JWT Auth)**, and **JPA**.

---

## ✨ Features

- 🔐 **Authentication & Authorization**
  - JWT-based authentication (Access + Refresh tokens)
  - Access token is returned in `Authorization: Bearer ...`
  - Refresh token is stored securely in `HttpOnly` cookies

- 👤 **User Management**
  - Registration & login via phone number
  - User profile with display name & avatar
  - Secure password hashing with `BCrypt`

- 🎬 **Video**
  - Upload and store video metadata
  - (planned) Streaming support
  - (planned) Likes, comments, subscriptions

- ⚡ **Tech Stack**
  - **Java 17+**
  - **Spring Boot 3**
  - **Spring Security 6 (JWT)**
  - **JPA / Hibernate**
  - **PostgreSQL** (or any SQL DB)
  - **Maven**
