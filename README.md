# GymFlow API

GymFlow is a production-ready fitness class booking platform.  
This repository contains the backend service: a Spring Boot 3 application providing authentication, class scheduling, booking, payments, and admin capabilities.

The Web service is available at: https://github.com/vslbak/gymflow-web \
Live demo: https://gymflow.codebshift.dev/

**Disclaimer**: this is a demo project for learning purposes only.\
No real payments or bookings are processed.\
Signup at the demo website is disabled to prevent abuse. A Non-admin pre-seeded user can be used to book classes. \
Stripe test card number: `4242 4242 4242 4242` with any future date and any CVC.

## Tech Stack
- Java 21
- Spring Boot 3
- Spring Security (JWT)
- PostgreSQL
- Liquibase
- Stripe Checkout
- Caddy
- Docker & Docker Compose

## Features
- User registration & login
- JWT access/refresh tokens
- Class listing & session generation
- Booking flow with seat limits
- Stripe Checkout redirection + webhook confirmation
- Admin panel for class/session/booking management
- Liquibase migrations with initial demo data

## Local Development

### Environment variables (.env)
```
POSTGRES_HOST=postgres
POSTGRES_DB=gymflow
POSTGRES_USER=gymflow
POSTGRES_PASSWORD=gymflow

STRIPE_SECRET_KEY=sk_test_xxx
STRIPE_WEBHOOK_SECRET=whsec_xxx

JWT_SECRET=change-this
FRONTEND_BASE=http://localhost:5173
```

### Start backend + DB
Spin up the DB
```
docker compose up
```
Then run the spring boot app
```
./mvnw spring-boot:run
```

## Deployment
The deployment config can be found under `./deploy`. \
Currently uses Caddy as a reverse proxy with automatic HTTPS via Let's Encrypt. \
The demo website is hosted on DigitalOcean using the config under `./deploy` and a dedicated, non-checked in `.env` file.