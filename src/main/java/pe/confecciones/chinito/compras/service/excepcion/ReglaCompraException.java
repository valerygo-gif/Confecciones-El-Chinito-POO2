package pe.confecciones.chinito.compras.service.excepcion;

// Se lanza cuando una operación incumple una regla del módulo; el mensaje se muestra al usuario.
public class ReglaCompraException extends RuntimeException {
    public ReglaCompraException(String mensaje) { super(mensaje); }
}
