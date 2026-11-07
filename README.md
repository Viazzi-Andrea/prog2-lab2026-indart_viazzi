# Gestor de Préstamos del Aula de Informática

Trabajo de Programación 2 del CeRP del Suroeste, modalidad semipresencial.
Docente: Domingo Pérez. Año: 2026.
**Repositorio:** https://github.com/Viazzi-Andrea/prog2-lab2026-indart_viazzi

## Integrantes

- Mateo Indart Vega.
- Andrea Viazzi Moreira.

---

## 1. Problema y destinatarios

En un centro de educación media, el aula de informática concentra equipamiento que se comparte entre muchos docentes y grupos: notebooks, proyectores y kits de robótica, además del propio espacio del aula.

La gestión de ese equipamiento suele resolverse de manera informal: un cuaderno en la puerta, un mensaje al referente del aula o un acuerdo verbal entre colegas. Ese mecanismo produce cuatro problemas recurrentes:

- No se sabe con certeza qué recursos están disponibles en un momento dado.
- No queda registro de qué equipo salió, ni desde cuándo.
- Un equipo que se rompió sigue figurando como prestable hasta que alguien avisa.
- El aula se superpone entre grupos porque no hay un único lugar donde consultarlo.

**Destinatario directo:** el docente referente o encargado del aula de informática, que administra el inventario y autoriza los préstamos.

**Destinatarios indirectos:** los docentes que solicitan equipamiento para sus clases y el equipo de dirección, que necesita conocer el estado del inventario para planificar reparaciones y reposiciones.

---

## 2. Utilidad de la solución

La aplicación reemplaza el registro informal por un inventario único y consultable:

- **Estado explícito de cada recurso.** Cada equipo indica si está Disponible, Prestado o En reparación, de modo que la consulta no depende de la memoria de quien atiende.
- **Préstamos acotados en el tiempo.** El plazo se registra en días, con un máximo definido, lo que evita los préstamos indefinidos que terminan en equipos extraviados.
- **Trazabilidad del deterioro.** El campo de observaciones permite anotar roturas, faltantes o condiciones particulares, información que en el registro en papel se pierde.
- **Conservación entre sesiones.** Los datos se guardan en un archivo JSON y se recuperan al volver a abrir la aplicación.
- **Funcionamiento autónomo.** No requiere conexión a internet, servidor ni base de datos: se ejecuta en la propia computadora del aula, condición necesaria en centros con conectividad intermitente.

---

## 3. Adaptación del modelo y reglas

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
3. **Un recurso en reparación no puede pasar a prestado.** Esta es la regla propia del problema: evita asignar equipamiento que no está en condiciones de uso.
4. **Plazo acotado entre 1 y 15 días.** Se controla en el modelo y además se restringe desde el componente de la interfaz.

---

## 4. Componentes gráficos

El prototipo incluía cinco tipos: `JTextField`, `JComboBox`, `JCheckBox`, `JButton` y `JTable`. El equipo agregó tres tipos más, todos con incidencia real sobre los datos.

| Componente | Función en el sistema |
| `JRadioButton` con `ButtonGroup` | Selección del estado del recurso. Se eligió por tratarse de opciones mutuamente excluyentes: un recurso no puede estar Disponible y En reparación a la vez. El `ButtonGroup` garantiza esa exclusividad y asegura que siempre haya una opción seleccionada. |
| `JSpinner` | Ingreso del plazo de préstamo. Configurado con un `SpinnerNumberModel` de 1 a 15, restringe el rango desde el propio componente, antes de que el dato llegue a la validación. |
| `JTextArea` | Registro de observaciones. Se eligió sobre `JTextField` porque el texto es de extensión variable y multilínea; está contenido en un `JScrollPane` para que el contenido extenso siga siendo accesible. |

El total asciende a **ocho tipos de componentes** con uso funcional.

---

## 5. Organización de paquetes y recorrido de una operación

