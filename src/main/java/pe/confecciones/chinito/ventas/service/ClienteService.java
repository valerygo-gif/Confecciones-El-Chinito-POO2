package pe.confecciones.chinito.ventas.service;

import pe.confecciones.chinito.ventas.modelo.Cliente;
import pe.confecciones.chinito.ventas.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) { this.clientes = clientes; }

    public List<Cliente> listar(String buscar) {
        return buscar == null || buscar.isBlank() ? clientes.findAllByOrderByNombreAsc()
                : clientes.findByNombreContainingIgnoreCaseOrderByNombreAsc(buscar.trim());
    }

    public Cliente buscar(Long id) {
        return clientes.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
    }

    // Sirve para crear (id nulo) y para editar (id existente).
    public Cliente guardar(Long id, Cliente c) {
        if (c.getNombre() == null || c.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        String numero = c.getNumeroDocumento() == null || c.getNumeroDocumento().isBlank() ? null : c.getNumeroDocumento().trim();
        if ((numero == null) != (c.getTipoDocumento() == null)) {
            throw new IllegalArgumentException("Indique tipo y número de documento, o deje ambos vacíos.");
        }
        Cliente actual = id == null ? new Cliente() : buscar(id);
        boolean cambioDoc = id == null || actual.getTipoDocumento() != c.getTipoDocumento()
                || (numero != null && !numero.equals(actual.getNumeroDocumento()));
        if (numero != null && cambioDoc && clientes.existsByTipoDocumentoAndNumeroDocumento(c.getTipoDocumento(), numero)) {
            throw new IllegalArgumentException("Ya existe un cliente con ese documento.");
        }
        actual.setTipoDocumento(c.getTipoDocumento());
        actual.setNumeroDocumento(numero);
        actual.setNombre(c.getNombre().trim());
        actual.setTelefono(c.getTelefono());
        actual.setEmail(c.getEmail());
        actual.setDireccion(c.getDireccion());
        return clientes.save(actual);
    }
}
