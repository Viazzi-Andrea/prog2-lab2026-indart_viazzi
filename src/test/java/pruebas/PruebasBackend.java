package pruebas;

import fachada.FachadaRegistros;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import modelo.Registro;
import persistencia.PersistenciaRegistros;

// Programa de comprobación sin bibliotecas de pruebas. Un error termina con código distinto de cero.
public class PruebasBackend {
    private static int comprobaciones;

    public static void main(String[] argumentos) throws Exception {
        Path carpeta = Files.createTempDirectory("prog2-pruebas-");
        try {
            probarModelo();
            probarOperaciones(carpeta);
            probarArchivos(carpeta);
            probarFalloEscritura(carpeta);
            PruebasControlador.ejecutar(carpeta);
            System.out.println("Backend: " + comprobaciones + " comprobaciones aprobadas.");
        } finally {
            borrarCarpeta(carpeta);
        }
    }

    private static Registro crearRegistro(int identificador, String estado) {
        return new Registro(identificador, "Proyector del salón", "Proyector", true,
                estado, 1, "Revisión técnica\nIncluye cable y lámpara.");
    }

    private static void probarModelo() throws Exception {
        comprobar(crearRegistro(1, "Prestado").getDiasPrestamo() == 1, "Límite inferior de días");
        Registro registro = crearRegistro(1, "Disponible");
        registro.setDiasPrestamo(15);
        comprobar(registro.getDiasPrestamo() == 15, "Límite superior de días");
        esperarError(IllegalArgumentException.class, "Los días de préstamo deben estar entre 1 y 15.",
                () -> registro.setDiasPrestamo(0));
        esperarError(IllegalArgumentException.class, "Los días de préstamo deben estar entre 1 y 15.",
                () -> registro.setDiasPrestamo(16));
        comprobar(registro.getDiasPrestamo() == 15, "Setter inválido no cambia el valor anterior");
        esperarError(IllegalArgumentException.class, "El nombre no puede quedar vacío.",
                () -> registro.setNombre("  "));
        esperarError(IllegalArgumentException.class, null, () -> registro.setNombre(null));
        esperarError(IllegalArgumentException.class, null, () -> crearRegistro(0, "Disponible"));
        esperarError(IllegalArgumentException.class, null, () -> crearRegistro(-1, "Disponible"));
        esperarError(IllegalArgumentException.class, null, () -> registro.setCategoria("General"));
        esperarError(IllegalArgumentException.class, null, () -> registro.setEstado("Roto"));
        esperarError(IllegalArgumentException.class, null, () -> registro.setObservaciones(null));
        esperarError(IllegalArgumentException.class, null,
                () -> new Registro(1, "Equipo", "Notebook", true, "Disponible", 0, ""));
        esperarError(IllegalArgumentException.class, null,
                () -> new Registro(1, "Equipo", "Notebook", true, "Disponible", 16, ""));
        Registro registroAnterior = new Registro(2, "Notebook", "Notebook", true);
        comprobar("Disponible".equals(registroAnterior.getEstado())
                && registroAnterior.getDiasPrestamo() == 1
                && registroAnterior.getObservaciones().isEmpty(), "Constructor original con valores válidos");
    }

