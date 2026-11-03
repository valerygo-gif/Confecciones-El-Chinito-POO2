package pe.confecciones.chinito.catalogo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.catalogo.dto.*;
import pe.confecciones.chinito.catalogo.modelo.*;
import pe.confecciones.chinito.catalogo.repository.*;
import java.util.List;
import java.math.BigDecimal;

@Service
public class InventarioService {
    private final ExistenciaProductoRepository existencias;
    private final ExistenciaMaterialRepository existenciaMaterial;
    private final MovimientoInventarioRepository movimientos;
    private final MovimientoMaterialRepository movimientosMaterial;
    private final ProductoVarianteRepository variantes;
    private final MaterialRepository materiales;
    private final UbicacionRepository ubicaciones;

    public InventarioService(ExistenciaProductoRepository existencias,
                             ExistenciaMaterialRepository existenciaMaterial,
                             MovimientoInventarioRepository movimientos,
                             MovimientoMaterialRepository movimientosMaterial,
                             ProductoVarianteRepository variantes, MaterialRepository materiales,
                             UbicacionRepository ubicaciones) {
        this.existencias = existencias;
        this.existenciaMaterial = existenciaMaterial;
        this.movimientos = movimientos;
        this.movimientosMaterial = movimientosMaterial;
        this.variantes = variantes;
        this.materiales = materiales;
        this.ubicaciones = ubicaciones;
    }

    @Transactional(readOnly = true)
    public List<StockItem> listarStock() {
        return existencias.listarDisponibilidad().stream()
                .map(r -> new StockItem(((Number) r[0]).longValue(), String.valueOf(r[1]),
                        String.valueOf(r[2]), String.valueOf(r[3]), String.valueOf(r[4]),
                        ((Number) r[5]).intValue(), ((Number) r[6]).intValue(),
                        ((Number) r[7]).intValue())).toList();
    }

    @Transactional(readOnly = true)
    public int consultarDisponible(Long varianteId) {
        if (varianteId == null) return 0;
        Number total = existencias.totalDisponible(varianteId);
        return total == null ? 0 : total.intValue();
    }

    @Transactional(readOnly = true)
    public List<ProductoVariante> todasVariantes() { return variantes.findAll(); }

    @Transactional(readOnly = true)
    public List<MovimientoInventario> ultimosMovimientos() {
        return movimientos.findTop20ByOrderByFechaDescIdDesc();
    }

    @Transactional
    public void moverProducto(Long varianteId, Long ubicacionId, MovimientoInventario.Tipo tipo,
                             Integer cantidad, String motivo) {
        if (tipo != MovimientoInventario.Tipo.INGRESO && tipo != MovimientoInventario.Tipo.SALIDA &&
            tipo != MovimientoInventario.Tipo.AJUSTE_POSITIVO && tipo != MovimientoInventario.Tipo.AJUSTE_NEGATIVO)
            throw new IllegalArgumentException("Selecciona una entrada o salida válida.");
        validarEntero(cantidad);
        ProductoVariante variante = obtenerVarianteActiva(varianteId);
        Ubicacion ubicacion = obtenerUbicacionActiva(ubicacionId);
        cambiarExistencia(variante, ubicacion, tipo, cantidad, motivo);
    }

    @Transactional
    public void trasladarProducto(Long varianteId, Long origenId, Long destinoId,
                                 Integer cantidad, String motivo) {
        validarEntero(cantidad);
        if (origenId == null || origenId.equals(destinoId))
            throw new IllegalArgumentException("Selecciona dos ubicaciones diferentes.");
        ProductoVariante variante = obtenerVarianteActiva(varianteId);
        Ubicacion origen = obtenerUbicacionActiva(origenId);
        Ubicacion destino = obtenerUbicacionActiva(destinoId);
        // Ambas operaciones pertenecen a la misma transacción.
        cambiarExistencia(variante, origen, MovimientoInventario.Tipo.TRASLADO_SALIDA, cantidad, motivo);
        cambiarExistencia(variante, destino, MovimientoInventario.Tipo.TRASLADO_ENTRADA, cantidad, motivo);
    }

