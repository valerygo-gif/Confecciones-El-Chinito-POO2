package pe.confecciones.chinito.compras.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.compras.api.CostoConsulta;
import pe.confecciones.chinito.compras.dto.CostoOrdenResponse;
import pe.confecciones.chinito.compras.dto.CostoVarianteResponse;
import pe.confecciones.chinito.compras.dto.ResumenProveedorResponse;
import pe.confecciones.chinito.compras.repository.CompraRepository;
import pe.confecciones.chinito.compras.repository.CostoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Reportes de compras y costos. Los costos salen de las vistas del esquema (solo lectura).
@Service
@Transactional(readOnly = true)
public class CostoServiceImpl implements CostoService, CostoConsulta {
    private final CompraRepository compras;
    private final CostoRepository costos;

    public CostoServiceImpl(CompraRepository compras, CostoRepository costos) {
        this.compras = compras;
        this.costos = costos;
    }

    @Override
    public List<ResumenProveedorResponse> comprasPorProveedor() {
        return compras.resumenPorProveedor().stream().map(f -> new ResumenProveedorResponse(
                ((Number) f[0]).longValue(), String.valueOf(f[1]), ((Number) f[2]).longValue(), dec(f[3]))).toList();
    }

    @Override
    public List<CostoVarianteResponse> costosEstimados() {
        return costos.costosEstimados().stream().map(f -> new CostoVarianteResponse(((Number) f[0]).longValue(),
                String.valueOf(f[1]), String.valueOf(f[2]), String.valueOf(f[3]), dec(f[4]), dec(f[5]), dec(f[6]))).toList();
    }

    @Override
    public List<CostoOrdenResponse> costosReales() {
        return costos.costosReales().stream().map(f -> new CostoOrdenResponse(((Number) f[0]).longValue(),
                ((Number) f[1]).longValue(), ((Number) f[2]).longValue(), ((Number) f[3]).intValue(),
                dec(f[4]), dec(f[5]), dec(f[6]))).toList();
    }

    @Override
    public Optional<BigDecimal> costoMaterialesEstimado(Long idVariante) {
        return costosEstimados().stream().filter(c -> c.idVariante().equals(idVariante))
                .map(CostoVarianteResponse::costoMateriales).findFirst();
    }

    @Override
    public BigDecimal totalComprado() { return compras.totalRecibido(); }

    private static BigDecimal dec(Object o) {
        return o == null ? BigDecimal.ZERO : o instanceof BigDecimal b ? b : new BigDecimal(o.toString());
    }
}
