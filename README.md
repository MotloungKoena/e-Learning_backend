# E-Learning Platform - Backend API

## 🚀 Overview

Backend API for the E-Learning Platform built with Spring Boot. Handles user authentication, course management, enrollments, payments, and email notifications.

## 🛠️ Tech Stack

- Java 17
- Spring Boot 4.0.3
- MySQL 8.0
- Spring Security with JWT
- Stripe API (Payments)
- Gmail SMTP (Emails)

## ✨ Features

- JWT Authentication with 3 roles (Student, Instructor, Admin)
- Course CRUD operations
- Enrollment & progress tracking
- Course materials upload
- Ratings & reviews
- Stripe payment integration
- Email verification & password reset
- PDF certificate generation
- Admin user management

## 🚀 Getting Started

### Prerequisites

- Java 17
- MySQL 8.0
- Maven
- Stripe Account (for payments)
- Gmail Account (for emails)

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/MotloungKoena/e-Learning_backend.git


### **Create MySQL Database**
CREATE DATABASE elearning_db;


### **Configure application.properties**
1. spring.datasource.username=root
2. spring.datasource.password=your_password
3. spring.mail.username=your_email@gmail.com
4. spring.mail.password=your_app_password


### **Set Stripe environment variables**

STRIPE_PUBLISHABLE_KEY=pk_test_...

STRIPE_SECRET_KEY=sk_test_...


-Run the application 