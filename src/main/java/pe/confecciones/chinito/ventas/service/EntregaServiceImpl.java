package pe.confecciones.chinito.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.confecciones.chinito.ventas.dto.EntregaRequest;
import pe.confecciones.chinito.ventas.modelo.Entrega;
import pe.confecciones.chinito.ventas.repository.EntregaRepository;
import pe.confecciones.chinito.ventas.service.validador.VentaActivaValidator;
import java.util.List;

@Service
public class EntregaServiceImpl implements EntregaService {
    private final EntregaRepository entregas;
    private final VentaActivaValidator ventaActiva;

    public EntregaServiceImpl(EntregaRepository entregas, VentaActivaValidator ventaActiva) {
        this.entregas = entregas;
        this.ventaActiva = ventaActiva;
    }

    @Override
    public List<Entrega> listar(Long idPedido) { return entregas.findByIdPedidoOrderByFechaEntregaDesc(idPedido); }

    @Override
    @Transactional
    public Entrega registrar(Long idPedido, EntregaRequest datos) {
        ventaActiva.validar(idPedido);
        Entrega e = new Entrega();
        e.setIdPedido(idPedido);
        e.setObservaciones(datos.observaciones());
        return entregas.save(e);
    }
}
