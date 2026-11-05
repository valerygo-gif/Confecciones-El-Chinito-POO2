package pe.confecciones.chinito.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.ventas.api.ClienteConsulta;
import pe.confecciones.chinito.ventas.dto.ClienteRequest;
import pe.confecciones.chinito.ventas.modelo.Cliente;
import pe.confecciones.chinito.ventas.repository.ClienteRepository;
import pe.confecciones.chinito.ventas.service.excepcion.ClienteNoEncontradoException;
import pe.confecciones.chinito.ventas.service.validador.ClienteDocumentoValidator;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService, ClienteConsulta {
    private final ClienteRepository clientes;
    private final ClienteDocumentoValidator documentoValidator;

    public ClienteServiceImpl(ClienteRepository clientes, ClienteDocumentoValidator documentoValidator) {
        this.clientes = clientes;
        this.documentoValidator = documentoValidator;
    }

    @Override
    public List<Cliente> listar(String buscar) {
        return buscar == null || buscar.isBlank() ? clientes.findAllByOrderByNombreAsc()
                : clientes.findByNombreContainingIgnoreCaseOrderByNombreAsc(buscar.trim());
    }

    @Override
    public Cliente buscar(Long id) {
        return clientes.findById(id).orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    @Override
    @Transactional
    public Cliente guardar(Long id, ClienteRequest datos) {
        ClienteRequest r = datos.normalizado();
        Cliente actual = id == null ? null : buscar(id);
        documentoValidator.validar(r, actual);
        Cliente c = actual == null ? new Cliente() : actual;
        c.setNombre(r.nombre());
        c.setTipoDocumento(r.tipoDocumento());
        c.setNumeroDocumento(r.numeroDocumento());
        c.setTelefono(r.telefono());
        c.setEmail(r.email());
        c.setDireccion(r.direccion());
        return clientes.save(c);
    }

    @Override
    public boolean existeCliente(Long idCliente) { return idCliente != null && clientes.existsById(idCliente); }

    @Override
    public Optional<String> nombreCliente(Long idCliente) {
        return clientes.findById(idCliente).map(Cliente::getNombre);
    }
}
