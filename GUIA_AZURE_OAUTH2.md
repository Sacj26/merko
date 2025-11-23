# 🚀 GUÍA DE CONFIGURACIÓN MERKO
## OAuth2 Google + Azure MySQL Database

---

## 📋 PARTE 1: CONFIGURAR AZURE MYSQL

### Paso 1: Obtener Credenciales de Azure MySQL

1. Ve al [Portal de Azure](https://portal.azure.com/)
2. Busca tu servidor MySQL
3. En el panel izquierdo, ve a **"Configuración" > "Cadenas de conexión"**
4. Copia la información:
   - **Servidor**: `merko-def.mysql.database.azure.com`
   - **Puerto**: `3306`
   - **Usuario**: `admin_@merko-def`
   - **Base de datos**: `merko`

### Paso 2: Configurar Firewall de Azure

1. En tu servidor MySQL de Azure, ve a **"Seguridad" > "Reglas de firewall"**
2. Agrega tu IP actual:
   - Nombre: `Mi PC`
   - IP Inicial: Tu IP pública (búscala en [whatismyip.com](https://www.whatismyip.com/))
   - IP Final: La misma
3. **Importante**: Para desarrollo, puedes habilitar temporalmente:
   - ☑️ "Permitir el acceso a servicios de Azure"
4. Guarda los cambios

### Paso 3: Actualizar application.properties

Abre `src/main/resources/application.properties` y reemplaza:

```properties
# ============================================
# AZURE MYSQL DATABASE CONFIGURATION
# ============================================
spring.datasource.url=jdbc:mysql://merko-def.mysql.database.azure.com:3306/merko?useSSL=true&requireSSL=true&serverTimezone=UTC
spring.datasource.username=admin_@merko-def
spring.datasource.password=26092003.Sebas
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# SSL Configuration para Azure (requerido)
spring.datasource.hikari.connection-test-query=SELECT 1
```

**Ejemplo real:**
```properties
spring.datasource.url=jdbc:mysql://merko-server.mysql.database.azure.com:3306/merko?useSSL=true&requireSSL=true&serverTimezone=UTC
spring.datasource.username=admin@merko-server
spring.datasource.password=MiPassword123!
```

### Paso 4: Probar Conexión

Ejecuta tu aplicación:
```bash
mvnw spring-boot:run
```

Si ves este mensaje, la conexión está OK:
```
HikariPool-1 - Start completed.
```

Si ves error de SSL, agrega esto al URL:
```properties
spring.datasource.url=jdbc:mysql://...?useSSL=true&requireSSL=false&serverTimezone=UTC
```

---

## 🔐 PARTE 2: CONFIGURAR GOOGLE OAUTH2

### Paso 1: Crear Proyecto en Google Cloud

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Click en el menú de proyectos (arriba)
3. Click en **"Nuevo proyecto"**
4. Nombre: `MERKO`
5. Click en **"Crear"**

### Paso 2: Habilitar Google+ API

1. En el menú lateral, ve a **"APIs y servicios" > "Biblioteca"**
2. Busca: `Google+ API`
3. Click en **"Habilitar"**

### Paso 3: Crear Credenciales OAuth 2.0

1. Ve a **"APIs y servicios" > "Credenciales"**
2. Click en **"+ CREAR CREDENCIALES"** > **"ID de cliente de OAuth 2.0"**
3. Si pide configurar pantalla de consentimiento:
   - Click en **"CONFIGURAR PANTALLA DE CONSENTIMIENTO"**
   - Tipo de usuario: **Externo**
   - Nombre de la aplicación: `MERKO`
   - Correo de asistencia: tu correo
   - Dominios autorizados: (dejar vacío por ahora)
   - Información del desarrollador: tu correo
   - Guardar y continuar
   - Ámbitos: Agregar `userinfo.email` y `userinfo.profile`
   - Guardar y continuar
   - Usuarios de prueba: Agregar tu correo de Gmail
   - Guardar

4. Volver a **"Credenciales"** > **"+ CREAR CREDENCIALES"** > **"ID de cliente de OAuth 2.0"**
5. Tipo de aplicación: **Aplicación web**
6. Nombre: `MERKO Web`
7. **URI de redireccionamiento autorizados**:
   - `http://localhost:8080/login/oauth2/code/google`
   - `http://localhost:8080/oauth2/code/google`
8. Click en **"CREAR"**
9. **Copiar el ID de cliente y Clave secreta del cliente** ⚠️

'560225093878-sp6pvvr94n6ij8hnvfrvdkh436s1vbbu.apps.googleusercontent.com'

### Paso 4: Configurar en application.properties

Agrega al final de `application.properties`:

```properties
# ============================================
# GOOGLE OAUTH2 CONFIGURATION
# ============================================
spring.security.oauth2.client.registration.google.client-id=TU_CLIENT_ID_AQUI
spring.security.oauth2.client.registration.google.client-secret=TU_CLIENT_SECRET_AQUI
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
```

**Ejemplo real:**
```properties
spring.security.oauth2.client.registration.google.client-id=123456789-abcdefg.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=GOCSPX-abcd1234efgh5678
```

---

## 📊 PARTE 3: EJECUTAR MIGRACIONES SQL

### Paso 1: Ejecutar Script OAuth2

Conecta a tu base de datos Azure con MySQL Workbench:
- Host: `tu-servidor.mysql.database.azure.com`
- Port: `3306`
- Username: `tu-usuario@tu-servidor`
- Password: tu password
- Schema: `merko`
- SSL: **Use SSL** ✓

Ejecuta el script:
```sql
-- scripts/V6__add_oauth2_support.sql
ALTER TABLE usuario 
ADD COLUMN google_id VARCHAR(255) UNIQUE AFTER ultimo_login,
ADD COLUMN profile_picture VARCHAR(500) AFTER google_id,
ADD COLUMN oauth2_user BOOLEAN DEFAULT FALSE AFTER profile_picture;

CREATE INDEX idx_usuario_google_id ON usuario(google_id);
ALTER TABLE usuario MODIFY COLUMN password VARCHAR(255) NULL;
```

### Paso 2: Verificar Cambios

```sql
DESC usuario;

-- Deberías ver las nuevas columnas:
-- google_id
-- profile_picture
-- oauth2_user
```

---

## 🎯 PARTE 4: PROBAR LA APLICACIÓN

### Paso 1: Compilar Proyecto

```bash
mvnw clean install
```

### Paso 2: Ejecutar Aplicación

```bash
mvnw spring-boot:run
```

### Paso 3: Probar Login con Google

1. Abre: `http://localhost:8080/login`
2. Deberías ver el botón **"Continuar con Google"**
3. Click en el botón
4. Selecciona tu cuenta de Google
5. Acepta los permisos
6. Deberías ser redirigido a `/publico/productos`

### Paso 4: Verificar Usuario Creado

```sql
SELECT 
    id,
    username,
    correo,
    nombre,
    google_id,
    profile_picture,
    oauth2_user,
    rol,
    activo
FROM usuario
WHERE oauth2_user = TRUE;
```

---

## ⚠️ SOLUCIÓN DE PROBLEMAS COMUNES

### Error: "Communications link failure"

**Causa**: No se puede conectar a Azure MySQL

**Solución**:
1. Verifica tu IP en las reglas de firewall de Azure
2. Asegúrate de habilitar "Permitir acceso a servicios de Azure"
3. Verifica usuario y password

### Error: "SSL connection is required"

**Causa**: Azure MySQL requiere SSL

**Solución**: Asegúrate de tener `useSSL=true&requireSSL=true` en el URL

Si persiste, intenta:
```properties
spring.datasource.url=jdbc:mysql://...?useSSL=true&requireSSL=false
```

### Error: "redirect_uri_mismatch"

**Causa**: El redirect URI no coincide

**Solución**:
1. Ve a Google Cloud Console > Credenciales
2. Edita tu OAuth client ID
3. Asegúrate de tener exactamente:
   - `http://localhost:8080/login/oauth2/code/google`
4. Guarda

### Error: "User not found" al hacer login con Google

**Causa**: El servicio OAuth2 no está registrando al usuario

**Solución**:
1. Verifica que ejecutaste el script SQL de migración
2. Verifica que `CustomOAuth2UserService` está siendo usado
3. Revisa los logs de la aplicación

### Error: "Password cannot be null"

**Causa**: Intentas crear usuario OAuth2 pero password es requerido

**Solución**: Ejecuta esto en tu BD Azure:
```sql
ALTER TABLE usuario MODIFY COLUMN password VARCHAR(255) NULL;
```

---

## 🔒 SEGURIDAD - PRODUCCIÓN

### 1. Variables de Entorno

**NO** subas credenciales a Git. Usa variables de entorno:

**application.properties**:
```properties
spring.datasource.url=${AZURE_MYSQL_URL}
spring.datasource.username=${AZURE_MYSQL_USERNAME}
spring.datasource.password=${AZURE_MYSQL_PASSWORD}

spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
```

**En tu sistema (PowerShell)**:
```powershell
$env:AZURE_MYSQL_URL="jdbc:mysql://..."
$env:AZURE_MYSQL_USERNAME="admin@servidor"
$env:AZURE_MYSQL_PASSWORD="tu-password"
$env:GOOGLE_CLIENT_ID="tu-client-id"
$env:GOOGLE_CLIENT_SECRET="tu-client-secret"
```

### 2. Actualizar Redirect URI para Producción

Cuando despliegues a producción:
1. Agrega el dominio de producción a Google Cloud Console
2. Ejemplo: `https://merko.com/login/oauth2/code/google`

---

## 📝 CHECKLIST FINAL

- [ ] Azure MySQL configurado y firewall abierto
- [ ] Credenciales de Azure en application.properties
- [ ] Conexión exitosa a Azure (logs sin error)
- [ ] Proyecto creado en Google Cloud Console
- [ ] OAuth2 Client ID creado
- [ ] Credenciales de Google en application.properties
- [ ] Script SQL V6 ejecutado en Azure
- [ ] Columnas oauth2 agregadas a tabla usuario
- [ ] Aplicación compilada sin errores
- [ ] Login tradicional funciona
- [ ] Botón de Google aparece en /login
- [ ] Login con Google funciona
- [ ] Usuario OAuth2 se crea en BD

---

## 🚀 SIGUIENTE PASO: DESPLEGAR A AZURE

¿Quieres que te ayude a desplegar tu aplicación Spring Boot en Azure App Service?

Incluiría:
1. Crear Azure App Service
2. Configurar variables de entorno
3. Desplegar con Maven/GitHub Actions
4. Configurar dominio personalizado
5. Habilitar SSL/HTTPS
