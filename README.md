# cliente-service

REST API de clientes creada con Spring Boot 3, Java 21, Maven y PostgreSQL.

## Configuración

1. Copia `.env.example` a `.env`.

La aplicación y Docker Compose leen la misma configuración. `.env` está excluido de Git.

## Usar PostgreSQL con Docker

Docker Desktop debe estar iniciado. El contenedor publica PostgreSQL en el puerto `5433` del equipo, evitando conflictos con instalaciones locales que normalmente usan `5432`.

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

## Usar una instalación local de PostgreSQL

En `.env`, cambia `DB_PORT` por el puerto de la instancia (por ejemplo, `5432`) y configura `DB_NAME`, `DB_USER` y `DB_PASSWORD` con una base, usuario y contraseña que ya existan en esa instancia. La aplicación no crea el usuario ni la base de datos de PostgreSQL.

Después ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

## Endpoints

La API escucha en el puerto 8080 y Actuator expone `/actuator/health` en el puerto 8081.

## Pruebas

Ejecuta la suite automatizada (sin Docker ni una base de datos externa):

```powershell
.\mvnw.cmd test
```

Las pruebas unitarias y los slices de Spring usan mocks o H2; la prueba E2E también
usa H2 en modo PostgreSQL para que se pueda ejecutar en entornos sin Docker. JaCoCo
genera el reporte en `target/site/jacoco/index.html`.
