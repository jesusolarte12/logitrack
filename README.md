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

## Despliegue en Tomcat (Servidor de Producción)

### Requisitos del Servidor

La máquina destino debe tener instalado:
- **Java JDK 17** o superior
- **Apache Tomcat 10** o superior
- **MySQL 8.0** o superior
- Acceso con permisos de administrador/sudo

---

### Despliegue de la Aplicación

#### Paso 1: Configurar la Base de Datos en el Servidor

```bash
# Conectar a MySQL
mysql -u root -p

# Ejecutar scripts de base de datos
mysql -u root -p < /ruta/a/schema.sql
mysql -u root -p < /ruta/a/data.sql
```

#### Paso 3: Compilar el Proyecto

```bash
# En tu máquina de desarrollo
cd /ruta/logitrack
mvn clean package -DskipTests

# El WAR se genera en: target/logitrack-0.0.1-SNAPSHOT.war
```

#### Paso 4: Transferir el WAR al Servidor

**Con SCP (SSH)**
```bash
scp target/logitrack-0.0.1-SNAPSHOT.war usuario@servidor:/tmp/logitrack.war
```

#### Paso 5: Desplegar en Tomcat

**Si Tomcat está instalado con gestor de paquetes:**
```bash
# Ubuntu/Debian/Arch
sudo cp /tmp/logitrack.war /var/lib/tomcat10/webapps/logitrack.war

# Reiniciar Tomcat
sudo systemctl restart tomcat10

# Verificar estado
sudo systemctl status tomcat10

```

**Si Tomcat está en /opt/tomcat10:**
```bash
sudo cp /tmp/logitrack.war /opt/tomcat10/webapps/logitrack.war

# Reiniciar Tomcat
sudo /opt/tomcat10/bin/shutdown.sh
sudo /opt/tomcat10/bin/startup.sh

```

#### Paso 6: Verificar el Despliegue

```bash
# Esperar 15-30 segundos para que despliegue

# Verificar que la carpeta se desempaquetó
ls -la /var/lib/tomcat10/webapps/ | grep logitrack
# Deberías ver: logitrack/ y logitrack.war

# Probar endpoint
curl http://localhost:8080/logitrack/auth/login
# Debería responder (aunque sea error sin credenciales)
```

---

### 🌐 Acceso a la Aplicación

Una vez desplegado, accede desde cualquier navegador:

**En el servidor local:**
```
http://localhost:8080/logitrack/templates/login.html
http://localhost:8080/logitrack/docs 
```

---

### 🔄 Actualizar la Aplicación

Para desplegar una nueva versión:

```bash
# 1. Detener Tomcat
sudo systemctl stop tomcat10

# 2. Eliminar versión anterior
sudo rm -rf /var/lib/tomcat10/webapps/logitrack*

# 3. Copiar nuevo WAR
sudo cp /ruta/nuevo/logitrack-0.0.1-SNAPSHOT.war /var/lib/tomcat10/webapps/logitrack.war

# 4. Iniciar Tomcat
sudo systemctl start tomcat10

# 5. Verificar logs
sudo tail -f /var/log/tomcat10/catalina.out
```

---

**Problema: Error de conexión a MySQL**
```bash
# Verificar que MySQL esté corriendo
sudo systemctl status mysql

# Probar conexión
mysql -u logitrack_user -p -h localhost LogiTrack
```

**Problema: 404 Not Found**
```bash
# Verificar que la app se desempaquetó
ls -la /var/lib/tomcat10/webapps/logitrack/

# Verificar URL correcta
# Debe ser: http://IP:8080/logitrack/templates/login.html
# NO: http://IP:8080/templates/login.html
```

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