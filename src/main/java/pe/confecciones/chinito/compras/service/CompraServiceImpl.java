package pe.confecciones.chinito.compras.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.compras.dto.*;
import pe.confecciones.chinito.compras.modelo.*;
import pe.confecciones.chinito.compras.repository.CompraRepository;
import pe.confecciones.chinito.compras.repository.ProveedorRepository;
import pe.confecciones.chinito.compras.service.excepcion.CompraNoEncontradaException;
import pe.confecciones.chinito.compras.service.excepcion.ProveedorNoEncontradoException;
import pe.confecciones.chinito.compras.service.integracion.InventarioPuerto;
import pe.confecciones.chinito.compras.service.validador.CompraAnulableValidator;
import pe.confecciones.chinito.compras.service.validador.CompraRecibibleValidator;
import pe.confecciones.chinito.compras.service.validador.CompraRegistrableValidator;
import java.util.List;

@Service
public class CompraServiceImpl implements CompraService {
    private final CompraRepository compras;
    private final ProveedorRepository proveedores;
    private final InventarioPuerto inventario;
    private final CompraRegistrableValidator registrable;
    private final CompraRecibibleValidator recibible;
    private final CompraAnulableValidator anulable;

    public CompraServiceImpl(CompraRepository compras, ProveedorRepository proveedores, InventarioPuerto inventario,
                             CompraRegistrableValidator registrable, CompraRecibibleValidator recibible,
                             CompraAnulableValidator anulable) {
        this.compras = compras;
        this.proveedores = proveedores;
        this.inventario = inventario;
        this.registrable = registrable;
        this.recibible = recibible;
        this.anulable = anulable;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponse> listar() {
        return compras.findAllByOrderByFechaCompraDesc().stream().map(this::respuesta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompraResponse buscar(Long id) { return respuesta(obtener(id)); }

    @Override
    @Transactional
    public CompraResponse registrar(CompraRequest datos) {
        Proveedor proveedor = datos.idProveedor() == null ? null : proveedores.findById(datos.idProveedor()).orElse(null);
        if (proveedor == null) {
            throw new ProveedorNoEncontradoException(datos.idProveedor());
        }
        registrable.validar(datos);
        Compra c = new Compra();
        c.setProveedor(proveedor);
        String doc = datos.nroDocumento();
        c.setNroDocumento(doc == null || doc.isBlank() ? null : doc.trim());
        for (LineaCompraRequest l : datos.lineas()) {
            CompraDetalle d = new CompraDetalle();
            if (l.tipo() == TipoItemCompra.MATERIAL) d.setIdMaterial(l.idItem()); else d.setIdVariante(l.idItem());
            d.setCantidad(l.cantidad());
            d.setPrecioUnitario(l.precioUnitario());
            c.agregar(d);
        }
        return respuesta(compras.save(c));
    }

    @Override
    @Transactional
    public CompraResponse recibir(Long id, RecepcionRequest recepcion) {
        Compra c = obtener(id);
        recibible.validar(c, recepcion == null ? null : recepcion.idUbicacion());
        String motivo = "Compra N.º " + c.getId();
        for (CompraDetalle d : c.getDetalles()) {
            if (d.getTipo() == TipoItemCompra.MATERIAL) {
                inventario.ingresarMaterial(d.getIdMaterial(), recepcion.idUbicacion(), d.getCantidad(), motivo);
            } else {
                inventario.ingresarProducto(d.getIdVariante(), recepcion.idUbicacion(), d.getCantidad().intValueExact(), motivo);
            }
        }
        c.setEstado(EstadoCompra.RECIBIDA);
        return respuesta(compras.save(c));
    }

    @Override
    @Transactional
    public CompraResponse anular(Long id) {
        Compra c = obtener(id);
        anulable.validar(c);
        c.setEstado(EstadoCompra.ANULADA);
        return respuesta(compras.save(c));
    }

    private Compra obtener(Long id) {
        return compras.findById(id).orElseThrow(() -> new CompraNoEncontradaException(id));
    }

    private CompraResponse respuesta(Compra c) {
        List<LineaCompraResponse> lineas = c.getDetalles().stream().map(d -> new LineaCompraResponse(
                d.getTipo(), d.getIdItem(), descripcion(d), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal())).toList();
        return new CompraResponse(c.getId(), c.getProveedor().getId(), c.getProveedor().getNombre(), c.getNroDocumento(),
                c.getFechaCompra(), c.getEstado(), c.getTotal(), lineas);
    }

    private String descripcion(CompraDetalle d) {
        return (d.getTipo() == TipoItemCompra.MATERIAL ? inventario.nombreMaterial(d.getIdMaterial())
                : inventario.nombreVariante(d.getIdVariante())).orElse("Ítem " + d.getIdItem());
    }
}