    private static void probarOperaciones(Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("operaciones.json");
        FachadaRegistros fachada = new FachadaRegistros(new PersistenciaRegistros(archivo));
        esperarError(IOException.class, null, () -> fachada.registrar(crearRegistro(1, "Disponible")));
        fachada.cargarDatos();
        comprobar(fachada.listarRegistros().isEmpty() && !Files.exists(archivo), "Inicio sin archivo no escribe");
        Registro registroNuevo = crearRegistro(1, "En reparación");
        fachada.registrar(registroNuevo);
        comprobar(Files.exists(archivo) && fachada.listarRegistros().size() == 1, "Alta con autoguardado");
        esperarError(IllegalArgumentException.class, "Ya existe un equipo con ese identificador.",
                () -> fachada.registrar(registroNuevo));
        esperarError(IllegalArgumentException.class, null, () -> fachada.registrar(null));
        registroNuevo.setNombre("Cambio externo");
        fachada.listarRegistros().get(0).setEstado("Prestado");
        fachada.buscarPorId(1).setNombre("Otro cambio externo");
        fachada.filtrar("").get(0).setNombre("Filtro alterado");
        List<Registro> registrosCopiados = fachada.listarRegistros();
        registrosCopiados.clear();
        comprobar("Proyector del salón".equals(fachada.buscarPorId(1).getNombre())
                && "En reparación".equals(fachada.buscarPorId(1).getEstado()), "Lista y objetos protegidos");
        byte[] contenidoAnterior = Files.readAllBytes(archivo);
        esperarError(IllegalArgumentException.class, "No se puede prestar un equipo en reparación.",
                () -> fachada.modificar(crearRegistro(1, "Prestado")));
        comprobar(Arrays.equals(contenidoAnterior, Files.readAllBytes(archivo)), "Transición rechazada no escribe");
        fachada.modificar(crearRegistro(1, "Disponible"));
        Registro registroModificado = crearRegistro(1, "Prestado");
        registroModificado.setDiasPrestamo(15);
        registroModificado.setActivo(false);
        fachada.modificar(registroModificado);
        comprobar(fachada.listarRegistros().size() == 1
                && fachada.buscarPorId(1).getIdentificador() == 1
                && "Prestado".equals(fachada.buscarPorId(1).getEstado()), "Modificación conserva ID y cantidad");
        esperarError(IllegalArgumentException.class, null, () -> fachada.modificar(crearRegistro(99, "Disponible")));
        comprobar(fachada.buscarPorId(99) == null, "Consulta ausente devuelve null");
        comprobar(fachada.filtrar(" SALÓN ").size() == 1, "Filtro parcial por nombre");
        comprobar(fachada.filtrar(" PROYEC ").size() == 1, "Filtro parcial por categoría");
        comprobar(fachada.filtrar("  ").size() == 1, "Filtro vacío devuelve todos");
        comprobar(fachada.filtrar("inexistente").isEmpty(), "Filtro sin coincidencias");
        comprobar(fachada.filtrar("salon").isEmpty(), "No se promete equivalencia de tildes");
        fachada.guardarDatos();
        FachadaRegistros fachadaNueva = new FachadaRegistros(new PersistenciaRegistros(archivo));
        fachadaNueva.cargarDatos();
        Registro registroLeido = fachadaNueva.buscarPorId(1);
        comprobar(registroLeido.getIdentificador() == 1
                && registroLeido.getNombre().equals(registroModificado.getNombre())
                && registroLeido.getCategoria().equals(registroModificado.getCategoria())
                && !registroLeido.isActivo() && "Prestado".equals(registroLeido.getEstado())
                && registroLeido.getDiasPrestamo() == 15
                && registroLeido.getObservaciones().equals(registroModificado.getObservaciones()),
                "Nueva carga conserva siete campos, tildes y notas multilínea");
        comprobar(Files.readString(archivo, StandardCharsets.UTF_8).contains("salón"), "Archivo en UTF-8");
        fachada.eliminar(1);
        comprobar(fachada.listarRegistros().isEmpty()
                && new PersistenciaRegistros(archivo).leer().isEmpty(), "Eliminación guardada");
        esperarError(IllegalArgumentException.class, "No existe un equipo con ese identificador.",
                () -> fachada.eliminar(1));
        fachada.registrar(crearRegistro(1, "Prestado"));
        comprobar(fachada.buscarPorId(1) != null, "ID eliminado se puede reutilizar; alta inicial prestada permitida");
        fachada.registrar(new Registro(2, "Sala 2", "Aula", true, "Disponible", 1, ""));
        comprobar(fachada.filtrar("aula").size() == 1
                && fachada.filtrar("aula").get(0).getIdentificador() == 2, "Categoría coincide aunque el nombre no coincida");
    }

    private static void probarArchivos(Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("lectura.json");
        PersistenciaRegistros persistencia = new PersistenciaRegistros(archivo);
        Files.writeString(archivo, "[]");
        comprobar(persistencia.leer().isEmpty(), "Arreglo vacío válido");
        String registroAnterior = "{\"identificador\":1,\"nombre\":\"Equipo\",\"categoria\":\"Notebook\",\"activo\":true}";
        Files.writeString(archivo, "[" + registroAnterior + "]");
        byte[] contenidoAnterior = Files.readAllBytes(archivo);
        Registro registro = persistencia.leer().get(0);
        comprobar("Disponible".equals(registro.getEstado()) && registro.getDiasPrestamo() == 1
                && registro.getObservaciones().isEmpty(), "Compatibilidad limitada de campos ausentes");
        comprobar(Arrays.equals(contenidoAnterior, Files.readAllBytes(archivo)), "Carga antigua no reescribe");
        String[] contenidosInvalidos = {
            "", "null", "{}", "[", "[null]", "[1]", "[] []",
            "[" + registroAnterior + "," + registroAnterior + "]",
            "[" + registroAnterior.replace("Notebook", "General") + "]",
            "[" + registroAnterior.replace("Equipo", "  ") + "]",
            "[" + registroAnterior.replace("\"identificador\":1", "\"identificador\":0") + "]",
            "[" + registroAnterior.replace("\"identificador\":1", "\"identificador\":1.5") + "]",
            "[" + registroAnterior.replace("\"identificador\":1", "\"identificador\":2147483648") + "]",
            "[" + registroAnterior.replace("\"activo\":true", "\"activo\":\"true\"") + "]",
            "[" + registroAnterior.replace("\"nombre\":\"Equipo\"", "\"nombre\":123") + "]",
            "[" + registroAnterior.replace("\"activo\":true", "\"activo\":null") + "]",
            "[" + registroAnterior.replace(",\"activo\":true", "") + "]",
            "[" + registroAnterior.replace("}", ",\"estado\":null}") + "]",
            "[" + registroAnterior.replace("}", ",\"estado\":\"Roto\"}") + "]",
            "[" + registroAnterior.replace("}", ",\"diasPrestamo\":0}") + "]",
            "[" + registroAnterior.replace("}", ",\"diasPrestamo\":16}") + "]",
            "[" + registroAnterior.replace("}", ",\"observaciones\":null}") + "]"
        };
        for (String contenido : contenidosInvalidos) {
            Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
            FachadaRegistros fachada = new FachadaRegistros(persistencia);
            esperarError(IOException.class, null, () -> fachada.cargarDatos());
            comprobar(fachada.listarRegistros().isEmpty(), "Carga inválida no acepta una parte del archivo");
            esperarError(IOException.class, null, () -> fachada.guardarDatos());
            esperarError(IOException.class, null, () -> fachada.registrar(crearRegistro(2, "Disponible")));
            esperarError(IOException.class, null, () -> fachada.modificar(crearRegistro(1, "Disponible")));
            esperarError(IOException.class, null, () -> fachada.eliminar(1));
            comprobar(contenido.equals(Files.readString(archivo, StandardCharsets.UTF_8)), "Archivo inválido preservado");
        }
        Path directorio = Files.createDirectory(carpeta.resolve("no-es-un-archivo"));
        esperarError(IOException.class, null, () -> new PersistenciaRegistros(directorio).leer());
        byte[] bytesInvalidos = {(byte) 0xff, (byte) 0xfe};
        Files.write(archivo, bytesInvalidos);
        FachadaRegistros fachada = new FachadaRegistros(persistencia);
        esperarError(IOException.class, null, () -> fachada.cargarDatos());
        esperarError(IOException.class, null, () -> fachada.guardarDatos());
        comprobar(Arrays.equals(bytesInvalidos, Files.readAllBytes(archivo)), "UTF-8 ilegible preserva sus bytes originales");
    }

