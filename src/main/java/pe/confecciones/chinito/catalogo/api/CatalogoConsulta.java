package pe.confecciones.chinito.catalogo.api;

import pe.confecciones.chinito.catalogo.dto.VarianteInfo;
import java.util.Optional;
import java.util.List;

// Los otros módulos usan esta interfaz para consultar variantes y stock sin depender de nuestras clases internas.
public interface CatalogoConsulta {
    Optional<VarianteInfo> buscarVariante(Long varianteId);
    List<VarianteInfo> listarVariantesActivas();
    int consultarStockDisponible(Long varianteId);
}
