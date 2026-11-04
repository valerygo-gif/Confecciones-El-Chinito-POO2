package pe.confecciones.chinito.pedidos.dto;

public record OrdenDetalleRequest(Long idVariante, Long idPedidoComponente, Integer cantidadAProducir) { }
