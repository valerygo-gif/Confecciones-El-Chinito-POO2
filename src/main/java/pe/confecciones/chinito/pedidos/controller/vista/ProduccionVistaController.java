package pe.confecciones.chinito.pedidos.controller.vista;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.confecciones.chinito.catalogo.api.CatalogoConsulta;
import pe.confecciones.chinito.pedidos.dto.LineaOrdenResponse;
import pe.confecciones.chinito.pedidos.dto.OrdenProduccionResponse;
import pe.confecciones.chinito.pedidos.dto.TrabajoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoOrden;
import pe.confecciones.chinito.pedidos.service.OrdenProduccionService;
import pe.confecciones.chinito.pedidos.service.RecursoNoEncontradoException;
import pe.confecciones.chinito.pedidos.service.TrabajoRealizadoService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Pantallas de solo lectura de órdenes de producción y de los trabajos reportados en cada línea.
@Controller
@RequestMapping("/produccion")
public class ProduccionVistaController {
    private final OrdenProduccionService ordenes;
    private final TrabajoRealizadoService trabajos;
    private final CatalogoConsulta catalogo;

    public ProduccionVistaController(OrdenProduccionService ordenes, TrabajoRealizadoService trabajos,
                                     CatalogoConsulta catalogo) {
        this.ordenes = ordenes;
        this.trabajos = trabajos;
        this.catalogo = catalogo;
    }

    // Fila del listado: la orden con su avance total ya calculado.
    public record FilaOrden(OrdenProduccionResponse orden, int terminadas, int total) { }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoOrden estado, Model model) {
        List<FilaOrden> filas = ordenes.listar(estado).stream().map(o -> new FilaOrden(o,
                o.lineas().stream().mapToInt(LineaOrdenResponse::cantidadTerminada).sum(),
                o.lineas().stream().mapToInt(LineaOrdenResponse::cantidadAProducir).sum())).toList();
        model.addAttribute("filas", filas);
        model.addAttribute("estados", EstadoOrden.values());
        model.addAttribute("estadoActual", estado);
        return "pedidos/ordenes";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        OrdenProduccionResponse orden = ordenes.obtener(id);
        Map<Long, String> variantes = new HashMap<>();
        Map<Long, List<TrabajoResponse>> trabajosPorLinea = new HashMap<>();
        for (LineaOrdenResponse l : orden.lineas()) {
            variantes.put(l.idVariante(), catalogo.buscarVariante(l.idVariante())
                    .map(v -> v.producto() + " " + v.talla() + " (" + v.sku() + ")")
                    .orElse("Variante " + l.idVariante()));
            trabajosPorLinea.put(l.id(), trabajos.listarPorLineaOrden(l.id()));
        }
        model.addAttribute("orden", orden);
        model.addAttribute("variantes", variantes);
        model.addAttribute("trabajos", trabajosPorLinea);
        return "pedidos/orden-detalle";
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException e, Model model) {
        model.addAttribute("mensaje", e.getMessage());
        return "pedidos/error";
    }
}
