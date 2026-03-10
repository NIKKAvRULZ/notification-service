# Notification Service ✉️

Part of the **Food Ordering System** Microservices Project for SLIIT Cloud Computing.

## 🚀 Overview
The Notification Service is a Spring Boot-based microservice responsible for sending email notifications across the Gourmet Express platform. It coordinates asynchronous alerts for user registrations, order status updates, and payment summaries.

## 🌐 Live Deployment
* **Base URL:** `https://notification-service-production-e192.up.railway.app/api/v1/notify`
* **API Documentation (Swagger):** [Explore Endpoints](https://notification-service-production-e192.up.railway.app/swagger-ui.html)

---

## 🛠️ Tech Stack
* **Framework:** Spring Boot 3
* **Email Client:** JavaMailSender / SendGrid
* **Containerization:** Docker
* **API Documentation:** OpenAPI / Swagger
* **Cloud Hosting:** Railway

## 📦 API Endpoints
Detailed interactive documentation for the REST APIs can be found on our live Swagger UIs:

**Base URL:** `https://notification-service-production-e192.up.railway.app/api/v1/notify`
**Swagger UI:** [https://notification-service-production-e192.up.railway.app/swagger-ui.html](https://notification-service-production-e192.up.railway.app/swagger-ui.html)  

- `POST /`: Process payment notification and send integration email
- `GET /welcome/{userId}`: Send a welcome email to a newly registered user
- `GET /order-pending/{userId}/{orderId}`: Send an email reminder for an order with a pending payment status
- `GET /ping`: Health check endpoint to keep the service awake

## 🧪 Running Locally
1. Clone the repository: `git clone <repository-url>/notification-service.git`
2. Configure `.env` variables or `application.yaml` with your `SENDGRID_API_KEY` and mail properties.
3. Run Maven build: `mvn clean package`
4. Run the application: `java -jar target/*.jar`
5. Access Swagger at: `http://localhost:8086/swagger-ui.html`
