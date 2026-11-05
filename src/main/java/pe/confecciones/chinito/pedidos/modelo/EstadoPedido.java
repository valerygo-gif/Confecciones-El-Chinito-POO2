package pe.confecciones.chinito.pedidos.modelo;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

// Valores idénticos al ENUM pedido.estado.
public enum EstadoPedido {
    REGISTRADO, CONFIRMADO, EN_PRODUCCION, LISTO, ENTREGA_PARCIAL, ENTREGADO, CANCELADO;

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

    public boolean puedePasarA(EstadoPedido nuevo) {
        return nuevo != null && PERMITIDAS.get(this).contains(nuevo);
    }
}
