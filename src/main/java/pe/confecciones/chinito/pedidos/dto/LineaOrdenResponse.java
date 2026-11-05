package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.LineaOrden;

public record LineaOrdenResponse(Long id, Long idOrden, Long idVariante, Long idPedidoComponente,
                                 Integer cantidadAProducir, Integer cantidadTerminada) {
    public static LineaOrdenResponse desde(LineaOrden l) {
        return new LineaOrdenResponse(l.getId(), l.getIdOrden(), l.getIdVariante(),
                l.getIdPedidoComponente(), l.getCantidadAProducir(), l.getCantidadTerminada());
    }
}
