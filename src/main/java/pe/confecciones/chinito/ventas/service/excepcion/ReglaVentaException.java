package pe.confecciones.chinito.ventas.service.excepcion;

// Se lanza cuando una operación incumple una regla del módulo; el mensaje se muestra al usuario.
public class ReglaVentaException extends RuntimeException {
    public ReglaVentaException(String mensaje) { super(mensaje); }
}