    private void cambiarExistencia(ProductoVariante variante, Ubicacion ubicacion,
                                  MovimientoInventario.Tipo tipo, int cantidad, String motivo) {
        ExistenciaProductoId id = new ExistenciaProductoId(ubicacion.getId(), variante.getId());
        // Creamos la fila si falta y la bloqueamos antes de modificar stock.
        existencias.asegurarFila(ubicacion.getId(), variante.getId());
        ExistenciaProducto fila = existencias.bloquear(id)
                .orElseThrow(() -> new IllegalStateException("No se pudo acceder al stock."));
        boolean suma = tipo == MovimientoInventario.Tipo.INGRESO ||
                tipo == MovimientoInventario.Tipo.AJUSTE_POSITIVO ||
                tipo == MovimientoInventario.Tipo.TRASLADO_ENTRADA;
        long resultado = (long) fila.getCantidadFisica() + (suma ? cantidad : -cantidad);
        Number reservadas = existencias.totalReservadoEnUbicacion(variante.getId(), ubicacion.getId());
        long reserva = reservadas == null ? 0 : reservadas.longValue();
        if (resultado < reserva)
            throw new IllegalArgumentException("No puedes retirar stock reservado. Disponible para retirar: "
                    + Math.max(0, fila.getCantidadFisica() - reserva));
        if (resultado > Integer.MAX_VALUE)
            throw new IllegalArgumentException("La cantidad supera el límite permitido.");
        fila.setCantidadFisica((int) resultado);
        existencias.saveAndFlush(fila);
        MovimientoInventario mov = new MovimientoInventario();
        mov.setVariante(variante);
        mov.setUbicacion(ubicacion);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setMotivo(motivoSeguro(motivo));
        movimientos.save(mov);
    }

    @Transactional(readOnly = true)
    public List<Material> listarMateriales() { return materiales.findAllByOrderByNombreAsc(); }

