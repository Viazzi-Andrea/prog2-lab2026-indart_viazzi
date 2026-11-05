package persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import modelo.Registro;

public class PersistenciaRegistros {
    private final Path archivo;
    private final Gson gson;

    public PersistenciaRegistros(Path archivo) {
        this.archivo = archivo;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setStrictness(Strictness.STRICT)
                .create();
    }

    public List<Registro> leer() throws IOException {
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            JsonElement contenido = gson.fromJson(lector, JsonElement.class);
            if (contenido == null || !contenido.isJsonArray()) {
                throw new IllegalArgumentException("El archivo debe contener un arreglo JSON, aunque esté vacío.");
            }
            List<Registro> registros = new ArrayList<>();
            int numeroRegistro = 0;
            for (JsonElement elemento : contenido.getAsJsonArray()) {
                numeroRegistro++;
                try {
                    if (!elemento.isJsonObject()) {
                        throw new IllegalArgumentException("Debe ser un objeto JSON.");
                    }
                    Registro registro = leerRegistro(elemento.getAsJsonObject());
                    for (Registro registroAnterior : registros) {
                        if (registroAnterior.getIdentificador() == registro.getIdentificador()) {
                            throw new IllegalArgumentException("Ya existe un equipo con ese identificador.");
                        }
                    }
                    registros.add(registro);
                } catch (IllegalArgumentException e) {
                    throw new IOException("Registro " + numeroRegistro + " inválido: " + e.getMessage(), e);
                }
            }
            return registros;
        } catch (NoSuchFileException e) {
            return new ArrayList<>();
        } catch (JsonParseException | IllegalArgumentException e) {
            throw new IOException("Contenido inválido en " + archivo + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("No se pudo leer " + archivo + ": " + e.getMessage(), e);
        }
    }

    private Registro leerRegistro(JsonObject objeto) {
        int identificador = leerEntero(objeto, "identificador");
        String nombre = leerTexto(objeto, "nombre");
        String categoria = leerTexto(objeto, "categoria");
        JsonElement activo = exigirCampo(objeto, "activo");
        if (!activo.isJsonPrimitive() || !activo.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("El campo activo debe ser booleano.");
        }

        // Solo los campos nuevos ausentes admiten los valores del formato anterior.
        String estado = "Disponible";
        int diasPrestamo = 1;
        String observaciones = "";
        if (objeto.has("estado")) {
            estado = leerTexto(objeto, "estado");
        }
        if (objeto.has("diasPrestamo")) {
            diasPrestamo = leerEntero(objeto, "diasPrestamo");
        }
        if (objeto.has("observaciones")) {
            observaciones = leerTexto(objeto, "observaciones");
        }
        // No deserializamos directamente Registro: Gson podría omitir el constructor.
        return new Registro(identificador, nombre, categoria, activo.getAsBoolean(),
                estado, diasPrestamo, observaciones);
    }

    private JsonElement exigirCampo(JsonObject objeto, String nombreCampo) {
        JsonElement campo = objeto.get(nombreCampo);
        if (campo == null || campo.isJsonNull()) {
            throw new IllegalArgumentException("Falta un valor para el campo " + nombreCampo + ".");
        }
        return campo;
    }

    private String leerTexto(JsonObject objeto, String nombreCampo) {
        JsonElement campo = exigirCampo(objeto, nombreCampo);
        if (!campo.isJsonPrimitive() || !campo.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser texto.");
        }
        return campo.getAsString();
    }

    private int leerEntero(JsonObject objeto, String nombreCampo) {
        JsonElement campo = exigirCampo(objeto, nombreCampo);
        if (!campo.isJsonPrimitive() || !campo.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser un entero.");
        }
        try {
            // La conversión exacta rechaza fracciones y números fuera del rango de int.
            return campo.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser un entero dentro del rango de int.", e);
        }
    }

    public void escribir(List<Registro> registros) throws IOException {
        Path destino = archivo.toAbsolutePath();
        Path carpeta = destino.getParent();
        Files.createDirectories(carpeta);
        Path temporal = Files.createTempFile(carpeta, "registros-", ".tmp");
        try {
            try (Writer escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                try {
                    gson.toJson(registros, escritor);
                } catch (JsonParseException e) {
                    throw new IOException("No se pudo completar el archivo JSON.", e);
                }
            }
            // El destino solo cambia cuando el archivo temporal está completo y cerrado.
            Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            IOException error = new IOException("El sistema de archivos no admite reemplazo atómico. No se guardaron los cambios.", e);
            borrarTemporal(temporal, error);
            throw error;
        } catch (IOException e) {
            borrarTemporal(temporal, e);
            throw e;
        }
    }

    private void borrarTemporal(Path temporal, IOException error) {
        try {
            Files.deleteIfExists(temporal);
        } catch (IOException errorLimpieza) {
            // Conservamos el error original y también la causa de la limpieza fallida.
            error.addSuppressed(errorLimpieza);
        }
    }
}
