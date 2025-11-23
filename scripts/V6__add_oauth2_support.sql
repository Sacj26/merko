-- ============================================
-- Script: Agregar soporte OAuth2 (Google) a tabla usuario
-- Descripción: Agrega columnas necesarias para autenticación con Google
-- Fecha: 2025-11-23
-- ============================================

-- Agregar columnas para OAuth2
ALTER TABLE usuario 
ADD COLUMN google_id VARCHAR(255) UNIQUE AFTER ultimo_login,
ADD COLUMN profile_picture VARCHAR(500) AFTER google_id,
ADD COLUMN oauth2_user BOOLEAN DEFAULT FALSE AFTER profile_picture;

-- Crear índice para búsquedas por Google ID
CREATE INDEX idx_usuario_google_id ON usuario(google_id);

-- Hacer el campo password opcional (NULL) para usuarios OAuth2
ALTER TABLE usuario MODIFY COLUMN password VARCHAR(255) NULL;

-- Verificar los cambios
DESC usuario;

-- Consulta de ejemplo: Ver usuarios OAuth2
SELECT 
    id,
    username,
    correo as email,
    nombre,
    apellido,
    google_id,
    profile_picture,
    oauth2_user,
    activo,
    fecha_creacion
FROM usuario
WHERE oauth2_user = TRUE;