    @Transactional(readOnly = true)
    public List<MaterialStockItem> resumenMateriales() {
        return existenciaMaterial.resumenMateriales().stream()
                .map(r -> new MaterialStockItem(((Number) r[0]).longValue(), String.valueOf(r[1]),
                        String.valueOf(r[2]), (BigDecimal) r[3], (BigDecimal) r[4])).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoMaterial> ultimosMovimientosMaterial() {
        return movimientosMaterial.findTop20ByOrderByFechaDescIdDesc();
    }

    @Transactional
    public void crearMaterial(String nombre, String unidad, BigDecimal costo, BigDecimal stockMinimo) {
        Material material = new Material();
        asignarMaterial(material, nombre, unidad, costo, stockMinimo);
        materiales.save(material);
    }

    @Transactional
    public void editarMaterial(Long id, String nombre, String unidad, BigDecimal costo, BigDecimal stockMinimo) {
        Material material = materiales.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado."));
        asignarMaterial(material, nombre, unidad, costo, stockMinimo);
        materiales.save(material);
    }

    private void asignarMaterial(Material material, String nombre, String unidad, BigDecimal costo, BigDecimal minimo) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 140 ||
                unidad == null || unidad.isBlank() || unidad.trim().length() > 30)
            throw new IllegalArgumentException("Escribe un nombre y una unidad de medida válidos.");
        validarDecimal(costo, 2);
        validarDecimalNoNegativo(minimo, 3);
        material.setNombre(nombre.trim());
        material.setUnidadMedida(unidad.trim());
        material.setCostoReferencial(costo);
        material.setStockMinimo(minimo);
    }

    @Transactional
    public void moverMaterial(Long materialId, Long ubicacionId, MovimientoInventario.Tipo tipo,
                             BigDecimal cantidad, String motivo) {
        if (tipo != MovimientoInventario.Tipo.INGRESO && tipo != MovimientoInventario.Tipo.SALIDA &&
            tipo != MovimientoInventario.Tipo.AJUSTE_POSITIVO && tipo != MovimientoInventario.Tipo.AJUSTE_NEGATIVO)
            throw new IllegalArgumentException("Selecciona una entrada o salida válida.");
        validarDecimal(cantidad, 3);
        Material material = materiales.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado."));
        Ubicacion ubicacion = obtenerUbicacionActiva(ubicacionId);
        cambiarExistenciaMaterial(material, ubicacion, tipo, cantidad, motivo);
    }

    @Transactional
    public void trasladarMaterial(Long materialId, Long origenId, Long destinoId,
                                 BigDecimal cantidad, String motivo) {
        validarDecimal(cantidad, 3);
        if (origenId == null || origenId.equals(destinoId))
            throw new IllegalArgumentException("Selecciona dos ubicaciones diferentes.");
        Material material = materiales.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado."));
        cambiarExistenciaMaterial(material, obtenerUbicacionActiva(origenId),
                MovimientoInventario.Tipo.TRASLADO_SALIDA, cantidad, motivo);
        cambiarExistenciaMaterial(material, obtenerUbicacionActiva(destinoId),
                MovimientoInventario.Tipo.TRASLADO_ENTRADA, cantidad, motivo);
    }

    private void cambiarExistenciaMaterial(Material material, Ubicacion ubicacion,
                                           MovimientoInventario.Tipo tipo, BigDecimal cantidad, String motivo) {
        ExistenciaMaterialId id = new ExistenciaMaterialId(ubicacion.getId(), material.getId());
        existenciaMaterial.asegurarFila(ubicacion.getId(), material.getId());
        ExistenciaMaterial fila = existenciaMaterial.bloquear(id)
                .orElseThrow(() -> new IllegalStateException("No se pudo acceder al material."));
        boolean suma = tipo == MovimientoInventario.Tipo.INGRESO ||
                tipo == MovimientoInventario.Tipo.AJUSTE_POSITIVO ||
                tipo == MovimientoInventario.Tipo.TRASLADO_ENTRADA;
        BigDecimal resultado = suma ? fila.getCantidadFisica().add(cantidad)
                                    : fila.getCantidadFisica().subtract(cantidad);
        if (resultado.signum() < 0)
            throw new IllegalArgumentException("No hay suficiente material en esa ubicación.");
        validarDecimalNoNegativo(resultado, 3);
        fila.setCantidadFisica(resultado);
        existenciaMaterial.saveAndFlush(fila);
        MovimientoMaterial mov = new MovimientoMaterial();
        mov.setMaterial(material);
        mov.setUbicacion(ubicacion);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setMotivo(motivoSeguro(motivo));
        movimientosMaterial.save(mov);
    }

    @Transactional(readOnly = true)
    public List<Ubicacion> listarUbicaciones() { return ubicaciones.findAllByOrderByNombreAsc(); }

    @Transactional
    public void crearUbicacion(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 100)
            throw new IllegalArgumentException("Ingresa un nombre de ubicación válido.");
        if (descripcion != null && descripcion.length() > 255)
            throw new IllegalArgumentException("La descripción admite hasta 255 caracteres.");
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setNombre(nombre.trim());
        ubicacion.setDescripcion(descripcion);
        ubicaciones.save(ubicacion);
    }

    @Transactional
    public void cambiarEstadoUbicacion(Long id) {
        Ubicacion ubicacion = ubicaciones.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada."));
        ubicacion.setActiva(!ubicacion.isActiva());
        ubicaciones.save(ubicacion);
    }

    private ProductoVariante obtenerVarianteActiva(Long id) {
        ProductoVariante v = variantes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Selecciona una variante válida."));
        if (!v.isActivo() || !v.getProducto().isActivo())
            throw new IllegalArgumentException("La variante seleccionada está inactiva.");
        return v;
    }

    private Ubicacion obtenerUbicacionActiva(Long id) {
        Ubicacion u = ubicaciones.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Selecciona una ubicación válida."));
        if (!u.isActiva()) throw new IllegalArgumentException("La ubicación está inactiva.");
        return u;
    }

    private void validarEntero(Integer cantidad) {
        if (cantidad == null || cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
    }

    private void validarDecimal(BigDecimal valor, int escala) {
        validarDecimalNoNegativo(valor, escala);
        if (valor.signum() == 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
    }

    private void validarDecimalNoNegativo(BigDecimal valor, int escala) {
        if (valor == null || valor.signum() < 0 || valor.scale() > escala ||
                valor.precision() - valor.scale() > 12 - escala)
            throw new IllegalArgumentException("Ingresa una cantidad válida.");
    }

    private String motivoSeguro(String motivo) {
        if (motivo != null && motivo.length() > 255)
            throw new IllegalArgumentException("El motivo admite hasta 255 caracteres.");
        return motivo == null || motivo.isBlank() ? null : motivo.trim();
    }
}
