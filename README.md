# Corenz Service

Servicio de backend desarrollado con Spring Boot 3.3.5 y Java 21, estructurado bajo el patrón de Arquitectura Hexagonal (Ports & Adapters). Provee endpoints para autenticación, gestión de perfiles, refresco de sesión y restablecimiento de credenciales mediante integración con PostgreSQL, Redis y servidores SMTP.

## Entorno de Producción

El servicio se encuentra desplegado en Render y consumido directamente por el cliente web:
* Podrá encontrar la demo en el repositorio del frontend: https://github.com/renz-ayala/HexaAuth-frontend

## Stack Tecnológico

* Lenguaje: Java 21 (LTS)
* Framework Principal: Spring Boot 3.3.x
* Seguridad & Tokens: Spring Security / JJWT (Java JWT)
* Persistencia Relacional: PostgreSQL / Spring Data JPA / JdbcTemplate (Stored Procedures)
* Caché & In-Memory Store: Redis (Upstash) / Spring Data Redis
* Construcción & Automatización: Gradle
* Emailing & Servidor SMTP: Brevo SMTP / Spring Mail
* Infraestructura & Hosting: Render (Web Services)

## Arquitectura y Componentes Clave

* Arquitectura Hexagonal: Desacoplamiento total de las reglas de dominio respecto a frameworks o librerías de infraestructura. La capa de aplicación define los casos de uso (usecase) y las interfaces de entrada/salida (ports), mientras que los adaptadores gestionan la persistencia y servicios externos.
* Persistencia Híbrida:
  - PostgreSQL: Consultas de lectura mediante Spring Data JPA y llamadas a procedimientos almacenados (sp_create_user, sp_validate_password, sp_change_password) mediante JdbcTemplate para operaciones críticas.
  - Redis (Upstash): Gestión de tokens temporales de confirmación, recuperación de contraseña y revocación (Blacklist) de Access Tokens.
* Seguridad y Red:
  - Generación y validación de Access Tokens (JWT) y Refresh Tokens.
  - Integración con servicio SMTP para el envío de correos transaccionales.

## Ejecución Local

### Requisitos
* Java 21 OpenJDK
* Instancia de PostgreSQL
* Instancia de Redis

### Pasos de Configuración

1. Clonar el repositorio:
git clone https://github.com/renz-ayala/HexaAuth-backend

2. Configurar variables de entorno y ajustar src/main/resources/application.yml:
(template)
spring.datasource.url=jdbc:postgresql://localhost:5432/userapps_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.data.redis.host=localhost
spring.data.redis.port=6379

cors.origin.allowed=http://localhost:4200

3. Compilar y ejecutar la aplicación:
./gradlew bootRun
