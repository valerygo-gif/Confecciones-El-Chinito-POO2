package pe.confecciones.chinito.compras.service.integracion;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

// Lo que Compras necesita de Catálogo e Inventario. CatalogoConsulta solo permite consultar,
// por eso el ingreso de existencias se aísla aquí (ver InventarioCatalogoAdapter).
public interface InventarioPuerto {
    Optional<String> nombreMaterial(Long idMaterial);
    Optional<String> nombreVariante(Long idVariante);
    boolean ubicacionActiva(Long idUbicacion);
    void ingresarMaterial(Long idMaterial, Long idUbicacion, BigDecimal cantidad, String motivo);
    void ingresarProducto(Long idVariante, Long idUbicacion, int cantidad, String motivo);
    // id -> nombre, para llenar los selectores de los formularios.
    Map<Long, String> materiales();
    Map<Long, String> variantes();
    Map<Long, String> ubicacionesActivas();
}
