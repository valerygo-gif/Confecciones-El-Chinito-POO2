package pe.confecciones.chinito.compras.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.compras.modelo.Compra;
import pe.confecciones.chinito.compras.modelo.EstadoCompra;
import pe.confecciones.chinito.compras.service.excepcion.ReglaCompraException;
import pe.confecciones.chinito.compras.service.integracion.InventarioPuerto;

// Solo se recibe una compra REGISTRADA y en una ubicación activa.
@Component
public class CompraRecibibleValidator {
    private final InventarioPuerto inventario;

    public CompraRecibibleValidator(InventarioPuerto inventario) { this.inventario = inventario; }

    public void validar(Compra compra, Long idUbicacion) {
        if (compra.getEstado() != EstadoCompra.REGISTRADA)
            throw new ReglaCompraException("La compra " + compra.getId() + " está " + compra.getEstado()
                    + " y no puede recibirse.");
        if (!inventario.ubicacionActiva(idUbicacion))
            throw new ReglaCompraException("Selecciona una ubicación activa para recibir la compra.");
    }
}
