package pe.confecciones.chinito.pedidos.controller;

import pe.confecciones.chinito.pedidos.dto.PedidoRequest;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.pedidos.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) { this.servicio = servicio; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse registrar(@RequestBody PedidoRequest request) { return servicio.registrar(request); }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable Long id) { return servicio.obtener(id); }

    @GetMapping
    public List<PedidoResponse> listar(@RequestParam(required = false) EstadoPedido estado) {
        return servicio.listar(estado);
    }

    @PatchMapping("/{id}/estado")
    public PedidoResponse cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido nuevo) {
        return servicio.cambiarEstado(id, nuevo);
    }
}
