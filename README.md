# Spring Boot Microservices Template

🚀 A production-ready Spring Boot microservices architecture template designed for hackathons and general-purpose applications. Get your microservices up and running in minutes!

## ✨ Features

- **🔍 Service Discovery**: Netflix Eureka for automatic service registration and discovery
- **🌐 API Gateway**: Spring Cloud Gateway for request routing and load balancing
- **🔐 Authentication Service**: Ready-to-extend auth service with JWT support
- **👥 User Management**: Complete user CRUD operations service

## 🏗️ Architecture Overview

This template provides a scalable microservices architecture perfect for hackathons and production applications:

```
                           ┌─────────────────┐
                           │   Load Balancer │
                           │   (Optional)    │
                           └─────────┬───────┘
                                     │
                           ┌─────────▼───────┐
                           │   API Gateway   │
                           │   (Port 8080)   │
                           └─────────┬───────┘
                                     │
                    ┌────────────────┼────────────────┐
                    │                │                │
          ┌─────────▼───────┐ ┌──────▼──────┐ ┌──────▼──────┐
          │  Auth Service   │ │Users Service│ │Service Reg. │
          │  (Port 8082)    │ │(Port 8083)  │ │(Port 8761)  │
          └─────────────────┘ └─────────────┘ └─────────────┘
                    │                │                │
                    └────────────────┼────────────────┘
                                     │
                           ┌─────────▼───────┐
                           │    Database     │
                           │   (Optional)    │
                           └─────────────────┘
```

## 📋 API Response Format

All services follow a **consistent, standardized API response format** for better frontend integration:

### ✅ Success Response
```json
{
  "message": "Operation completed successfully",
  "status": true,
  "error": false,
  "timestamp": "2025-10-02T12:30:45.123",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

### ❌ Error Response
```json
{
  "message": "Validation failed",
  "status": false,
  "error": true,
  "timestamp": "2025-10-02T12:30:45.123",
  "data": {
    "suggestion": "Please provide a valid email address",
  }
}
```

## 🚀 Quick Start (60 seconds setup!)

### Prerequisites
- ☕ **Java 21+** 
- 📦 **Maven 3.6+**
- 🐳 **Docker** (optional, for containerized deployment)

### Option 1: 🏃‍♂️ Local Development (Recommended for hackathons)

```bash
# 1. Clone the template
git clone https://github.com/xanderbilla/hackathon
cd spring-microservices-template

# 2. Copy environment file (optional - services work without database)
cp .env.hackathon .env

# 3. Start all services with one command!
./local-run.sh start

# 4. Verify all services are running
./local-run.sh health
```

### Manual Health Endpoints
```bash
# Service Registry
curl http://localhost:8761/api/v1/service/health

# API Gateway  
curl http://localhost:8080/health

# Auth Service
curl http://localhost:8082/api/v1/auth/health

# Users Service
curl http://localhost:8083/api/v1/users/health
```

## 🤝 Contributing

This template is designed to be:
- **Hackathon-friendly**: Quick setup, easy to extend
- **Production-ready**: Best practices, proper error handling
- **Educational**: Clean code structure, well-documented

Feel free to submit issues and enhancement requests!

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

- [Xander Billa](https://xanderbilla.com)