package pe.confecciones.chinito.catalogo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.catalogo.api.CatalogoConsulta;
import pe.confecciones.chinito.catalogo.dto.*;
import pe.confecciones.chinito.catalogo.modelo.*;
import pe.confecciones.chinito.catalogo.repository.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CatalogoService implements CatalogoConsulta {
    private final ProductoRepository productos;
    private final ProductoVarianteRepository variantes;
    private final CategoriaProductoRepository categorias;
    private final DanzaRepository danzas;
    private final TallaRepository tallas;
    private final InventarioService inventario;

    public CatalogoService(ProductoRepository productos, ProductoVarianteRepository variantes,
                           CategoriaProductoRepository categorias, DanzaRepository danzas,
                           TallaRepository tallas, InventarioService inventario) {
        this.productos = productos;
        this.variantes = variantes;
        this.categorias = categorias;
        this.danzas = danzas;
        this.tallas = tallas;
        this.inventario = inventario;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductos(String filtro) {
        return filtro == null || filtro.isBlank() ? productos.findAllByOrderByNombreAsc()
                : productos.findByNombreContainingIgnoreCaseOrderByNombreAsc(filtro.trim());
    }

    @Transactional(readOnly = true)
    public Producto obtenerProducto(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<ProductoVariante> listarVariantes(Long productoId) {
        return variantes.findByProducto_IdOrderByTalla_OrdenVisualAsc(productoId);
    }

    @Transactional(readOnly = true)
    public ProductoVariante obtenerVariante(Long id) {
        return variantes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada."));
    }

    @Transactional(readOnly = true)
    public ProductoForm datosProducto(Long id) {
        Producto p = obtenerProducto(id);
        ProductoForm f = new ProductoForm();
        f.setNombre(p.getNombre());
        f.setCategoriaId(p.getCategoria().getId());
        f.setDanzaId(p.getDanza() == null ? null : p.getDanza().getId());
        f.setTipo(p.getTipo().name());
        f.setDescripcion(p.getDescripcion());
        return f;
    }

    @Transactional
    public Producto crearProducto(ProductoForm form) {
        Producto p = new Producto();
        asignarDatos(p, form);
        return productos.save(p);
    }

    @Transactional
    public Producto editarProducto(Long id, ProductoForm form) {
        Producto p = obtenerProducto(id);
        asignarDatos(p, form);
        return productos.save(p);
    }

    private void asignarDatos(Producto p, ProductoForm form) {
        if (form.getNombre() == null || form.getNombre().isBlank())
            throw new IllegalArgumentException("Escribe el nombre del producto.");
        if (form.getNombre().trim().length() > 150)
            throw new IllegalArgumentException("El nombre admite hasta 150 caracteres.");
        if (form.getCategoriaId() == null)
            throw new IllegalArgumentException("Selecciona una categoría.");
        p.setNombre(form.getNombre().trim());
        p.setCategoria(categorias.findById(form.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada.")));
        p.setDanza(form.getDanzaId() == null ? null : danzas.findById(form.getDanzaId())
                .orElseThrow(() -> new IllegalArgumentException("Danza no encontrada.")));
        try { p.setTipo(Producto.TipoProducto.valueOf(form.getTipo())); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("Tipo de producto inválido."); }
        p.setDescripcion(form.getDescripcion());
    }

    @Transactional
    public void cambiarEstadoProducto(Long id) {
        Producto p = obtenerProducto(id);
        p.setActivo(!p.isActivo());
        productos.save(p);
    }

    @Transactional
    public ProductoVariante crearVariante(Long productoId, VarianteForm form) {
        Producto producto = obtenerProducto(productoId);
        if (!producto.isActivo()) throw new IllegalArgumentException("Activa el producto antes de agregar variantes.");
        Talla talla = resolverTalla(producto, form.getTallaId());
        validarVariante(form);
        if (variantes.existsByProducto_IdAndTalla_Id(productoId, talla.getId()))
            throw new IllegalArgumentException("Esta talla ya existe para este producto.");
        validarSku(form.getSku(), null);
        ProductoVariante v = new ProductoVariante();
        v.setProducto(producto);
        v.setTalla(talla);
        asignarDatosVariante(v, form);
        return variantes.save(v);
    }

    @Transactional(readOnly = true)
    public VarianteForm datosVariante(Long id) {
        ProductoVariante v = obtenerVariante(id);
        VarianteForm f = new VarianteForm();
        f.setTallaId(v.getTalla().getId());
        f.setSku(v.getSku());
        f.setPrecio(v.getPrecioVentaReferencial());
        f.setStockMinimo(v.getStockMinimo());
        return f;
    }

    @Transactional
    public ProductoVariante editarVariante(Long id, VarianteForm form) {
        ProductoVariante v = obtenerVariante(id);
        validarVariante(form);
        Talla talla = resolverTalla(v.getProducto(), form.getTallaId());
        if (!talla.getId().equals(v.getTalla().getId()) &&
                variantes.existsByProducto_IdAndTalla_Id(v.getProducto().getId(), talla.getId()))
            throw new IllegalArgumentException("Esta talla ya existe para el producto.");
        validarSku(form.getSku(), id);
        v.setTalla(talla);
        asignarDatosVariante(v, form);
        return variantes.save(v);
    }

    @Transactional
    public void cambiarEstadoVariante(Long id) {
        ProductoVariante v = obtenerVariante(id);
        v.setActivo(!v.isActivo());
        variantes.save(v);
    }

    private Talla resolverTalla(Producto producto, Long tallaId) {
        if (tallaId != null) return tallas.findById(tallaId)
                .orElseThrow(() -> new IllegalArgumentException("Talla no encontrada."));
        if (producto.getTipo() != Producto.TipoProducto.ACCESORIO)
            throw new IllegalArgumentException("Selecciona una talla para la prenda.");
        // La base V3 exige una talla por variante. Para accesorios sin talla usamos 'Única'.
        return tallas.findByEtiquetaIgnoreCase("Única").orElseGet(() -> {
            Talla talla = new Talla();
            talla.setEtiqueta("Única");
            talla.setOrdenVisual(999);
            return tallas.saveAndFlush(talla);
        });
    }

    private void validarVariante(VarianteForm form) {
        if (form.getPrecio() == null || form.getPrecio().signum() < 0 ||
                form.getPrecio().scale() > 2 || form.getPrecio().precision() - form.getPrecio().scale() > 10)
            throw new IllegalArgumentException("Ingresa un precio válido con hasta dos decimales.");
        if (form.getStockMinimo() == null || form.getStockMinimo() < 0)
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        if (form.getSku() != null && form.getSku().trim().length() > 70)
            throw new IllegalArgumentException("El SKU admite hasta 70 caracteres.");
    }

    private void validarSku(String sku, Long varianteActual) {
        if (sku == null || sku.isBlank()) return;
        variantes.findBySkuIgnoreCase(sku.trim()).ifPresent(v -> {
            if (varianteActual == null || !v.getId().equals(varianteActual))
                throw new IllegalArgumentException("El SKU ya pertenece a otra variante.");
        });
    }

    private void asignarDatosVariante(ProductoVariante v, VarianteForm f) {
        v.setSku(f.getSku() == null || f.getSku().isBlank() ? null : f.getSku().trim());
        v.setPrecioVentaReferencial(f.getPrecio());
        v.setStockMinimo(f.getStockMinimo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VarianteInfo> listarVariantesActivas() {
        return variantes.findAll().stream()
                .filter(v -> v.isActivo() && v.getProducto().isActivo())
                .map(v -> new VarianteInfo(v.getId(), v.getProducto().getNombre(),
                        v.getTalla().getEtiqueta(), v.getSku(), v.getPrecioVentaReferencial(),
                        inventario.consultarDisponible(v.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VarianteInfo> buscarVariante(Long varianteId) {
        return variantes.findById(varianteId)
                .filter(v -> v.isActivo() && v.getProducto().isActivo())
                .map(v -> new VarianteInfo(v.getId(), v.getProducto().getNombre(),
                        v.getTalla().getEtiqueta(), v.getSku(),
                        v.getPrecioVentaReferencial(), inventario.consultarDisponible(varianteId)));
    }

    @Override
    @Transactional(readOnly = true)
    public int consultarStockDisponible(Long varianteId) {
        if (varianteId == null) return 0;
        return variantes.findById(varianteId)
                .filter(v -> v.isActivo() && v.getProducto().isActivo())
                .map(v -> inventario.consultarDisponible(varianteId)).orElse(0);
    }
}
