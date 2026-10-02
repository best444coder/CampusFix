# CampusFix — Java Web Application

A modern college complaint management system built with Java, Spring Boot, Thymeleaf, JPA and H2.

## Requirements
- Java 25+
- Maven 3.9+

## Run
```bash
mvn spring-boot:run
```

Open: http://localhost:8080

## Demo accounts
Student:
- Email: student@campusfix.com
- Password: student123

Admin:
- Email: admin@campusfix.com
- Password: admin123

## Main flow

Student:
Login → Report Problem → Upload Photo → Submit → Complaint ID → Track Status → Notifications

Admin:
Login → Dashboard → Manage Complaints → Assign Department → Update Status → Resolution Notes → Resolve

## Storage
- H2 file database: `./data/campusfixdb`
- Uploaded photos: `./uploads`

For production, replace the simple session login with Spring Security, hash passwords with BCrypt, and use MySQL/PostgreSQL plus cloud/object storage for images.
