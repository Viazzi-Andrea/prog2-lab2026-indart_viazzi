# Gestor de Préstamos del Aula de Informática

Trabajo de Programación 2 del CeRP del Suroeste, modalidad semipresencial.
Docente: Domingo Pérez. Año: 2026.

## Integrantes

- Mateo Indart Vega.
- Andrea Viazzi Moreira.


## Estado del proyecto

Partimos del prototipo docente de un gestor de registros en Java y Swing,
organizado con MVC, fachada y persistencia en JSON.

**Actualmente solo está disponible el prototipo.** La ventana muestra tres
registros de ejemplo del archivo JSON. Los botones Registrar, Modificar,
Eliminar, Buscar, Limpiar y Guardar muestran un aviso de funcionalidad pendiente.
Seleccionar una fila todavía no carga sus datos en el formulario.

Los apartados marcados como **Pendiente** deben completarse con nuestro
desarrollo real antes de entregar. La [consigna del laboratorio](docs/letra.md)
detalla los requisitos.

## Problema y destinatarios

En un centro de educación media, el aula de informática concentra equipamiento que se comparte entre muchos docentes y grupos: notebooks, proyectores y kits de robótica, además del propio espacio del aula.

La gestión de ese equipamiento suele resolverse de manera informal: un cuaderno en la puerta, un mensaje al referente del aula o un acuerdo verbal entre colegas.
Ese mecanismo produce cuatro problemas recurrentes:

- No se sabe con certeza qué recursos están disponibles en un momento dado.
- No queda registro de qué equipo salió, ni desde cuándo.
- Un equipo que se rompió sigue figurando como prestable hasta que alguien avisa.
- El aula se superpone entre grupos porque no hay un único lugar donde consultarlo.

**Destinatario directo:** el docente referente o encargado del aula de
informática, que es quien administra el inventario y autoriza los préstamos.

**Destinatarios indirectos:** los docentes que solicitan equipamiento para sus
clases y el equipo de dirección, que necesita conocer el estado del inventario
para planificar reparaciones y reposiciones.

## Utilidad de la solución

La aplicación reemplaza el registro informal por un inventario único y consultable, y aporta concretamente lo siguiente:

- **Estado explícito de cada recurso.** Cada equipo indica si está Disponible, Prestado o En reparación, de modo que la consulta no depende de la memoria de quien atiende.
- **Préstamos acotados en el tiempo.** El plazo se registra en días, con un máximo definido, lo que evita los préstamos indefinidos que en la práctica terminan en equipos extraviados.
- **Trazabilidad del deterioro.** El campo de observaciones permite anotar roturas, faltantes o condiciones particulares de cada recurso, información que en el registro en papel se pierde.
- **Conservación entre sesiones.** Los datos se guardan en un archivo JSON y se recuperan al volver a abrir la aplicación.
- **Funcionamiento autónomo.** No requiere conexión a internet, servidor ni base de datos: se ejecuta en la propia computadora del aula, condición necesaria en centros con conectividad intermitente.

## Adaptación del modelo y reglas

El prototipo entregado gestionaba registros genéricos con cuatro atributos. La adaptación consistió en reinterpretar esos atributos y agregar tres propios del problema.

### Reinterpretación de los atributos del prototipo

| Atributo del prototipo | Significado en este sistema |
| `identificador` | Número de inventario del recurso. Único y no reutilizable. |
| `nombre` | Denominación del recurso (por ejemplo, "Notebook Lenovo 03"). |
| `categoria` | Tipo de recurso. |
| `activo` | Indica si el recurso sigue dado de alta en el inventario. Un equipo dado de baja conserva su historial pero deja de ofrecerse. |

### Atributos agregados por el equipo

| Atributo | Tipo | Función |
| `estado` | `String` | Situación actual del recurso: Disponible, Prestado o En reparación. |
| `diasPrestamo` | `int` | Plazo del préstamo, acotado entre 1 y 15 días. |
| `observaciones` | `String` | Texto libre para registrar roturas, faltantes o condiciones de uso. |

### Categorías definidas

Notebook, Proyector, Kit de robótica y Aula.

