package pe.confecciones.chinito.compras.service;

import pe.confecciones.chinito.compras.dto.CompraRequest;
import pe.confecciones.chinito.compras.dto.CompraResponse;
import pe.confecciones.chinito.compras.dto.RecepcionRequest;
import java.util.List;

public interface CompraService {
    List<CompraResponse> listar();
    CompraResponse buscar(Long id);
    CompraResponse registrar(CompraRequest datos);
    // Ingresa al inventario lo comprado y marca la compra como RECIBIDA.
    CompraResponse recibir(Long id, RecepcionRequest recepcion);
    CompraResponse anular(Long id);
}
