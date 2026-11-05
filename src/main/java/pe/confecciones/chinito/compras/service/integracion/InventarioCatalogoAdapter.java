package pe.confecciones.chinito.compras.service.integracion;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.catalogo.api.CatalogoConsulta;
import pe.confecciones.chinito.catalogo.dto.VarianteInfo;
import pe.confecciones.chinito.catalogo.modelo.Material;
import pe.confecciones.chinito.catalogo.modelo.MovimientoInventario;
import pe.confecciones.chinito.catalogo.service.InventarioService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// Único punto de Compras que toca clases de Catálogo. Consulta por CatalogoConsulta
// y registra ingresos con InventarioService (no se modifica el paquete catalogo).
@Component
public class InventarioCatalogoAdapter implements InventarioPuerto {
    private final CatalogoConsulta catalogo;
    private final InventarioService inventario;

    public InventarioCatalogoAdapter(CatalogoConsulta catalogo, InventarioService inventario) {
        this.catalogo = catalogo;
        this.inventario = inventario;
    }

    @Override
    public Optional<String> nombreMaterial(Long idMaterial) {
        if (idMaterial == null) return Optional.empty();
        return inventario.listarMateriales().stream().filter(m -> m.getId().equals(idMaterial))
                .map(Material::getNombre).findFirst();
    }

    @Override
    public Optional<String> nombreVariante(Long idVariante) {
        if (idVariante == null) return Optional.empty();
        return catalogo.buscarVariante(idVariante).map(InventarioCatalogoAdapter::etiqueta);
    }

    @Override
    public boolean ubicacionActiva(Long idUbicacion) {
        return idUbicacion != null && inventario.listarUbicaciones().stream()
                .anyMatch(u -> u.getId().equals(idUbicacion) && u.isActiva());
    }

    @Override
    public void ingresarMaterial(Long idMaterial, Long idUbicacion, BigDecimal cantidad, String motivo) {
        inventario.moverMaterial(idMaterial, idUbicacion, MovimientoInventario.Tipo.INGRESO, cantidad, motivo);
    }

    @Override
    public void ingresarProducto(Long idVariante, Long idUbicacion, int cantidad, String motivo) {
        inventario.moverProducto(idVariante, idUbicacion, MovimientoInventario.Tipo.INGRESO, cantidad, motivo);
    }

    @Override
    public Map<Long, String> materiales() {
        Map<Long, String> r = new LinkedHashMap<>();
        inventario.listarMateriales().forEach(m -> r.put(m.getId(), m.getNombre() + " (" + m.getUnidadMedida() + ")"));
        return r;
    }

    @Override
    public Map<Long, String> variantes() {
        Map<Long, String> r = new LinkedHashMap<>();
        catalogo.listarVariantesActivas().forEach(v -> r.put(v.id(), etiqueta(v)));
        return r;
    }

    @Override
    public Map<Long, String> ubicacionesActivas() {
        Map<Long, String> r = new LinkedHashMap<>();
        inventario.listarUbicaciones().stream().filter(u -> u.isActiva()).forEach(u -> r.put(u.getId(), u.getNombre()));
        return r;
    }

    private static String etiqueta(VarianteInfo v) { return v.producto() + " " + v.talla() + " (" + v.sku() + ")"; }
}
