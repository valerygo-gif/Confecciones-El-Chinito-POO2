package pe.confecciones.chinito.ventas.service;

import pe.confecciones.chinito.ventas.dto.ClienteRequest;
import pe.confecciones.chinito.ventas.modelo.Cliente;
import java.util.List;

public interface ClienteService {
    List<Cliente> listar(String buscar);
    Cliente buscar(Long id);
    // Crea (id nulo) o edita (id existente).
    Cliente guardar(Long id, ClienteRequest datos);
}
