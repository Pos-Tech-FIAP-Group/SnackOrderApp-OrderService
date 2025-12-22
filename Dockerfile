# Etapa 1 - Build do JAR com Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw ./
RUN chmod +x mvnw

COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa 2 - Rodar app com imagem leve
FROM eclipse-temurin:21-jre-alpine

RUN addgroup --system spring && \
    adduser --system --ingroup spring spring

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN chown -R spring:spring /app

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]