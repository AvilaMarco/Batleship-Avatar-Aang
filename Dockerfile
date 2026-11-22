# syntax=docker/dockerfile:1
FROM node:22-slim AS front
WORKDIR /fe
COPY frontend/package.json frontend/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
COPY --from=front /fe/dist src/main/resources/static
RUN --mount=type=cache,target=/root/.m2 sh mvnw -B -DskipTests package \
 && cp target/shipbender-*.jar /app.jar

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
COPY --from=build /app.jar /app/app.jar
USER app
# si falta el perfil, Spring usa dev (seeder + secreto conocido): prod fijo en la imagen
ENV SPRING_PROFILES_ACTIVE=prod PORT=8080
EXPOSE 8080
# JVM acotada para el servidor (768m): el compose puede sobrescribirlo con JAVA_TOOL_OPTIONS
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
