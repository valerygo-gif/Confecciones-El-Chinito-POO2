package pe.confecciones.chinito.pedidos.service.validador;

import pe.confecciones.chinito.pedidos.modelo.EstadoOrden;
import pe.confecciones.chinito.pedidos.service.excepcion.TransicionEstadoInvalidaException;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import static pe.confecciones.chinito.pedidos.modelo.EstadoOrden.*;

@Component
public class TransicionEstadoOrdenValidator {
    private static final Map<EstadoOrden, Set<EstadoOrden>> PERMITIDAS = new EnumMap<>(EstadoOrden.class);
    static {
        PERMITIDAS.put(PENDIENTE, Set.of(EN_PROCESO, CANCELADA));
        PERMITIDAS.put(EN_PROCESO, Set.of(TERMINADA, CANCELADA));
        PERMITIDAS.put(TERMINADA, Set.of());
        PERMITIDAS.put(CANCELADA, Set.of());
    }

    public void validar(EstadoOrden actual, EstadoOrden nuevo) {
        if (nuevo == null || !PERMITIDAS.get(actual).contains(nuevo)) {
            throw new TransicionEstadoInvalidaException("orden de producción", actual, nuevo);
        }
    }
}
