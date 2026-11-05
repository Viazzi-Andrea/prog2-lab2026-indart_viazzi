package modelo;

public class Registro {
    private int identificador;
    private String nombre;
    private String categoria;
    private boolean activo;
    private String estado;
    private int diasPrestamo;
    private String observaciones;

    // Compatibilidad con el constructor original: recurso disponible, un día y sin notas.
    public Registro(int identificador, String nombre, String categoria, boolean activo) {
        this(identificador, nombre, categoria, activo, "Disponible", 1, "");
    }

    public Registro(int identificador, String nombre, String categoria, boolean activo,
            String estado, int diasPrestamo, String observaciones) {
        if (identificador <= 0) {
            throw new IllegalArgumentException("El identificador debe ser un entero positivo.");
        }
        validarNombre(nombre);
        validarCategoria(categoria);
        validarEstado(estado);
        validarDiasPrestamo(diasPrestamo);
        validarObservaciones(observaciones);
        this.identificador = identificador;
        this.nombre = nombre;
        this.categoria = categoria;
        this.activo = activo;
        this.estado = estado;
        this.diasPrestamo = diasPrestamo;
        this.observaciones = observaciones;
    }

    public int getIdentificador() {
        return identificador;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre;
    }

    public void setCategoria(String categoria) {
        validarCategoria(categoria);
        this.categoria = categoria;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEstado() {
        return estado;
    }

    public int getDiasPrestamo() {
        return diasPrestamo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setEstado(String estado) {
        validarEstado(estado);
        this.estado = estado;
    }

    public void setDiasPrestamo(int diasPrestamo) {
        validarDiasPrestamo(diasPrestamo);
        this.diasPrestamo = diasPrestamo;
    }

    public void setObservaciones(String observaciones) {
        validarObservaciones(observaciones);
        this.observaciones = observaciones;
    }

    // La copia vuelve a validar y evita compartir registros internos con otros objetos.
    public Registro copiar() {
        return new Registro(identificador, nombre, categoria, activo,
                estado, diasPrestamo, observaciones);
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede quedar vacío.");
        }
    }

    private static void validarCategoria(String categoria) {
        if (!"Notebook".equals(categoria) && !"Proyector".equals(categoria)
                && !"Kit de robótica".equals(categoria) && !"Aula".equals(categoria)) {
            throw new IllegalArgumentException("La categoría debe ser Notebook, Proyector, Kit de robótica o Aula.");
        }
    }

    private static void validarEstado(String estado) {
        if (!"Disponible".equals(estado) && !"Prestado".equals(estado)
                && !"En reparación".equals(estado)) {
            throw new IllegalArgumentException("El estado debe ser Disponible, Prestado o En reparación.");
        }
    }

    private static void validarDiasPrestamo(int diasPrestamo) {
        if (diasPrestamo < 1 || diasPrestamo > 15) {
            throw new IllegalArgumentException("Los días de préstamo deben estar entre 1 y 15.");
        }
    }

    private static void validarObservaciones(String observaciones) {
        if (observaciones == null) {
            throw new IllegalArgumentException("Las observaciones deben ser texto; pueden quedar vacías.");
        }
    }
}
