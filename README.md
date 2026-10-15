# cliente-service

REST API de clientes creada con Spring Boot 3, Java 21, Maven y PostgreSQL.

## Ejecución local

1. Copia `.env.example` a `.env` y define un valor local para `DB_PASSWORD`.
2. Inicia PostgreSQL con `docker compose up -d`.
3. Ejecuta la aplicación con `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).

La API escucha en el puerto 8080 y Actuator expone `/actuator/health` en el puerto 8081.
