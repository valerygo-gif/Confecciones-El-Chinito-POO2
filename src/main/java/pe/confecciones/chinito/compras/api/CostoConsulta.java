package pe.confecciones.chinito.compras.api;

import java.math.BigDecimal;
import java.util.Optional;

// Costos de materiales y compras, para quien necesite calcular márgenes.
public interface CostoConsulta {
    Optional<BigDecimal> costoMaterialesEstimado(Long idVariante);
    BigDecimal totalComprado();
}
