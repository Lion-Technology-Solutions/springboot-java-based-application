# LionTech Resorts

LionTech Resorts is a Spring Boot hotel management web application built with Maven and packaged as a `.war` for Apache Tomcat.

## Features

- Account creation and secure login
- Public room catalog with premium room photography
- Global booking workflow with guest country, language, currency, and special requests
- Checkout flow with a fake payment processor
- Booking confirmation and masked card storage
- Admin room management and full room catalog seeding
- Facilities and amenities management through seeded data
- Language service with English, French, Spanish, and Haitian Creole support
- H2 fake database service for realistic persistence during demos
- Health endpoint for deployment monitoring

## Technology

- Java 17
- Spring Boot 3.4
- Maven
- Thymeleaf
- Spring Security
- Spring Data JPA
- H2 database
- WAR deployment to Tomcat

## Build

```bash
mvn clean package
```

The deployable WAR is created at:

```text
target/liontech-resorts.war
```

## Deploy to Tomcat

Copy the WAR into Tomcat's `webapps` directory:

```bash
cp target/liontech-resorts.war $CATALINA_HOME/webapps/
```

Then start Tomcat and open:

```text
http://localhost:8080/liontech-resorts/
```

## Local Run

The app is still executable for local development:

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080/
```

## Demo Admin

Default seeded admin:

```text
Email: admin@liontechresorts.com
Password: Admin@12345!
```

For production, override the default password:

```bash
export LIONTECH_ADMIN_PASSWORD='replace-with-a-strong-password'
```

## Fake Database

By default the application uses an H2 file database:

```text
./data/liontech-resorts.mv.db
```

Override it with environment variables:

```bash
export LIONTECH_DB_URL='jdbc:h2:file:/opt/liontech/data/liontech-resorts'
export LIONTECH_DB_USERNAME='sa'
export LIONTECH_DB_PASSWORD='change-me'
```

The application seeds realistic rooms, amenities, facilities, and an admin account on first startup.
