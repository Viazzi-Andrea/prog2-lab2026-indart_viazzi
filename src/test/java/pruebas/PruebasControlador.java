package pruebas;

import controlador.ControladorRegistros;
import fachada.FachadaRegistros;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import modelo.Registro;
import persistencia.PersistenciaRegistros;
import vista.VentanaPrincipal;

// Usa los componentes reales, pero captura los diálogos para no necesitar clics manuales.
class PruebasControlador {
    static void ejecutar(Path carpeta) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Controlador/Swing: omitido porque no hay entorno gráfico.");
            return;
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                probarBotones(carpeta);
                probarReintento(carpeta);
            } catch (Exception e) {
                throw new RuntimeException("Falló la comprobación del controlador.", e);
            }
        });
        System.out.println("Controlador/Swing: selección, seis botones, errores, eliminación y reapertura comprobados.");
    }

    private static void probarReintento(Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("reintento-controlador.json");
        PersistenciaConFallo persistencia = new PersistenciaConFallo(archivo);
        FachadaRegistros fachada = new FachadaRegistros(persistencia);
        VentanaDePrueba vista = new VentanaDePrueba();
        try {
            new ControladorRegistros(vista, fachada).iniciar();
            vista.cargarFormulario(1, "Equipo para reintentar", "Notebook", true, "Disponible", 1, "");
            vista.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(vista.error.contains("No se guardaron") && vista.mensaje.isEmpty()
                    && "Equipo para reintentar".equals(vista.getNombre())
                    && fachada.listarRegistros().isEmpty() && !Files.exists(archivo),
                    "Error de escritura conserva formulario y no confirma memoria ni éxito");
            persistencia.fallar = false;
            vista.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(fachada.listarRegistros().size() == 1
                    && vista.getIdentificador().isEmpty(), "Reintento desde el mismo formulario funciona");
        } finally {
            vista.dispose();
        }
    }

    private static void probarBotones(Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("controlador.json");
        VentanaDePrueba vista = new VentanaDePrueba();
        try {
            FachadaRegistros fachada = new FachadaRegistros(new PersistenciaRegistros(archivo));
            ControladorRegistros controlador = new ControladorRegistros(vista, fachada);
            controlador.iniciar();
            vista.getBotonModificar().doClick();
            PruebasBackend.comprobar(vista.error.contains("Seleccioná"), "Modificar exige selección");
            vista.getBotonEliminar().doClick();
            PruebasBackend.comprobar(vista.error.contains("Seleccioná"), "Eliminar exige selección");
            vista.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(vista.error.contains("Ingresá"), "ID vacío informado");
            vista.getCampoIdentificador().setText("2147483648");
            vista.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(vista.error.contains("rango"), "ID fuera de rango informado");
            vista.getCampoIdentificador().setText("abc");
            vista.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(vista.error.contains("entero"), "ID no entero informado");
            vista.cargarFormulario(-1, "Equipo", "Notebook", true, "Disponible", 1, "");
            vista.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(vista.error.contains("positivo"), "ID negativo rechazado");
            vista.cargarFormulario(1, "", "Notebook", true, "Disponible", 1, "");
            vista.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(vista.error.equals("El nombre no puede quedar vacío.")
                    && "1".equals(vista.getIdentificador()), "Validación conserva el formulario");
            vista.cargarFormulario(1, "Notebook de prueba", "Notebook", true, "Disponible", 15, "Nota\nTécnica");
            vista.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(vista.getTablaRegistros().getRowCount() == 1
                    && vista.getIdentificador().isEmpty() && vista.mensaje.contains("registrado"), "Registrar actualiza y limpia");
            vista.getTablaRegistros().setRowSelectionInterval(0, 0);
            PruebasBackend.comprobar("Notebook de prueba".equals(vista.getNombre())
                    && vista.getDiasPrestamo() == 15 && "Nota\nTécnica".equals(vista.getObservaciones()),
                    "Listener de selección carga los siete datos sin depender del ratón");
            vista.getCampoIdentificador().setText("2");
            vista.getBotonModificar().doClick();
            PruebasBackend.comprobar(vista.error.contains("No se puede cambiar")
                    && fachada.buscarPorId(2) == null, "No modifica otra identidad");
            vista.getCampoIdentificador().setText("1");
            vista.getCampoNombre().setText("Notebook actualizada");
            vista.getBotonModificar().doClick();
            PruebasBackend.comprobar("Notebook actualizada".equals(fachada.buscarPorId(1).getNombre())
                    && fachada.listarRegistros().size() == 1, "Botón Modificar reemplaza, no duplica");
            vista.getCampoBusqueda().setText("no coincide");
            vista.getBotonBuscar().doClick();
            PruebasBackend.comprobar(vista.getTablaRegistros().getRowCount() == 0, "Botón Buscar aplica filtro");
            vista.cargarFormulario(2, "Otra notebook", "Notebook", true, "Disponible", 1, "");
            vista.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(vista.getTablaRegistros().getRowCount() == 0
                    && fachada.listarRegistros().size() == 2, "Alta conserva el filtro activo");
            vista.getBotonLimpiar().doClick();
            PruebasBackend.comprobar(vista.getCampoBusqueda().getText().isEmpty()
                    && vista.getTablaRegistros().getRowCount() == 2, "Limpiar borra búsqueda y restaura listado");
            vista.getTablaRegistros().setRowSelectionInterval(0, 0);
            byte[] contenidoAnterior = Files.readAllBytes(archivo);
            vista.confirmar = false;
            vista.getBotonEliminar().doClick();
            PruebasBackend.comprobar(fachada.listarRegistros().size() == 2
                    && Arrays.equals(contenidoAnterior, Files.readAllBytes(archivo)), "Cancelar eliminación no altera datos");
            vista.confirmar = true;
            vista.getBotonEliminar().doClick();
            PruebasBackend.comprobar(fachada.buscarPorId(1) == null
                    && vista.getTablaRegistros().getRowCount() == 1, "Confirmar eliminación guarda y actualiza");
            vista.getBotonGuardar().doClick();
            PruebasBackend.comprobar(vista.mensaje.startsWith("Datos guardados"), "Botón Guardar repite escritura");
        } finally {
            vista.dispose();
        }
        VentanaDePrueba vistaNueva = new VentanaDePrueba();
        try {
            FachadaRegistros fachadaNueva = new FachadaRegistros(new PersistenciaRegistros(archivo));
            new ControladorRegistros(vistaNueva, fachadaNueva).iniciar();
            PruebasBackend.comprobar(vistaNueva.getTablaRegistros().getRowCount() == 1
                    && fachadaNueva.buscarPorId(2) != null, "Cerrar y crear otra ventana recupera lo guardado");
        } finally {
            vistaNueva.dispose();
        }
        Files.writeString(archivo, "archivo dañado");
        VentanaDePrueba vistaDañada = new VentanaDePrueba();
        try {
            FachadaRegistros fachadaDañada = new FachadaRegistros(new PersistenciaRegistros(archivo));
            new ControladorRegistros(vistaDañada, fachadaDañada).iniciar();
            PruebasBackend.comprobar(vistaDañada.error.contains("Recuperalo"), "Error de carga informado");
            vistaDañada.cargarFormulario(3, "Reintento", "Notebook", true, "Disponible", 1, "");
            vistaDañada.mensaje = "";
            vistaDañada.getBotonRegistrar().doClick();
            PruebasBackend.comprobar(vistaDañada.error.contains("No se guardaron")
                    && vistaDañada.mensaje.isEmpty() && "Reintento".equals(vistaDañada.getNombre())
                    && "archivo dañado".equals(Files.readString(archivo)), "Guardado bloqueado no anuncia éxito ni limpia");
        } finally {
            vistaDañada.dispose();
        }
    }

    private static class VentanaDePrueba extends VentanaPrincipal {
        private String error = "";
        private String mensaje = "";
        private boolean confirmar;

        @Override
        public void setVisible(boolean visible) {
            // No abrir ventanas durante la comprobación automática.
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }

        @Override
        public void mostrarMensaje(String mensaje) {
            this.mensaje = mensaje;
        }

        @Override
        public boolean confirmarEliminacion(String nombre) {
            return confirmar;
        }
    }

    private static class PersistenciaConFallo extends PersistenciaRegistros {
        private boolean fallar = true;

        PersistenciaConFallo(Path archivo) {
            super(archivo);
        }

        @Override
        public void escribir(List<Registro> registros) throws IOException {
            if (fallar) {
                throw new IOException("Fallo de escritura simulado.");
            }
            super.escribir(registros);
        }
    }
}
