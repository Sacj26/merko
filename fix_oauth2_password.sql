-- ================================================================
-- FIX: Permitir password NULL para usuarios OAuth2
-- ================================================================
-- Ejecuta este script en MySQL Workbench o tu cliente MySQL

USE merko;

-- Hacer que la columna password permita valores NULL
ALTER TABLE usuario MODIFY COLUMN password VARCHAR(255) NULL;

-- Verificar el cambio
DESCRIBE usuario;

-- Mostrar usuarios OAuth2 existentes (si los hay)
SELECT id, username, correo, nombre, apellido, google_id, oauth2_user, password 
FROM usuario 
WHERE oauth2_user = TRUE OR google_id IS NOT NULL;
