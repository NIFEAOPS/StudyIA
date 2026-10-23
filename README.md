# Study IA Backend

Backend MVP en Spring Boot para generar roadmaps de aprendizaje personalizados.

## Requisitos

- Java 17+
- Maven 3.9+
- `OPENAI_API_KEY` para usar la integración real con OpenAI

## Ejecutar

```bash
mvn spring-boot:run
```

Sin API key, el backend usa una respuesta local de desarrollo para permitir probar el flujo completo.

## Despliegue en una VM con Docker

La configuración de producción incluye el backend Spring Boot y el frontend React
en Docker Compose. Caddy sirve el frontend, redirige `/api/*` al backend y
gestiona HTTPS para `studyia.app`.

En la VM, crea un archivo `.env` junto a `docker-compose.yml` (no lo subas a Git)
con las credenciales de Cloud SQL y los secretos de producción:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://34.23.217.174:5432/studyapp?sslmode=require&connectTimeout=10
SPRING_DATASOURCE_USERNAME=nicolas
SPRING_DATASOURCE_PASSWORD=<contraseña-de-Cloud-SQL>
JWT_SECRET=<secreto-base64-de-al-menos-256-bits>
STUDYIA_MAIL_ENABLED=true
MAIL_HOST=<servidor-smtp>
MAIL_PORT=587
MAIL_USERNAME=<usuario-smtp>
MAIL_PASSWORD=<contraseña-smtp>
OPENAI_API_KEY=<clave-opcional>
```

Apunta el registro DNS `A` de `studyia.app` a la IP pública estática de la VM,
abre los puertos 80 y 443 y ejecuta:

```bash
docker compose up -d --build
docker compose logs -f
```

Caddy emitirá y renovará el certificado HTTPS automáticamente. Para actualizar:

```bash
git pull origin main
docker compose up -d --build
```

## Base de datos Cloud SQL

La configuración de producción usa PostgreSQL en la instancia de Cloud SQL
`project-466ce984-13f9-4b4c-94c:us-east1:study-ia`, mediante su IP pública y
TLS. El nombre de la base de datos configurado para esta instancia es
`studyapp`. La URL, el usuario y la contraseña pueden sobrescribirse con
variables de entorno; la contraseña es obligatoria y no debe guardarse en el
repositorio.

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://34.23.217.174:5432/studyapp?sslmode=require'
export SPRING_DATASOURCE_USERNAME='nicolas'
export SPRING_DATASOURCE_PASSWORD='<contraseña-de-Cloud-SQL>'
export SPRING_JPA_HIBERNATE_DDL_AUTO=update
mvn spring-boot:run
```

El backend crea o actualiza las tablas JPA (`users`, `students`, `assessments`,
`roadmaps`, `roadmap_stages`, `roadmap_activities`, `learning_sessions` y
`email_verifications`) al iniciar cuando
`SPRING_JPA_HIBERNATE_DDL_AUTO=update`. Para producción se recomienda migrar a
un esquema versionado y usar `validate` después de aplicar las migraciones.

Las pruebas de contexto (`@SpringBootTest`) también usan PostgreSQL mediante
las mismas variables de entorno; no se utiliza H2 ni una base embebida. Antes
de ejecutar Maven, verifica que la instancia permita la IP de origen:

```bash
mvn test
```

Este proyecto es Spring Boot/JPA, no Node.js/Prisma. JPA es el ORM que usa
realmente el backend y `ddl-auto=update` crea automáticamente las tablas,
incluidas `student_interests` y `student_current_knowledge`. Agregar Prisma
crearía un segundo ORM y una segunda fuente de verdad que no participaría en
el proceso de la aplicación.

## Autenticacion

El backend usa access tokens JWT stateless de 15 minutos y refresh tokens de 7 dias en una cookie `HttpOnly`.
En produccion define una clave Base64 de al menos 256 bits y activa la cookie segura:

```bash
export JWT_SECRET="<base64-secret-de-256-bits>"
export JWT_REFRESH_COOKIE_SECURE=true
```

Endpoints disponibles: `POST /api/v1/auth/register`, `POST /api/v1/auth/login` y
`POST /api/v1/auth/refresh`. Las rutas distintas de autenticacion requieren
`Authorization: Bearer <access-token>`.

## Endpoint principal

`POST /api/v1/roadmaps`

```json
{
  "name": "Ana",
  "email": "ana@example.com",
  "interests": ["backend", "cloud"],
  "currentKnowledge": ["Java", "SQL"],
  "weeklyAvailabilityHours": 8,
  "professionalGoals": "Conseguir un puesto junior backend"
}
```