    private static void probarFalloEscritura(Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("fallos.json");
        PersistenciaConFallo persistencia = new PersistenciaConFallo(archivo);
        FachadaRegistros fachada = new FachadaRegistros(persistencia);
        fachada.cargarDatos();
        fachada.registrar(crearRegistro(1, "Disponible"));
        byte[] contenidoAnterior = Files.readAllBytes(archivo);
        persistencia.fallar = true;
        esperarError(IOException.class, null, () -> fachada.registrar(crearRegistro(2, "Disponible")));
        esperarError(IOException.class, null, () -> fachada.modificar(crearRegistro(1, "Prestado")));
        esperarError(IOException.class, null, () -> fachada.eliminar(1));
        esperarError(IOException.class, null, () -> fachada.guardarDatos());
        comprobar(fachada.listarRegistros().size() == 1 && fachada.buscarPorId(2) == null
                && "Disponible".equals(fachada.buscarPorId(1).getEstado()), "Escrituras fallidas no confirman memoria");
        comprobar(Arrays.equals(contenidoAnterior, Files.readAllBytes(archivo)), "Escrituras fallidas conservan bytes");
        persistencia.fallar = false;
        fachada.registrar(crearRegistro(2, "Disponible"));
        comprobar(fachada.listarRegistros().size() == 2, "Reintentar alta no produce duplicado");

        // Fallo real al reemplazar: el destino es una carpeta no vacía, no un JSON.
        Path destino = Files.createDirectory(carpeta.resolve("destino-ocupado"));
        Files.writeString(destino.resolve("conservar.txt"), "original");
        esperarError(IOException.class, null,
                () -> new PersistenciaRegistros(destino).escribir(fachada.listarRegistros()));
        comprobar("original".equals(Files.readString(destino.resolve("conservar.txt"))), "Fallo real conserva el destino");
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(carpeta)) {
            for (Path archivoTemporal : archivos) {
                comprobar(!archivoTemporal.getFileName().toString().endsWith(".tmp"), "No quedan temporales de escritura");
            }
        }
    }

    static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
        comprobaciones++;
    }

    private static void esperarError(Class<? extends Exception> tipo, String mensaje, Accion accion) throws Exception {
        try {
            accion.ejecutar();
        } catch (Exception error) {
            comprobar(tipo.isInstance(error), "Tipo de error esperado: " + tipo.getSimpleName() + "; recibido: " + error);
            if (mensaje != null) {
                comprobar(mensaje.equals(error.getMessage()), "Mensaje esperado: " + mensaje);
            }
            if (error instanceof IOException) {
                comprobar(error.getMessage() != null && !error.getMessage().isBlank(), "Error con contexto");
            }
            return;
        }
        throw new AssertionError("Se esperaba " + tipo.getSimpleName());
    }

    private static void borrarCarpeta(Path carpeta) throws IOException {
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(carpeta)) {
            for (Path archivo : archivos) {
                if (Files.isDirectory(archivo)) {
                    borrarCarpeta(archivo);
                } else {
                    Files.delete(archivo);
                }
            }
        }
        Files.delete(carpeta);
    }

    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static class PersistenciaConFallo extends PersistenciaRegistros {
        private boolean fallar;

        PersistenciaConFallo(Path archivo) {
            super(archivo);
        }

        @Override
        public void escribir(List<Registro> registros) throws IOException {
            if (fallar) {
                throw new IOException("Fallo de escritura simulado para comprobar el reintento.");
            }
            super.escribir(registros);
        }
    }
}