La inclusión de **Aula** como categoría es una decisión de diseño deliberada:
trata el espacio físico como un recurso más del inventario. De ese modo, reservar el aula para un grupo recorre exactamente el mismo circuito que prestar un proyector, sin duplicar lógica ni pantallas.

### Reglas de validación

1. **Nombre obligatorio.** No se admite un recurso sin denominación, porque el listado quedaría inutilizable.
2. **Identificador único.** No se permite registrar dos recursos con el mismo número de inventario.
3. **Un recurso en reparación no puede prestarse.** Esta es la regla propia del problema: evita asignar equipamiento que no está en condiciones de uso.
4. **Plazo acotado.** El préstamo debe ubicarse entre 1 y 15 días.

---

## Componentes gráficos

La base incluye JTextField, JComboBox, JCheckBox, JButton y JTable. El equipo agregó tres tipos más, todos con
incidencia real sobre los datos.

Los tres tipos nuevos todavía no fueron elegidos ni implementados:

| Componente | Función en el sistema 
| `JRadioButton` (con `ButtonGroup`) | Selección del estado del recurso. Se eligió por tratarse de opciones mutuamente excluyentes: un recurso no puede estar Disponible y En reparación a la vez. El `ButtonGroup` garantiza esa exclusividad y asegura que siempre haya una opción seleccionada. |
| `JSpinner` | Ingreso del plazo de préstamo. Configurado con un `SpinnerNumberModel` de 1 a 15, restringe el rango desde el propio componente, antes de que el dato llegue a la validación. |
| `JTextArea` | Registro de observaciones. Se eligió sobre `JTextField` porque el texto es de extensión variable y multilínea; está contenido en un `JScrollPane` para que el contenido extenso siga siendo accesible. |

El total asciende a **ocho tipos de componentes** con uso funcional.

## Organización de paquetes y recorrido de una operación

| Paquete o carpeta | Responsabilidad |
| [app](src/main/java/app) | Iniciar la aplicación y conectar los objetos. |
| [modelo](src/main/java/modelo) | Representar los registros y, al completar el proyecto, sus reglas. |
| [vista](src/main/java/vista) | Mostrar la interfaz Swing y los mensajes. |
| [controlador](src/main/java/controlador) | Recibir eventos y coordinar las acciones mediante la fachada. |
| [fachada](src/main/java/fachada) | Ofrecer operaciones y coordinar el modelo con la persistencia. |
| [persistencia](src/main/java/persistencia) | Leer y escribir JSON, sin depender de Swing. |
| [datos](datos) | Contener el archivo JSON; no es un paquete Java. |

El recorrido que ya funciona es la carga inicial:
[Aplicacion](src/main/java/app/Aplicacion.java) conecta los objetos;
el controlador solicita la carga a la fachada; la fachada utiliza la
persistencia para leer el archivo; y el controlador entrega los registros a la
vista para mostrarlos en la tabla. La vista no lee ni escribe archivos.

**Pendiente de documentar:** el recorrido completo de una operación de gestión
implementada por nuestro equipo.

## Instrucciones de ejecución

### Requisitos

- JDK 25 LTS o superior, con `JAVA_HOME` apuntando al JDK y su carpeta `bin`
  en el `PATH`, para disponer de los comandos `java` y `javac`.
- Windows, macOS o Linux con un entorno gráfico para mostrar Swing.
- Git, si se descarga el proyecto mediante clonación.
- Conexión a Internet en la primera ejecución para descargar Maven y las
  dependencias. La aplicación utiliza un archivo local, sin base de datos ni
  conexión a Internet durante su uso.

No es necesario instalar Maven ni agregarlo al `PATH`: el repositorio incluye
Maven Wrapper, que descarga y utiliza Maven 3.9.11 automáticamente.
La compilación genera clases para Java 25; no se pueden ejecutar con Java 17
ni Java 21. Maven Wrapper utiliza el JDK indicado por `JAVA_HOME`.

Después de instalar o cambiar el JDK, reiniciar la terminal y VS Code.
Comprobar que `java -version`, `javac -version` y `.\mvnw.cmd -version`
(Windows) o `sh mvnw -version` (macOS y Linux) indiquen Java 25 o superior.

