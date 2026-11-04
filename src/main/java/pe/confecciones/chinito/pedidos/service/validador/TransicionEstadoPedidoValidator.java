package pe.confecciones.chinito.pedidos.service.validador;

import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.pedidos.service.excepcion.TransicionEstadoInvalidaException;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import static pe.confecciones.chinito.pedidos.modelo.EstadoPedido.*;

@Component
public class TransicionEstadoPedidoValidator {
    private static final Map<EstadoPedido, Set<EstadoPedido>> PERMITIDAS = new EnumMap<>(EstadoPedido.class);
    static {
        PERMITIDAS.put(REGISTRADO, Set.of(CONFIRMADO, CANCELADO));
        PERMITIDAS.put(CONFIRMADO, Set.of(EN_PRODUCCION, CANCELADO));
        PERMITIDAS.put(EN_PRODUCCION, Set.of(LISTO, CANCELADO));
        PERMITIDAS.put(LISTO, Set.of(ENTREGA_PARCIAL, ENTREGADO));
        PERMITIDAS.put(ENTREGA_PARCIAL, Set.of(ENTREGADO));
        PERMITIDAS.put(ENTREGADO, Set.of());
        PERMITIDAS.put(CANCELADO, Set.of());
    }

    public void validar(EstadoPedido actual, EstadoPedido nuevo) {
        if (nuevo == null || !PERMITIDAS.get(actual).contains(nuevo)) {
            throw new TransicionEstadoInvalidaException("pedido", actual, nuevo);
        }
    }
}
