-- Fix rol enum values: convert lowercase to uppercase to match Java enum
UPDATE usuario SET rol = 'ADMIN' WHERE LOWER(rol) = 'admin';
UPDATE usuario SET rol = 'CLIENTE' WHERE LOWER(rol) = 'cliente';
