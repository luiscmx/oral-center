# Oral Center

Oral Center es un sistema para un consultorio dental que permite organizar las citas y guardar los datos de pacientes, odontólogos y horarios.

## Objetivo

Buscamos facilitar el registro de citas y la consulta de horarios disponibles, para evitar que se asignen dos citas al mismo odontólogo en un mismo horario.

## Tecnologías y requisitos

- Java JDK 21.
- Swing para las ventanas.
- MySQL 8 y MySQL Workbench para la base de datos.
- Maven para descargar las librerías y compilar.
- NetBeans para abrir y trabajar con el proyecto.

## Instalación

Para preparar una base de datos nueva, abre MySQL Workbench y ejecuta estos archivos de la carpeta `sql` en este orden:

1. `01_base.sql`
2. `02_usuarios.sql`
3. `03_atenciones.sql`
4. `04_datos.sql`

Estos pasos crean la base `oral_center` y agregan datos de ejemplo. Se realizan una sola vez; si la base ya está preparada, puedes pasar a la configuración.

## Configuración

Copia `conexion.properties.example` y cambia el nombre de la copia a `conexion.properties`. Ambos archivos deben estar en la carpeta principal del proyecto, junto a `pom.xml`.

En la copia, completa `usuario` y `clave` con los datos de tu cuenta de MySQL. El campo `url` indica dónde está la base de datos; viene preparado para `oral_center` en el puerto `3306`. Si usas otro puerto, cámbialo allí.

El archivo `conexion.properties` se queda en tu equipo porque contiene tu clave de MySQL.

## Ejecución

Abre la carpeta del proyecto en NetBeans y espera a que cargue las librerías. Después compila el proyecto y ejecuta la clase `Aplicacion.java`.

También puedes usar estos comandos desde la carpeta del proyecto, si tienes Java y Maven disponibles en la terminal:

```bash
mvn package
java -jar target/oral-center-0.1.0-SNAPSHOT.jar
```

Después de compilar, también puedes abrir `INICIAR.cmd` en Windows. Mantén la carpeta `lib` junto al archivo JAR, ya que contiene las librerías del programa.

## Usuarios de ejemplo

Puedes ingresar con `admin`, `recepcion` u `odontologo`. Las tres cuentas usan la contraseña `Demo2026!` y tienen permisos diferentes. Para revisar todas las opciones, entra con `admin`.

Por ahora también se permite dejar la contraseña en blanco para las pruebas. Los datos incluidos en los scripts son ficticios.

## Estado actual

Ya se pueden registrar, buscar y editar pacientes, gestionar odontólogos y horarios, consultar disponibilidad y guardar citas. También se puede revisar la agenda con filtros y exportarla a Excel.

Todavía falta implementar la reprogramación y cancelación de citas, el cambio de sus estados y el historial de cada paciente.
