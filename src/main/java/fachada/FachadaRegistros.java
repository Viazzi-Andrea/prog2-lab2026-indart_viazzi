package fachada;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import modelo.Registro;
import persistencia.PersistenciaRegistros;

public class FachadaRegistros {
    private final PersistenciaRegistros persistencia;
    private List<Registro> registros;
    private boolean cargaCorrecta;

    public FachadaRegistros(PersistenciaRegistros persistencia) {
        this.persistencia = persistencia;
        registros = new ArrayList<>();
    }

    public void cargarDatos() throws IOException {
        cargaCorrecta = false;
        registros = persistencia.leer();
        cargaCorrecta = true;
    }

    public List<Registro> listarRegistros() {
        List<Registro> registrosCopiados = new ArrayList<>();
        for (Registro registro : registros) {
            registrosCopiados.add(registro.copiar());
        }
        return registrosCopiados;
    }

    public void guardarDatos() throws IOException {
        comprobarCarga();
        persistencia.escribir(registros);
    }

    private void comprobarCarga() throws IOException {
        if (!cargaCorrecta) {
            throw new IOException("La carga no fue correcta. Recuperá el archivo y reiniciá la aplicación antes de guardar.");
        }
    }

    public void registrar(Registro registro) throws IOException {
        comprobarCarga();
        Registro registroNuevo = copiarRegistro(registro);
        if (buscarPorId(registroNuevo.getIdentificador()) != null) {
            throw new IllegalArgumentException("Ya existe un equipo con ese identificador.");
        }
        List<Registro> registrosActualizados = listarRegistros();
        registrosActualizados.add(registroNuevo);
        confirmarCambios(registrosActualizados);
    }

    public void modificar(Registro registro) throws IOException {
        comprobarCarga();
        Registro registroNuevo = copiarRegistro(registro);
        int posicion = buscarPosicion(registroNuevo.getIdentificador());
        Registro registroAnterior = registros.get(posicion);
        if ("En reparación".equals(registroAnterior.getEstado())
                && "Prestado".equals(registroNuevo.getEstado())) {
            throw new IllegalArgumentException("No se puede prestar un equipo en reparación.");
        }
        List<Registro> registrosActualizados = listarRegistros();
        registrosActualizados.set(posicion, registroNuevo);
        confirmarCambios(registrosActualizados);
    }

    public void eliminar(int identificador) throws IOException {
        comprobarCarga();
        int posicion = buscarPosicion(identificador);
        List<Registro> registrosActualizados = listarRegistros();
        registrosActualizados.remove(posicion);
        confirmarCambios(registrosActualizados);
    }

    // Una consulta sin coincidencia devuelve null; modificar y eliminar informan un error.
    public Registro buscarPorId(int identificador) {
        for (Registro registro : registros) {
            if (registro.getIdentificador() == identificador) {
                return registro.copiar();
            }
        }
        return null;
    }

    public List<Registro> filtrar(String texto) {
        if (texto == null) {
            texto = "";
        }
        String textoBusqueda = texto.trim().toLowerCase(Locale.ROOT);
        List<Registro> registrosFiltrados = new ArrayList<>();
        for (Registro registro : registros) {
            String nombre = registro.getNombre().toLowerCase(Locale.ROOT);
            String categoria = registro.getCategoria().toLowerCase(Locale.ROOT);
            if (nombre.contains(textoBusqueda) || categoria.contains(textoBusqueda)) {
                registrosFiltrados.add(registro.copiar());
            }
        }
        return registrosFiltrados;
    }

    private int buscarPosicion(int identificador) {
        for (int posicion = 0; posicion < registros.size(); posicion++) {
            if (registros.get(posicion).getIdentificador() == identificador) {
                return posicion;
            }
        }
        throw new IllegalArgumentException("No existe un equipo con ese identificador.");
    }

    private Registro copiarRegistro(Registro registro) {
        if (registro == null) {
            throw new IllegalArgumentException("Debe indicar un registro.");
        }
        return registro.copiar();
    }

    private void confirmarCambios(List<Registro> registrosActualizados) throws IOException {
        // Primero se escribe la propuesta. Si falla, la lista anterior sigue intacta.
        persistencia.escribir(registrosActualizados);
        registros = registrosActualizados;
    }
}