| Paquete o carpeta | Responsabilidad |
| --- | --- |
| [app](src/main/java/app) | Inicia la aplicación, instala el Look and Feel y conecta los objetos entre sí. |
| [modelo](src/main/java/modelo) | La clase `Registro` representa un recurso del aula y valida sus propios datos. |
| [vista](src/main/java/vista) | `VentanaPrincipal` presenta la interfaz Swing, recoge lo que ingresa el usuario y muestra los mensajes. |
| [controlador](src/main/java/controlador) | `ControladorRegistros` recibe los eventos de los botones y de la tabla, y coordina las operaciones mediante la fachada. |
| [fachada](src/main/java/fachada) | `FachadaRegistros` ofrece las operaciones del sistema y aplica las reglas que involucran a más de un registro. |
| [persistencia](src/main/java/persistencia) | `PersistenciaRegistros` lee y escribe el archivo JSON, sin depender de Swing. |
| [datos](datos) | Carpeta, no paquete Java: contiene `registros.json`. |

La vista no lee ni escribe archivos y no decide reglas de negocio. Entrega y recibe datos simples (`String`, `int`, `boolean`) y nunca objetos `Registro`, salvo en `mostrarRegistros`, que únicamente los vuelca en la tabla. Esa separación permitió que los dos integrantes desarrollaran su parte en paralelo acordando previamente las firmas de los métodos.

### Recorrido de una operación: registrar un equipo

1. **Vista.** El usuario completa el formulario y presiona *Registrar*.
2. **Controlador.** El listener invoca `registrar()`. `leerIdentificador()` toma el texto de `vista.getIdentificador()` y lo convierte a entero; si no es un número, informa el error sin continuar. `leerFormulario()` construye un `Registro` con el resto de los datos que pide a la vista.
3. **Modelo.** El constructor de `Registro` valida sus propios datos: nombre no vacío, identificador positivo, categoría y estado dentro de los valores admitidos y plazo entre 1 y 15 días. Ante un dato inválido lanza `IllegalArgumentException`.
4. **Fachada.** `registrar()` comprueba que el identificador no exista, arma una lista actualizada y la entrega a `confirmarCambios()`.
5. **Persistencia.** `escribir()` serializa la lista con Gson en un archivo temporal y recién entonces lo mueve sobre el original mediante un movimiento atómico. Si la escritura falla, el archivo anterior queda intacto.
6. **Regreso.** La lista en memoria se reemplaza solo si la escritura terminó correctamente. El controlador ejecuta `terminarOperacion()`, que actualiza la tabla, limpia el formulario y muestra la confirmación.
7. **Ante un error** en cualquier punto, el controlador captura la excepción y pide a la vista que muestre el mensaje. La aplicación no se cierra y el usuario puede corregir.

---

### Requisitos

- **JDK 25 o superior**, con `JAVA_HOME` apuntando al JDK. El proyecto se desarrolló y probó con Eclipse Temurin 25 en Windows.
- Un entorno gráfico, porque la interfaz es Swing.
- No es necesario instalar Maven: el repositorio incluye **Maven Wrapper**.
- Conexión a internet únicamente en la primera ejecución, para descargar Maven y las dependencias. La aplicación en uso no requiere conexión.

Para comprobar la instalación, `java -version` y `javac -version` deben indicar Java 25 o superior.

### Descargar el proyecto

```text
git clone https://github.com/Viazzi-Andrea/prog2-lab2026-indart_viazzi.git
cd prog2-lab2026-indart_viazzi
```

### Ejecutar

Desde la raíz del proyecto, donde está `pom.xml`.

**Windows, PowerShell**

```powershell
.\mvnw.cmd compile exec:java
```

**macOS y Linux**

```sh
sh mvnw compile exec:java
```

Si se modificó `pom.xml`, conviene agregar `clean`:

```powershell
.\mvnw.cmd clean compile exec:java
```

### Dependencias

| Librería | Versión | Para qué | Origen |
| --- | --- | --- | --- |
| Gson | 2.13.2 | Lectura y escritura del archivo JSON. | Venía con el prototipo. |
| FlatLaf | 3.5.4 | Look and Feel de la interfaz. | Agregada por el equipo. |
| Swing | Incluido en Java | Componentes de la interfaz gráfica. | Venía con el prototipo. |

