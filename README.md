# Gimnasio - Proyecto integrador Programación II

Aplicación web para administrar un gimnasio: socios, planes, actividades, cobro de cuotas y registro de ingresos por DNI.

**Materia:** Programación II - IFES
**Profesor:** Juan José Navarro
**Integrantes:** Ibáñez, Matías

## Tecnologías

- Java 17 y Maven
- Spring Boot 3 (Spring MVC)
- DataNucleus JDO para la persistencia
- MySQL
- Thymeleaf, HTML, CSS y Bootstrap 5

## Modelo

| Clase | Qué representa |
|---|---|
| `Persona` (abstracta) | DNI, nombre, apellido, email y clave. De ella heredan `Socio` y `Empleado`. |
| `Socio` | Tiene un `Plan`, sus pagos y los descuentos (grupo familiar 10%, empleado de comercio 20%). |
| `Empleado` | Usuario de la recepción (legajo). |
| `Plan` | Dos veces por semana ($24.000), tres veces ($27.000) o pase libre ($30.000). Un plan tiene muchos socios (relación 1 a N). |
| `Actividad` | Día, horario y cupo. Tiene una lista de socios inscriptos (relación N a N). |
| `Pago` | Cuota cobrada a un socio: fecha, monto, medio de pago y período que cubre. |
| `Ingreso` | Registro de cada vez que un socio entra al gimnasio. |

## Capas

```
com.programacion2
├── app            Aplicacion (arranque de Spring Boot)
├── configuracion  Beans de Spring, PersistenceManagerFactory, control de sesión, datos iniciales
├── dominio        Clases persistentes (JDO)
├── repositorio    IRepositorio<T> y Repositorio<T> genérico
├── servicio       Interfaces y clases de servicio (reglas de negocio)
├── controlador    Controladores MVC
└── excepcion      ServicioException
```

## Funcionalidades

- Inicio de sesión de empleados y socios (cada uno ve solo su parte).
- ABM de socios con búsqueda por nombre, apellido o DNI, y filtro por plan y estado del abono.
- ABM de actividades con filtro por día.
- Consulta de planes y de los socios de cada plan.
- Cobro de cuota: calcula el monto con los descuentos y el período que cubre.
- **Operación de negocio: inscribir un socio a una actividad.** Valida que el socio esté activo y con el abono al día, que la actividad tenga cupo, que no supere las actividades de su plan y que no se superponga con otra en el mismo horario.
- Registro de ingreso por DNI: dice si puede pasar y a qué actividad viene.
- Portal del socio: datos del abono, pagos, ingresos, anotarse o darse de baja de actividades y cambiar la clave.
- Baja de socio sin inconsistencias: se lo saca de sus actividades y queda inactivo, conservando sus pagos e ingresos.

## Cómo ejecutarlo

1. Tener instalados JDK 17, Maven y MySQL.
2. Abrir `sql/crear_base.sql`, cambiar `tu_clave` por una clave propia y ejecutarlo en MySQL Workbench. Crea la base `gimnasio` y su usuario. Las tablas las crea DataNucleus al arrancar.
3. Definir la clave de la base en una variable de entorno (no se guarda en el repositorio):
   - Windows (PowerShell): `$env:DB_PASSWORD="tu_clave"`
   - Linux o Mac: `export DB_PASSWORD=tu_clave`
4. En la carpeta del proyecto ejecutar:
   ```
   mvn spring-boot:run
   ```
5. Entrar a http://localhost:8080

## Usuarios de prueba

La primera vez que arranca, la aplicación carga los planes, cinco actividades, un empleado y tres socios.

| Rol | DNI | Clave |
|---|---|---|
| Empleado | 11111111 | admin123 |
| Socia (con abono al día) | 40111222 | 40111222 |
| Socio (pase libre) | 38222333 | 38222333 |
| Socia (sin pagar) | 42333444 | 42333444 |

A los socios nuevos se les asigna su DNI como clave inicial.
