# Vitalid Backend API

Backend de la plataforma de telemedicina Vitalid construida con Spring Boot 3.1.5 y Java 17.

## 📋 Requisitos Previos

- Java 17 o superior
- Maven 3.8+
- PostgreSQL 15+
- Git
- Docker y Docker Compose (opcional)

## 🚀 Configuración Inicial

### 1. Clonar el repositorio
```bash
git clone https://github.com/Zelxel-zx/Vitalid-app---Backend.git
cd vitalid-backend
```

### 2. Crear la base de datos (PostgreSQL)
```bash
psql -U postgres
```
```sql
CREATE DATABASE vitalid_db;
CREATE USER vitalid_user WITH PASSWORD 'vitalid_password';
ALTER ROLE vitalid_user SET client_encoding TO 'utf8';
ALTER ROLE vitalid_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE vitalid_user SET default_transaction_deferrable TO on;
GRANT ALL PRIVILEGES ON DATABASE vitalid_db TO vitalid_user;
```

O con Docker Compose (recomendado):
```bash
docker-compose up -d
```
Esto iniciará PostgreSQL 15 automáticamente con la configuración necesaria.

### 3. Configurar variables de entorno (opcional)
```bash
cp .env.example .env
# Editar .env con tus valores
```

### 4. Instalar dependencias
```bash
mvn clean install
```

## 🏃 Ejecutar la aplicación

### Desarrollo
```bash
mvn spring-boot:run
```

La API estará disponible en: `http://localhost:8080`

### Producción
```bash
mvn clean package
java -jar target/vitalid-backend-1.0.0.jar --spring.profiles.active=prod
```

## 📚 Estructura del Proyecto

```
vitalid-backend/
├── src/
│   ├── main/
│   │   ├── java/com/vitalid/
│   │   │   ├── auth/           # Autenticación y autorización
│   │   │   ├── doctor/         # Gestión de doctores
│   │   │   ├── chat/           # Mensajería
│   │   │   ├── treatment/      # Tratamientos
│   │   │   ├── medication/     # Medicamentos
│   │   │   ├── checklist/      # Checklists de medicamentos
│   │   │   ├── appointment/    # Citas médicas
│   │   │   ├── health/         # Métricas de salud
│   │   │   ├── profile/        # Perfiles de usuario
│   │   │   ├── config/         # Configuración global
│   │   │   ├── exception/      # Manejo de errores
│   │   │   ├── security/       # Seguridad JWT
│   │   │   └── util/           # Utilidades
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/   # Flyway migrations
│   └── test/
└── pom.xml
```

## 🔧 Dependencias Principales

- **Spring Boot Web**: Framework REST
- **Spring Security + JWT**: Autenticación y autorización
- **Spring Data JPA**: Persistencia de datos
- **Hibernate**: ORM
- **PostgreSQL Driver**: Driver para PostgreSQL
- **Lombok**: Reducción de boilerplate
- **MapStruct**: Mapeo DTO ↔ Entity
- **Flyway**: Migraciones de base de datos
- **Docker Compose**: Orquestación de contenedores

## 📖 Documentación de APIs

### Base URL
```
http://localhost:8080/api
```

### Módulos Disponibles

- **Auth** (`/auth`) - Autenticación y registro
- **Doctors** (`/doctors`) - Gestión de doctores
- **Messages** (`/messages`) - Mensajería
- **Treatments** (`/treatments`) - Tratamientos
- **Medications** (`/medications`) - Medicamentos
- **Checklists** (`/checklists`) - Checklists
- **Appointments** (`/appointments`) - Citas
- **Health-Metrics** (`/health-metrics`) - Métricas de salud
- **Profile** (`/profile`) - Perfil de usuario

## 🔐 Autenticación

Todos los endpoints (excepto `/auth/register` y `/auth/login`) requieren un token JWT en el header:

```
Authorization: Bearer {token}
```

## 🗄️ Base de Datos

Las migraciones se ejecutan automáticamente al iniciar la aplicación usando Flyway.

### Migraciones
- `V1__Initial_Schema.sql` - Tablas principales
- `V2__Add_Tables.sql` - Tablas adicionales
- `V3__Add_Indexes.sql` - Índices y optimizaciones

## 🧪 Tests

Ejecutar tests:
```bash
mvn test
```

## 📝 Logging

El nivel de logging se configura en `application.properties`:

```properties
logging.level.root=INFO
logging.level.com.vitalid=DEBUG
```

## 🐳 Docker

### Build
```bash
docker build -t vitalid-backend .
```

### Run
```bash
docker run -p 8080:8080 --name vitalid-backend vitalid-backend
```

## 🤝 Contribuciones

1. Fork el repositorio
2. Crea una rama (`git checkout -b feature/AmazingFeature`)
3. Commit los cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver `LICENSE` para más detalles.

## 👥 Equipo de Desarrollo

Vitalid Backend Team

## 📞 Soporte

Para reportar bugs o solicitar features, abre un issue en el repositorio.

---

**Versión**: 1.0.0  
**Fecha de Actualización**: 03 de Mayo de 2026