### Descargar el proyecto

**Pendiente:** reemplazar `USUARIO` y `REPOSITORIO` por los datos del repositorio
del equipo antes de entregar.

```text
git clone https://github.com/USUARIO/REPOSITORIO.git
cd REPOSITORIO
```

El nombre del repositorio debe seguir el formato indicado en la consigna:
`prog2-lab2026-apellido1_apellido2`.

Ejecutar los siguientes comandos desde la raíz del proyecto, donde se
encuentra [pom.xml](pom.xml).

### Windows: PowerShell

```powershell
.\mvnw.cmd compile exec:java
```

Para compilar sin abrir la ventana:

```powershell
.\mvnw.cmd compile
```

Si aparece «mvn no se reconoce», utilizar `.\mvnw.cmd` como en los comandos
anteriores. El prefijo `.\` indica a PowerShell que ejecute el lanzador de la
carpeta actual.

### macOS y Linux: terminal

```sh
sh mvnw compile exec:java
```

Para compilar sin abrir la ventana:

```sh
sh mvnw compile
```

Al utilizar `sh mvnw` no es necesario cambiar los permisos del archivo.
En Linux se requiere una sesión de escritorio; una terminal de servidor sin
entorno gráfico no puede mostrar la ventana.

### VS Code y dependencias

Abrir la carpeta raíz con Extension Pack for Java instalado e importar el
proyecto Maven. También se puede ejecutar el método `main` de
[Aplicacion](src/main/java/app/Aplicacion.java), con la raíz del proyecto como
directorio de trabajo.
En `Java: Configure Java Runtime`, seleccionar un JDK 25 para este proyecto.
La configuración local de VS Code selecciona el JDK 25 instalado en
`%USERPROFILE%\.jdks\jdk-25.0.4.1+1` para las terminales de Windows y el
proyecto Java, sin modificar el `PATH` del sistema. Si se utiliza otra
instalación, ajustar las rutas en `.vscode/settings.json`.

La biblioteca de persistencia es Gson 2.13.2. Está declarada, junto con los
complementos de compilación y ejecución, en [pom.xml](pom.xml).

### Archivo de datos

El archivo de ejemplo es [datos/registros.json](datos/registros.json).
La ruta es relativa al directorio desde el cual se inicia la aplicación.

Si el archivo no existe, la aplicación inicia con una lista vacía. Si es
inválido o ilegible, muestra un error y mantiene la ventana abierta sin
reemplazarlo. La fachada bloquea el guardado cuando la carga no fue correcta.
Los errores de escritura se propagan al controlador; falta integrar el guardado
y sus mensajes con las operaciones del equipo.

**En el estado actual, ningún botón guarda datos y cerrar la ventana tampoco
guarda.**

## Pruebas realizadas y limitaciones conocidas

### Comprobaciones heredadas del prototipo

La base fue verificada en Windows con OpenJDK 17.0.17 y Maven 3.9.11.
Es compatible por diseño con macOS y Linux, pero su ejecución en esos sistemas
todavía no fue comprobada.

Tras actualizar a Java 25 LTS, se verificó en Windows con Eclipse Temurin
25.0.4.1 y Maven 3.9.11 la compilación limpia mediante `.\mvnw.cmd clean verify`.
También se comprobaron la carga de los tres registros de ejemplo, la lectura
sin archivo, la escritura y lectura JSON con tildes, la copia del listado,
el rechazo de JSON inválido y el bloqueo del guardado tras una carga fallida.
No hay pruebas automatizadas en el repositorio; estas comprobaciones se
realizaron con un programa temporal. La interfaz gráfica no se volvió a
verificar como parte de esta actualización.

| Acción | Resultado esperado | Resultado obtenido en la base |
| --- | --- | --- |
| Compilar mediante Maven Wrapper en Windows. | Compilación sin errores. | Correcto. |
| Abrir la aplicación desde la raíz. | Mostrar los tres registros del JSON. | Correcto. |
| Pulsar los seis botones. | Mostrar avisos de funcionalidad pendiente sin modificar datos. | Correcto. |
| Iniciar sin JSON, con una ruta de prueba. | Mostrar la tabla vacía sin crear el archivo. | Correcto. |
| Iniciar con JSON inválido, usando una copia de prueba. | Informar el error sin cerrar la ventana ni modificar el archivo. | Correcto. |
| Leer y escribir registros en un archivo temporal. | Recuperar todos los atributos, incluidos caracteres con tildes. | Correcto. |
| Guardar desde la fachada tras una carga fallida. | Rechazar el guardado sin reemplazar el archivo. | Correcto. |

### Pruebas de nuestro desarrollo

**Pendiente:** realizar estas pruebas y reemplazar los resultados por lo
realmente observado. No se consideran aprobadas por estar incluidas en la tabla.

| Acción | Resultado esperado | Resultado obtenido |
| --- | --- | --- |
| Crear un registro válido. | Aparece en el listado con identificador único. | Pendiente: alta sin implementar. |
| Incumplir la primera validación. | Informar el motivo y permitir corregir. | Pendiente: validación sin implementar. |
| Incumplir la segunda validación. | Informar el motivo y permitir corregir. | Pendiente: validación sin implementar. |
| Incumplir la regla propia del problema. | Rechazar la operación y explicar el motivo. | Pendiente: regla sin implementar. |
| Modificar un registro seleccionado. | Actualizarlo conservando su identificador, sin duplicarlo. | Pendiente: modificación sin implementar. |
| Cancelar una eliminación y confirmar otra con un registro de prueba. | Conservar el primero y eliminar solo el confirmado. | Pendiente: eliminación sin implementar. |
| Aplicar una búsqueda o filtro. | Mostrar únicamente los registros que cumplen el criterio. | Pendiente: búsqueda sin implementar. |
| Limpiar el formulario. | Dejarlo preparado para una nueva operación. | Pendiente: limpieza sin implementar. |
| Crear y modificar datos, guardar, cerrar y volver a abrir. | Recuperar los cambios desde el JSON. | Pendiente: guardado sin integrar. |
| Iniciar sin JSON y con JSON inválido, usando copias de prueba. | Mantener el manejo de errores de la base. | Pendiente: volver a comprobar tras nuestros cambios. |

Las limitaciones actuales son las funciones pendientes descritas en este
documento. **Pendiente:** actualizar esta sección con las limitaciones reales
de nuestra entrega.

## Procedencia del código y recursos utilizados

- **Base docente:** estructura MVC con fachada, modelo genérico, ventana inicial,
  avisos de funcionalidad pendiente, carga de ejemplo, infraestructura JSON y
  configuración Maven con Wrapper.
- **Trabajo de nuestro equipo:** pendiente de implementar y detallar,
  identificando qué agregamos y qué modificamos.
- **Bibliotecas:** Gson 2.13.2 para leer y escribir JSON; Swing, incluido en Java,
  para la interfaz.
- **Tutoriales, documentación y herramientas de IA utilizados por el equipo:**
  pendiente de declarar. Indicar los recursos, su uso concreto y las partes
  correspondientes, o aclarar si no se utilizaron.

## Aportes de cada integrante

| Integrante | Aportes realizados |
| --- | --- |
| Pendiente: primer integrante. | Pendiente de registrar aportes concretos. |
| Pendiente: segundo integrante. | Pendiente de registrar aportes concretos. |

Agregar una tercera fila solamente si corresponde. Todos los integrantes deben
conocer la solución completa y participar en el video.

## Video explicativo

**Pendiente de crear y publicar. No hay un video disponible todavía.**

Enlace público: **pendiente de agregar**.

El video debe durar como máximo cinco minutos, incluir la participación de todos
los integrantes y mostrar el problema, la aplicación, una validación, los tres
componentes nuevos, el recorrido de una operación y la conservación de datos.

Lo alojaremos en una plataforma externa, sin subir el archivo al repositorio.
Antes de entregar, comprobaremos que el enlace permita verlo desde una ventana
privada, sin iniciar sesión ni solicitar permisos.
