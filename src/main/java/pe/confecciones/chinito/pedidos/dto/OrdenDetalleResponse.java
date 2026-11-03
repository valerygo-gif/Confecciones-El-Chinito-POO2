package pe.confecciones.chinito.pedidos.dto;

import pe.confecciones.chinito.pedidos.modelo.OrdenProduccionDetalle;

public record OrdenDetalleResponse(Long id, Long idOrden, Long idVariante, Long idPedidoComponente,
                                   Integer cantidadAProducir, Integer cantidadTerminada) {
    public static OrdenDetalleResponse desde(OrdenProduccionDetalle d) {
        return new OrdenDetalleResponse(d.getId(), d.getIdOrden(), d.getIdVariante(),
                d.getIdPedidoComponente(), d.getCantidadAProducir(), d.getCantidadTerminada());
    }
}
