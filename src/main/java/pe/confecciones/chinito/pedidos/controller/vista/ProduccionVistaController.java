package pe.confecciones.chinito.pedidos.controller.vista;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.confecciones.chinito.catalogo.api.CatalogoConsulta;
import pe.confecciones.chinito.pedidos.dto.*;
import pe.confecciones.chinito.pedidos.modelo.*;
import pe.confecciones.chinito.pedidos.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

// Pantallas de órdenes de producción: listado, detalle, creación, cambio de estado y trabajos de cada línea.
@Controller
@RequestMapping("/produccion")
public class ProduccionVistaController {
    private final OrdenProduccionService ordenes;
    private final TrabajoRealizadoService trabajos;
    private final CatalogoConsulta catalogo;
    private final PedidoService pedidos;

    public ProduccionVistaController(OrdenProduccionService ordenes, TrabajoRealizadoService trabajos,
                                     CatalogoConsulta catalogo, PedidoService pedidos) {
        this.ordenes = ordenes;
        this.trabajos = trabajos;
        this.catalogo = catalogo;
        this.pedidos = pedidos;
    }

    // Fila del listado: la orden con su avance total ya calculado.
    public record FilaOrden(OrdenProduccionResponse orden, int terminadas, int total) { }

    // Fila del formulario de orden (para no perder lo escrito si hay un error).
    public record FilaForm(Long idVariante, Integer cantidad) { }

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

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long idPedido, Model model) {
        return formulario(model, idPedido == null ? TipoOrden.REPOSICION : TipoOrden.PEDIDO, idPedido,
                null, null, null, List.of());
    }

    // Las líneas llegan como listas paralelas (una posición por fila del formulario).
    @PostMapping
    public String crear(@RequestParam TipoOrden tipo,
                        @RequestParam(required = false) Long idPedido,
                        @RequestParam(required = false) LocalDate fechaObjetivo,
                        @RequestParam(required = false) Integer prioridadManual,
                        @RequestParam(required = false) String indicaciones,
                        @RequestParam(required = false) List<Long> idVariante,
                        @RequestParam(required = false) List<Integer> cantidad,
                        Model model) {
        Long pedido = tipo == TipoOrden.REPOSICION ? null : idPedido;
        List<FilaForm> filas = new ArrayList<>();
        int n = idVariante == null ? 0 : idVariante.size();
        for (int i = 0; i < n; i++) {
            Integer c = cantidad != null && i < cantidad.size() ? cantidad.get(i) : null;
            if (idVariante.get(i) == null && c == null) continue;
            filas.add(new FilaForm(idVariante.get(i), c));
        }
        try {
            List<OrdenProduccionRequest.Linea> lineas = filas.stream()
                    .map(f -> new OrdenProduccionRequest.Linea(f.idVariante(), null, f.cantidad())).toList();
            OrdenProduccionResponse creada = ordenes.crear(new OrdenProduccionRequest(
                    tipo, pedido, fechaObjetivo, prioridadManual, indicaciones, lineas));
            return "redirect:/produccion/" + creada.id();
        } catch (IllegalArgumentException | RecursoNoEncontradoException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, tipo, pedido, fechaObjetivo, prioridadManual, indicaciones, filas);
        }
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
        model.addAttribute("transiciones", Arrays.stream(EstadoOrden.values())
                .filter(orden.estado()::puedePasarA).toList());
        model.addAttribute("trabajadores", trabajos.trabajadoresActivos());
        model.addAttribute("nombresTrabajadores", trabajos.nombresTrabajadores());
        model.addAttribute("etapas", EtapaProduccion.values());
        model.addAttribute("hoy", LocalDate.now());
        return "pedidos/orden-detalle";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam EstadoOrden nuevo, RedirectAttributes flash) {
        try {
            ordenes.cambiarEstado(id, nuevo);
            flash.addFlashAttribute("mensaje", "La orden " + id + " pasó a " + nuevo + ".");
        } catch (TransicionInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/produccion/" + id;
    }

    @PostMapping("/{id}/trabajos")
    public String reportarTrabajo(@PathVariable Long id, @RequestParam Long idOrdenDetalle,
                                  @RequestParam(required = false) Long idTrabajador,
                                  @RequestParam(required = false) EtapaProduccion etapa,
                                  @RequestParam(required = false) LocalDate fechaTrabajo,
                                  @RequestParam(required = false) Integer cantidad,
                                  @RequestParam(required = false) BigDecimal tarifaPorUnidad,
                                  @RequestParam(required = false) String observaciones,
                                  RedirectAttributes flash) {
        try {
            trabajos.reportar(new TrabajoRequest(idOrdenDetalle, idTrabajador, etapa, fechaTrabajo,
                    cantidad, tarifaPorUnidad, observaciones));
            flash.addFlashAttribute("mensaje", "Trabajo reportado. Queda pendiente de validar.");
        } catch (IllegalArgumentException | RecursoNoEncontradoException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/produccion/" + id;
    }

    @PostMapping("/{id}/trabajos/{idTrabajo}/validar")
    public String validarTrabajo(@PathVariable Long id, @PathVariable Long idTrabajo, RedirectAttributes flash) {
        try {
            trabajos.validar(idTrabajo);
            flash.addFlashAttribute("mensaje", "Trabajo " + idTrabajo + " validado.");
        } catch (IllegalArgumentException | TransicionInvalidaException | RecursoNoEncontradoException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/produccion/" + id;
    }

    @PostMapping("/{id}/trabajos/{idTrabajo}/rechazar")
    public String rechazarTrabajo(@PathVariable Long id, @PathVariable Long idTrabajo,
                                  @RequestParam(required = false) String observaciones, RedirectAttributes flash) {
        try {
            trabajos.rechazar(idTrabajo, observaciones);
            flash.addFlashAttribute("mensaje", "Trabajo " + idTrabajo + " rechazado.");
        } catch (TransicionInvalidaException | RecursoNoEncontradoException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/produccion/" + id;
    }

    private String formulario(Model model, TipoOrden tipo, Long idPedido, LocalDate fechaObjetivo,
                              Integer prioridad, String indicaciones, List<FilaForm> filas) {
        model.addAttribute("tipoSel", tipo);
        model.addAttribute("idPedidoSel", idPedido);
        model.addAttribute("fechaObjetivo", fechaObjetivo);
        model.addAttribute("prioridadSel", prioridad);
        model.addAttribute("indicaciones", indicaciones);
        model.addAttribute("filas", filas.isEmpty() ? List.of(new FilaForm(null, null)) : filas);
        model.addAttribute("tipos", TipoOrden.values());
        model.addAttribute("listaPedidos", pedidos.listar(null).stream()
                .filter(p -> p.estado() != EstadoPedido.CANCELADO && p.estado() != EstadoPedido.ENTREGADO).toList());
        model.addAttribute("listaVariantes", catalogo.listarVariantesActivas());
        return "pedidos/orden-form";
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException e, Model model) {
        model.addAttribute("mensaje", e.getMessage());
        return "pedidos/error";
    }
}
