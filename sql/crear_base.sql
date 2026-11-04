-- Ejecutar UNA vez en MySQL Workbench, conectado como root.
-- Cambiá 'tu_clave' por la contraseña que quieras para el usuario de la aplicación
-- y usá esa misma en la variable de entorno DB_PASSWORD (ver README.md).

-- Esquema (= base de datos)
CREATE DATABASE IF NOT EXISTS gimnasio;

-- Usuario propio para la aplicación, con permisos solo sobre su esquema
CREATE USER IF NOT EXISTS 'gimnasio'@'localhost' IDENTIFIED BY 'tu_clave';
GRANT ALL PRIVILEGES ON gimnasio.* TO 'gimnasio'@'localhost';
FLUSH PRIVILEGES;

-- Las tablas NO se crean acá: las crea DataNucleus al arrancar la aplicación
-- a partir de las anotaciones de las clases del dominio.
-- Los planes, el empleado administrador y datos de ejemplo los carga
-- la clase DatosIniciales la primera vez (si la base está vacía).
