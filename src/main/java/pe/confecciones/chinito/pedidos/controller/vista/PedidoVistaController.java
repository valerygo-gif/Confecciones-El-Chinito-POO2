package pe.confecciones.chinito.pedidos.controller.vista;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.confecciones.chinito.ventas.api.ClienteConsulta;
import pe.confecciones.chinito.ventas.api.VentaConsulta;
import pe.confecciones.chinito.pedidos.api.ProduccionConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.pedidos.service.PedidoService;
import pe.confecciones.chinito.pedidos.service.RecursoNoEncontradoException;
import java.util.HashMap;
import java.util.Map;

// Pantallas de solo lectura de pedidos. Reutiliza PedidoService; el cliente y la venta
// se consultan por las interfaces públicas de Ventas.
@Controller
@RequestMapping("/pedidos")
public class PedidoVistaController {
    private final PedidoService pedidos;
    private final ProduccionConsulta produccion;
    private final ClienteConsulta clientes;
    private final VentaConsulta ventas;

    public PedidoVistaController(PedidoService pedidos, ProduccionConsulta produccion,
                                 ClienteConsulta clientes, VentaConsulta ventas) {
        this.pedidos = pedidos;
        this.produccion = produccion;
        this.clientes = clientes;
        this.ventas = ventas;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoPedido estado, Model model) {
        var lista = pedidos.listar(estado);
        Map<Long, String> nombres = new HashMap<>();
        for (PedidoResponse p : lista) nombres.computeIfAbsent(p.idCliente(), this::nombreCliente);
        model.addAttribute("pedidos", lista);
        model.addAttribute("clientes", nombres);
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("estadoActual", estado);
        return "pedidos/pedidos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        PedidoResponse pedido = pedidos.obtener(id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", nombreCliente(pedido.idCliente()));
        model.addAttribute("ordenes", produccion.listarOrdenesDePedido(id));
        model.addAttribute("tieneVenta", ventas.tieneVentaActiva(id));
        return "pedidos/pedido-detalle";
    }

    private String nombreCliente(Long idCliente) {
        return idCliente == null ? "-" : clientes.nombreCliente(idCliente).orElse("Cliente " + idCliente);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException e, Model model) {
        model.addAttribute("mensaje", e.getMessage());
        return "pedidos/error";
    }
}
