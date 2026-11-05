package pe.confecciones.chinito.pedidos.modelo;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

// Valores idénticos al ENUM orden_produccion.estado.
public enum EstadoOrden {
    PENDIENTE, EN_PROCESO, TERMINADA, CANCELADA;

    private static final Map<EstadoOrden, Set<EstadoOrden>> PERMITIDAS = new EnumMap<>(EstadoOrden.class);
    static {
        PERMITIDAS.put(PENDIENTE, Set.of(EN_PROCESO, CANCELADA));
        PERMITIDAS.put(EN_PROCESO, Set.of(TERMINADA, CANCELADA));
        PERMITIDAS.put(TERMINADA, Set.of());
        PERMITIDAS.put(CANCELADA, Set.of());
    }

    public boolean puedePasarA(EstadoOrden nuevo) {
        return nuevo != null && PERMITIDAS.get(this).contains(nuevo);
    }
}
