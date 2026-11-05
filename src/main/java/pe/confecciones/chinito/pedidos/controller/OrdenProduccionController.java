package pe.confecciones.chinito.pedidos.controller;

import pe.confecciones.chinito.pedidos.dto.*;
import pe.confecciones.chinito.pedidos.modelo.*;
import pe.confecciones.chinito.pedidos.service.OrdenProduccionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ordenes-produccion")
public class OrdenProduccionController {
    private final OrdenProduccionService servicio;

    public OrdenProduccionController(OrdenProduccionService servicio) { this.servicio = servicio; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenProduccionResponse crear(@RequestBody OrdenProduccionRequest request) {
        return servicio.crear(request);
    }

    @GetMapping("/{id}")
    public OrdenProduccionResponse obtener(@PathVariable Long id) { return servicio.obtener(id); }

    @GetMapping
    public List<OrdenProduccionResponse> listar(@RequestParam(required = false) EstadoOrden estado) {
        return servicio.listar(estado);
    }

    @PatchMapping("/{id}/estado")
    public OrdenProduccionResponse cambiarEstado(@PathVariable Long id,
                                                     @RequestParam EstadoOrden nuevo) {
        return servicio.cambiarEstado(id, nuevo);
    }
}
