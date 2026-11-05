package controlador;

import fachada.FachadaRegistros;
import java.io.IOException;
import modelo.Registro;
import vista.VentanaPrincipal;

public class ControladorRegistros {
    private final VentanaPrincipal vista;
    private final FachadaRegistros fachada;
    private String textoBusqueda = "";

    public ControladorRegistros(VentanaPrincipal vista, FachadaRegistros fachada) {
        this.vista = vista;
        this.fachada = fachada;
        vista.getBotonRegistrar().addActionListener(evento -> registrar());
        vista.getBotonModificar().addActionListener(evento -> modificar());
        vista.getBotonEliminar().addActionListener(evento -> eliminar());
        vista.getBotonBuscar().addActionListener(evento -> buscar());
        vista.getBotonLimpiar().addActionListener(evento -> limpiar());
        vista.getBotonGuardar().addActionListener(evento -> guardar());
        vista.getTablaRegistros().getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
    }

    public void iniciar() {
        vista.setVisible(true);
        try {
            fachada.cargarDatos();
            actualizarListado();
        } catch (IOException e) {
            vista.mostrarError("No se pudieron cargar los datos: " + e.getMessage()
                    + "\nEl archivo no fue modificado. Recuperalo y reiniciá la aplicación.");
        }
    }

    private int leerIdentificador() {
        String identificador = vista.getIdentificador();
        if (identificador.isBlank()) {
            throw new IllegalArgumentException("Ingresá el identificador.");
        }
        try {
            return Integer.parseInt(identificador);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El identificador debe ser un entero dentro del rango de int (hasta 2147483647).", e);
        }
    }

    private Registro leerFormulario(int identificador) {
        return new Registro(identificador, vista.getNombre(), vista.getCategoria(),
                vista.isActivo(), vista.getEstado(), vista.getDiasPrestamo(),
                vista.getObservaciones());
    }

    private void registrar() {
        try {
            Registro registroNuevo = leerFormulario(leerIdentificador());
            fachada.registrar(registroNuevo);
            terminarOperacion("Equipo registrado y guardado.");
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        } catch (IOException e) {
            mostrarErrorGuardado(e);
        }
    }

    private void modificar() {
        try {
            Registro registroSeleccionado = exigirSeleccion();
            int identificador = leerIdentificador();
            if (identificador != registroSeleccionado.getIdentificador()) {
                throw new IllegalArgumentException("No se puede cambiar el identificador del equipo seleccionado.");
            }
            Registro registroNuevo = leerFormulario(identificador);
            fachada.modificar(registroNuevo);
            terminarOperacion("Equipo modificado y guardado.");
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        } catch (IOException e) {
            mostrarErrorGuardado(e);
        }
    }

    private void eliminar() {
        try {
            Registro registroSeleccionado = exigirSeleccion();
            if (!vista.confirmarEliminacion(registroSeleccionado.getNombre())) {
                return;
            }
            fachada.eliminar(registroSeleccionado.getIdentificador());
            terminarOperacion("Equipo eliminado y cambios guardados.");
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        } catch (IOException e) {
            mostrarErrorGuardado(e);
        }
    }

    private Registro exigirSeleccion() {
        int identificador = vista.getIdentificadorSeleccionado();
        if (identificador == -1) {
            throw new IllegalArgumentException("Seleccioná un equipo del listado.");
        }
        Registro registroSeleccionado = fachada.buscarPorId(identificador);
        if (registroSeleccionado == null) {
            throw new IllegalArgumentException("El equipo seleccionado ya no existe.");
        }
        return registroSeleccionado;
    }

    private void cargarSeleccion() {
        int identificador = vista.getIdentificadorSeleccionado();
        if (identificador == -1) {
            return;
        }
        Registro registroSeleccionado = fachada.buscarPorId(identificador);
        if (registroSeleccionado != null) {
            vista.cargarFormulario(registroSeleccionado.getIdentificador(),
                    registroSeleccionado.getNombre(), registroSeleccionado.getCategoria(),
                    registroSeleccionado.isActivo(), registroSeleccionado.getEstado(),
                    registroSeleccionado.getDiasPrestamo(), registroSeleccionado.getObservaciones());
        }
    }

    private void buscar() {
        textoBusqueda = vista.getTextoBusqueda();
        actualizarListado();
        vista.limpiarFormulario();
    }

    private void limpiar() {
        textoBusqueda = "";
        vista.getCampoBusqueda().setText("");
        actualizarListado();
        vista.limpiarFormulario();
    }

    private void actualizarListado() {
        // Se conserva el último filtro aplicado, no un texto todavía sin buscar.
        vista.mostrarRegistros(fachada.filtrar(textoBusqueda));
    }

    private void terminarOperacion(String mensaje) {
        actualizarListado();
        vista.limpiarFormulario();
        vista.mostrarMensaje(mensaje);
    }

    private void guardar() {
        try {
            fachada.guardarDatos();
            vista.mostrarMensaje("Datos guardados. Guardar repite el guardado del listado; no incorpora cambios del formulario.");
        } catch (IOException e) {
            mostrarErrorGuardado(e);
        }
    }

    private void mostrarErrorGuardado(IOException error) {
        vista.mostrarError("No se guardaron los cambios: " + error.getMessage()
                + "\nLos datos anteriores se conservaron. Podés corregir el problema y reintentar.");
    }
}