Ambas dependencias externas están declaradas en [pom.xml](pom.xml) y Maven las descarga automáticamente.

### Archivo de datos

El archivo es [datos/registros.json](datos/registros.json) y contiene cuatro equipos de ejemplo, ficticios y sin datos personales reales. La ruta es relativa al directorio desde el cual se inicia la aplicación.

Si el archivo no existe, la aplicación inicia con el listado vacío y lo crea al guardar el primer equipo. Si es inválido, muestra un error, mantiene la ventana abierta, no lo reemplaza y bloquea el guardado posterior.

---

## 7. Pruebas realizadas y limitaciones conocidas

Las siguientes pruebas se ejecutaron sobre la versión final, el 7 de noviembre de 2026, en Windows con Eclipse Temurin 25.

| Acción | Resultado esperado | Resultado obtenido |
| --- | --- | --- |
| Registrar un equipo válido con todos sus datos. | Aparece en el listado y el formulario se limpia. | Correcto. Se registró y apareció en el listado. |
| Registrar dejando el nombre vacío. | Informar el motivo sin cerrar la aplicación. | Correcto. Se mostró "El nombre no puede quedar vacío." y la aplicación siguió funcionando. |
| Registrar con un identificador que ya existe. | Rechazar la operación e informar el motivo. | Correcto. Se mostró "Ya existe un equipo con ese identificador." y no se creó el duplicado. |
| Intentar pasar a Prestado un equipo En reparación (regla propia). | Rechazar la operación y explicar el motivo. | Correcto. Se mostró "No se puede prestar un equipo en reparación." y el equipo conservó su estado anterior. |
| Modificar las observaciones de un registro seleccionado. | Actualizarlo conservando su identificador, sin duplicarlo. | Correcto. Se actualizó la observación, conservó el identificador y no se generó un duplicado. |
| Cancelar una eliminación. | No eliminar el registro. | Correcto. El registro permaneció en el listado. |
| Confirmar la eliminación de otro registro. | Eliminarlo del listado y guardar el cambio. | Correcto. El registro se eliminó y el cambio se guardó. |
| Buscar por texto y luego limpiar. | Mostrar solo las coincidencias y después el listado completo. | Correcto. El filtro mostró únicamente los registros coincidentes en nombre o categoría, y al limpiar se restableció el listado. |
| Crear, modificar y eliminar; cerrar y volver a abrir. | Recuperar los cambios desde el JSON. | Correcto. Al reiniciar se recuperaron el registro creado, la modificación y la eliminación. |
| Iniciar sin archivo JSON, usando una copia de prueba. | Iniciar con el listado vacío y crear el archivo al guardar. | Correcto. Inició vacío y sin errores; al registrar el primer equipo se creó nuevamente el archivo. |
| Iniciar con un JSON inválido, usando una copia de prueba. | Informar el error sin cerrar la ventana ni sobrescribir el archivo. | Correcto. Informó el problema, bloqueó el guardado posterior y conservó el archivo sin sobrescribirlo. |

### Limitaciones conocidas

- **Aplicación monousuario.** No está prevista para que dos personas trabajen sobre el mismo archivo al mismo tiempo.
- **El identificador lo ingresa el usuario.** El sistema verifica que no se repita, pero no lo genera automáticamente.
- **El plazo se guarda en días, no en fechas.** No se registra la fecha de inicio ni la de vencimiento, de modo que el sistema no puede avisar de préstamos vencidos.
- **No se registra quién retira el equipo.** Ese dato puede anotarse en observaciones, pero no es un campo propio ni es consultable.
- **El botón Guardar reescribe el listado actual** y no incorpora lo que esté cargado en el formulario; para eso están Registrar y Modificar.
- **La validación del plazo no puede incumplirse desde la interfaz**, porque el `JSpinner` ya restringe el rango de 1 a 15. La regla igualmente existe en el modelo, de modo que los datos provenientes del archivo JSON también quedan validados.
- **La ejecución se comprobó únicamente en Windows.** El proyecto es compatible por diseño con macOS y Linux, pero no fue verificado en esos sistemas.

---

## 8. Procedencia del código y recursos utilizados

