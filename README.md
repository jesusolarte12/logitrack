# 📦 LogiTrack - Sistema de Gestión y Auditoría de Bodegas

## 📋 Descripción

Sistema backend en Spring Boot para gestión de bodegas, inventario, movimientos y auditoría con autenticación JWT.

**Características:** CRUD de bodegas y productos | Movimientos (entrada/salida/transferencia) | Auditoría automática | Seguridad JWT | API REST documentada con Swagger

---

## 🛠️ Tecnologías

- Spring Boot 3.5.7 | Spring Security + JWT | Spring Data JPA | MySQL 8.0 | Lombok | Maven | HTML/CSS/JS

---

## 📥 Instalación y Configuración

### Prerrequisitos
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

### 1. Clonar Repositorio
```bash
git clone https://github.com/jesusolarte12/logitrack.git
cd logitrack
```

### 2. Configurar Base de Datos
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/data.sql
```

### 3. Configurar Credenciales MySQL

Edita `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/LogiTrack
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA
```

### 4. Compilar y Ejecutar
```bash
mvn clean install
mvn spring-boot:run
```

**URLs Disponibles:**
- API: http://localhost:8080
- Swagger: http://localhost:8080/docs
- Frontend: http://localhost:8080/templates/login.html

---

## 🚀 Despliegue en Tomcat Externo

### 1. Generar WAR
```bash
mvn clean package
```

### 2. Copiar WAR a Tomcat
```bash
cp target/logitrack-0.0.1-SNAPSHOT.war /ruta/tomcat/webapps/logitrack.war
```

### 3. Iniciar Tomcat
```bash
cd /ruta/tomcat/bin
./startup.sh  # Linux/Mac
startup.bat   # Windows
```

### 4. Acceder a la aplicación
```
http://localhost:8080/logitrack/
http://localhost:8080/logitrack/docs
```

> **Nota:** Asegúrate de que el `application.properties` tenga las credenciales correctas de MySQL antes de generar el WAR.

---

## 🔑 Credenciales por Defecto

### Base de Datos MySQL
```
Host: localhost
Puerto: 3306
Base de datos: LogiTrack
Usuario: root
Contraseña: [Configurar en application.properties]
```

### Usuarios de Aplicación (según data.sql)
```
Admin:
  Username: admin
  Password: admin123
  Rol: ADMIN

Empleado:
  Username: ana
  Password: ana123
  Rol: EMPLEADO
```

---

## 📚 Endpoints Principales

### Autenticación
```http
POST /auth/login          # Iniciar sesión (obtener JWT)
POST /auth/register       # Registrar usuario
GET  /auth/userinfo       # Info usuario actual
```

### Módulos (requieren JWT)
```http
# Bodegas
GET    /bodegas           # Listar
POST   /bodegas           # Crear
PUT    /bodegas/{id}      # Actualizar
DELETE /bodegas/{id}      # Eliminar

# Productos
GET    /productos         # Listar
POST   /productos         # Crear
GET    /productos/stock-bajo  # Stock < 10

# Inventario
GET    /inventario/bodega/{bodegaId}  # Ver stock
PUT    /inventario/{id}               # Actualizar

# Movimientos
POST   /movimientos/entrada           # Registrar entrada
POST   /movimientos/salida            # Registrar salida
POST   /movimientos/transferencia     # Transferir entre bodegas
GET    /movimientos/filtrar?fechaInicio=...&fechaFin=...

# Auditoría
GET    /auditorias                    # Listar todas
GET    /auditorias/usuario/{id}       # Por usuario
GET    /auditorias/tipo/{tipo}        # Por tipo (INSERT/UPDATE/DELETE)

# Dashboard
GET    /dashboard/stats               # Estadísticas generales
```


### Probar en Swagger
1. Ir a http://localhost:8080/docs
2. Click en "Authorize" 🔓
3. Ingresar: `Bearer {token}`
4. Probar endpoints

---

## 📁 Estructura del Proyecto
```
src/main/java/com/proyecto/logitrack/
├── controller/      # Endpoints REST
├── service/         # Lógica de negocio
├── repository/      # JPA Repositories
├── entities/        # Entidades (Usuario, Bodega, Producto, etc.)
├── security/        # JWT + Spring Security
├── dto/             # Data Transfer Objects
└── exception/       # Manejo de errores

database/
├── schema.sql       # Estructura de BD
└── data.sql         # Datos iniciales
```

---

**Puerto 8080 ocupado:**
```properties
# application.properties
server.port=8081
```

---

## 👥 Colaboradores

- **Jesús Olarte** - [@jesusolarte12](https://github.com/jesusolarte12) 
- **Jolgan Pardo** - [@jolganpardo](https://github.com/jolganpardo)

---