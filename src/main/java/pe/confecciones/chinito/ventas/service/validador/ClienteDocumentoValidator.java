package pe.confecciones.chinito.ventas.service.validador;

import org.springframework.stereotype.Component;
import pe.confecciones.chinito.ventas.dto.ClienteRequest;
import pe.confecciones.chinito.ventas.modelo.Cliente;
import pe.confecciones.chinito.ventas.modelo.TipoDocumento;
import pe.confecciones.chinito.ventas.repository.ClienteRepository;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;

// Valida nombre y documento del cliente. Recibe los datos ya normalizados; actual es null al crear.
@Component
public class ClienteDocumentoValidator {
    private final ClienteRepository clientes;

    public ClienteDocumentoValidator(ClienteRepository clientes) { this.clientes = clientes; }

    public void validar(ClienteRequest datos, Cliente actual) {
        if (datos.nombre() == null) {
            throw new ReglaVentaException("El nombre del cliente es obligatorio.");
        }
        String numero = datos.numeroDocumento();
        if ((numero == null) != (datos.tipoDocumento() == null)) {
            throw new ReglaVentaException("Indique tipo y número de documento, o deje ambos vacíos.");
        }
        if (numero == null) return;
        if (datos.tipoDocumento() == TipoDocumento.DNI && !numero.matches("\\d{8}")) {
            throw new ReglaVentaException("El DNI debe tener 8 dígitos.");
        }
        if (datos.tipoDocumento() == TipoDocumento.RUC && !numero.matches("\\d{11}")) {
            throw new ReglaVentaException("El RUC debe tener 11 dígitos.");
        }
        boolean cambio = actual == null || actual.getTipoDocumento() != datos.tipoDocumento()
                || !numero.equals(actual.getNumeroDocumento());
        if (cambio && clientes.existsByTipoDocumentoAndNumeroDocumento(datos.tipoDocumento(), numero)) {
            throw new ReglaVentaException("Ya existe un cliente con ese documento.");
        }
    }
}