### Qué proviene del prototipo

Estructura de paquetes MVC con fachada, configuración Maven con Wrapper, infraestructura de lectura y escritura JSON con Gson, la clase `Registro` con sus cuatro atributos originales, los métodos `cargarDatos`, `listarRegistros` y `guardarDatos` de la fachada, la ventana con los cinco tipos de componentes iniciales y los avisos de funcionalidad pendiente en los botones.

### Qué implementó el equipo

- Los tres atributos nuevos del modelo con sus validaciones.
- Las cuatro reglas de validación y el manejo de errores mediante excepciones.
- Las operaciones `registrar`, `modificar`, `eliminar`, `buscarPorId` y `filtrar` de la fachada.
- El guardado en dos pasos mediante archivo temporal y la lectura defensiva del JSON.
- El controlador completo, con el cableado de los seis botones y de la selección en la tabla.
- Los tres componentes nuevos de la vista, el rediseño del formulario con `GridBagLayout`, las siete columnas de la tabla y los trece métodos que la vista expone al controlador.
- La incorporación de FlatLaf como Look and Feel.

### Bibliotecas

Gson 2.13.2 y FlatLaf 3.5.4, ambas declaradas en `pom.xml`. Swing forma parte de la biblioteca estándar de Java.

### Documentación y herramientas de IA

- Documentación oficial de Swing (Oracle) para el uso de `GridBagLayout`, `JSpinner` y `ButtonGroup`.
- [refactoring.guru](https://refactoring.guru/es/design-patterns/facade) para comprender el patrón Fachada.
- Documentación de FlatLaf para su instalación y configuración.
- El equipo utilizó asistentes de IA (Claude y ChatGPT) como apoyo durante el desarrollo, en un trabajo diseñado, integrado y probado por nosotros. Los usos concretos fueron: consultar el funcionamiento de componentes y gestores de disposición de Swing y contrastar alternativas de diseño, por ejemplo por qué GridBagLayout resulta más adecuado que GridLayout para un formulario con filas de distinta altura; revisar fragmentos de código que luego fueron integrados, compilados y verificados; definir por anticipado el contrato de métodos entre la vista y el controlador, lo que permitió a los dos integrantes trabajar en paralelo sin bloquearse; redactar los comentarios explicativos del código; apoyar el uso de Git desde la terminal; y redactar esta documentación. Las decisiones de diseño y el funcionamiento del código pueden explicarse en detalle.

Adicionalmente, los constructores y parte de la estructura de clases del backend se adaptaron a partir de un programa anterior de uno de los integrantes, ajustado a los atributos, las categorías y las reglas de este laboratorio.

---

## 9. Aportes de cada integrante

| Integrante | Aportes realizados |
| --- | --- |
| Mateo Indart Vega | Modelo (`Registro` con sus atributos nuevos y validaciones), fachada (`registrar`, `modificar`, `eliminar`, `buscarPorId`, `filtrar` y el guardado en dos pasos), persistencia (lectura defensiva del JSON y escritura mediante archivo temporal) y controlador completo. Clases de comprobación del backend en `src/test/java/pruebas`. |
| Andrea Viazzi Moreira | Vista completa: los tres componentes nuevos, el rediseño del formulario con `GridBagLayout`, las siete columnas de la tabla, los trece métodos de contacto con el controlador y la incorporación de FlatLaf. Documento de acuerdos del equipo, este README, la batería de pruebas funcionales y el guion y montaje del video. |

Ambos integrantes conocen la solución completa y participan en el video explicando su aporte.

---

## Video explicativo

**Pendiente de crear y publicar. No hay un video disponible todavía.**

Enlace público: **pendiente de agregar**.

El video debe durar como máximo cinco minutos, incluir la participación de todos
los integrantes y mostrar el problema, la aplicación, una validación, los tres
componentes nuevos, el recorrido de una operación y la conservación de datos.

Lo alojaremos en una plataforma externa, sin subir el archivo al repositorio.
Antes de entregar, comprobaremos que el enlace permita verlo desde una ventana
privada, sin iniciar sesión ni solicitar permisos.
