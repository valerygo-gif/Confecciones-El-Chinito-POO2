package pe.confecciones.chinito.pedidos.controller;

import pe.confecciones.chinito.pedidos.dto.*;
import pe.confecciones.chinito.pedidos.service.TrabajoRealizadoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/trabajos")
public class TrabajoRealizadoController {
    private final TrabajoRealizadoService servicio;

    public TrabajoRealizadoController(TrabajoRealizadoService servicio) { this.servicio = servicio; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrabajoResponse reportar(@RequestBody TrabajoRequest request) {
        return servicio.reportar(request);
    }

    @GetMapping("/{id}")
    public TrabajoResponse obtener(@PathVariable Long id) { return servicio.obtener(id); }

    @GetMapping(params = "idOrdenDetalle")
    public List<TrabajoResponse> porLineaOrden(@RequestParam Long idOrdenDetalle) {
        return servicio.listarPorLineaOrden(idOrdenDetalle);
    }

    @GetMapping(params = "idTrabajador")
    public List<TrabajoResponse> porTrabajador(@RequestParam Long idTrabajador) {
        return servicio.listarPorTrabajador(idTrabajador);
    }

    @PatchMapping("/{id}/validar")
    public TrabajoResponse validar(@PathVariable Long id) { return servicio.validar(id); }

    @PatchMapping("/{id}/rechazar")
    public TrabajoResponse rechazar(@PathVariable Long id,
                                        @RequestParam(required = false) String observaciones) {
        return servicio.rechazar(id, observaciones);
    }
}
