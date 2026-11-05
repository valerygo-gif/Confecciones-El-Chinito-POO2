package pe.confecciones.chinito.compras.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.compras.api.ProveedorConsulta;
import pe.confecciones.chinito.compras.dto.ProveedorRequest;
import pe.confecciones.chinito.compras.dto.ProveedorResponse;
import pe.confecciones.chinito.compras.modelo.Proveedor;
import pe.confecciones.chinito.compras.repository.CompraRepository;
import pe.confecciones.chinito.compras.repository.ProveedorRepository;
import pe.confecciones.chinito.compras.service.excepcion.ProveedorNoEncontradoException;
import pe.confecciones.chinito.compras.service.validador.ProveedorDatosValidator;
import pe.confecciones.chinito.compras.service.validador.ProveedorEliminableValidator;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorServiceImpl implements ProveedorService, ProveedorConsulta {
    private final ProveedorRepository proveedores;
    private final CompraRepository compras;
    private final ProveedorDatosValidator datosValidator;
    private final ProveedorEliminableValidator eliminableValidator;

    public ProveedorServiceImpl(ProveedorRepository proveedores, CompraRepository compras,
                                ProveedorDatosValidator datosValidator,
                                ProveedorEliminableValidator eliminableValidator) {
        this.proveedores = proveedores;
        this.compras = compras;
        this.datosValidator = datosValidator;
        this.eliminableValidator = eliminableValidator;
    }

    @Override
    public List<ProveedorResponse> listar(String buscar) {
        List<Proveedor> lista = buscar == null || buscar.isBlank() ? proveedores.findAllByOrderByNombreAsc()
                : proveedores.findByNombreContainingIgnoreCaseOrderByNombreAsc(buscar.trim());
        return lista.stream().map(p -> new ProveedorResponse(p.getId(), p.getNombre(), p.getRuc(), p.getTelefono(),
                p.getContacto(), p.getObservaciones(), !compras.existsByProveedor_Id(p.getId()))).toList();
    }

    @Override
    public Proveedor buscar(Long id) {
        return proveedores.findById(id).orElseThrow(() -> new ProveedorNoEncontradoException(id));
    }

    @Override
    @Transactional
    public Proveedor guardar(Long id, ProveedorRequest datos) {
        ProveedorRequest r = datos.normalizado();
        Proveedor actual = id == null ? null : buscar(id);
        datosValidator.validar(r, actual);
        Proveedor p = actual == null ? new Proveedor() : actual;
        p.setNombre(r.nombre());
        p.setRuc(r.ruc());
        p.setTelefono(r.telefono());
        p.setContacto(r.contacto());
        p.setObservaciones(r.observaciones());
        return proveedores.save(p);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Proveedor p = buscar(id);
        eliminableValidator.validar(id);
        proveedores.delete(p);
    }

    @Override
    public boolean existeProveedor(Long idProveedor) { return idProveedor != null && proveedores.existsById(idProveedor); }

    @Override
    public Optional<String> nombreProveedor(Long idProveedor) {
        return idProveedor == null ? Optional.empty() : proveedores.findById(idProveedor).map(Proveedor::getNombre);
    }
}
