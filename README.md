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

## SonarQube Analysis

The Maven build includes the SonarQube scanner plugin. Provide the SonarQube URL and token from your shell or CI secret store:

```bash
export SONAR_HOST_URL='http://sonarqube.example.com:9000'
export SONAR_TOKEN='replace-with-sonarqube-token'
mvn clean verify sonar:sonar \
  -Dsonar.host.url="$SONAR_HOST_URL" \
  -Dsonar.token="$SONAR_TOKEN"
```

The project key is configured as:

```text
liontech-resorts
```

## Publish WAR to Nexus

The POM is configured with Maven `distributionManagement` repository IDs:

```text
nexus-releases
nexus-snapshots
```

Copy `maven-settings.example.xml` to your Maven settings location or pass it with `-s`, then provide Nexus credentials and repository URLs from environment variables:

```bash
export NEXUS_USERNAME='deployment-user'
export NEXUS_PASSWORD='replace-with-nexus-password'
export NEXUS_RELEASES_URL='http://nexus.example.com:8081/repository/maven-releases/'
export NEXUS_SNAPSHOTS_URL='http://nexus.example.com:8081/repository/maven-snapshots/'

mvn -s maven-settings.example.xml clean deploy
```

For a Nexus staging repository, also provide the base Nexus URL and enable the profile:

```bash
export NEXUS_URL='http://nexus.example.com:8081/'
mvn -s maven-settings.example.xml -Pnexus-staging clean deploy
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
