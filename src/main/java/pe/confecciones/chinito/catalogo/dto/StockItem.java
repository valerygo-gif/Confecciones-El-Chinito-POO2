package pe.confecciones.chinito.catalogo.dto;

public record StockItem(
        Long varianteId, String producto, String talla, String sku, String ubicacion,
        int cantidadFisica, int cantidadReservada, int cantidadDisponible) {
}
