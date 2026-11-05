package pe.confecciones.chinito.pedidos.controller.vista;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.confecciones.chinito.pedidos.service.TransicionInvalidaException;
import java.util.Arrays;
import pe.confecciones.chinito.ventas.api.ClienteConsulta;
import pe.confecciones.chinito.ventas.api.VentaConsulta;
import pe.confecciones.chinito.pedidos.api.ProduccionConsulta;
import pe.confecciones.chinito.pedidos.dto.PedidoResponse;
import pe.confecciones.chinito.pedidos.modelo.EstadoPedido;
import pe.confecciones.chinito.pedidos.service.PedidoService;
import pe.confecciones.chinito.pedidos.service.RecursoNoEncontradoException;
import pe.confecciones.chinito.pedidos.dto.PedidoRequest;
import pe.confecciones.chinito.pedidos.modelo.TipoItemPedido;
import pe.confecciones.chinito.pedidos.modelo.TipoPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Pantallas de pedidos (listado, detalle y registro). Reutiliza PedidoService; el cliente y la venta
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

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return formulario(model, null);
    }

    // Los detalles llegan como listas paralelas (una posición por fila del formulario).
    @PostMapping
    public String crear(@RequestParam(required = false) Long idCliente,
                        @RequestParam(required = false) TipoPedido tipo,
                        @RequestParam(required = false) LocalDate fechaEntrega,
                        @RequestParam(required = false) String notas,
                        @RequestParam(required = false) List<TipoItemPedido> tipoItem,
                        @RequestParam(required = false) List<String> idConjunto,
                        @RequestParam(required = false) List<String> descripcion,
                        @RequestParam(required = false) List<Integer> cantidad,
                        @RequestParam(required = false) List<BigDecimal> precio,
                        Model model) {
        try {
            if (idCliente != null && !clientes.existeCliente(idCliente)) {
                throw new IllegalArgumentException("El cliente seleccionado no existe.");
            }
            List<PedidoRequest.Linea> lineas = new ArrayList<>();
            int n = descripcion == null ? 0 : descripcion.size();
            for (int i = 0; i < n; i++) {
                String conjunto = idConjunto != null && i < idConjunto.size() ? idConjunto.get(i) : null;
                lineas.add(new PedidoRequest.Linea(
                        tipoItem != null && i < tipoItem.size() ? tipoItem.get(i) : null,
                        conjunto == null || conjunto.isBlank() ? null : Long.valueOf(conjunto.trim()),
                        descripcion.get(i),
                        cantidad != null && i < cantidad.size() ? cantidad.get(i) : null,
                        precio != null && i < precio.size() ? precio.get(i) : null, null));
            }
            PedidoResponse creado = pedidos.registrar(
                    new PedidoRequest(idCliente, fechaEntrega, tipo, notas, lineas));
            return "redirect:/pedidos/" + creado.id();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, idCliente);
        }
    }

    private String formulario(Model model, Long idClienteElegido) {
        model.addAttribute("listaClientes", clientes.listarNombres());
        model.addAttribute("idClienteElegido", idClienteElegido);
        model.addAttribute("tiposPedido", TipoPedido.values());
        model.addAttribute("tiposItem", TipoItemPedido.values());
        return "pedidos/pedido-form";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        PedidoResponse pedido = pedidos.obtener(id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", nombreCliente(pedido.idCliente()));
        model.addAttribute("ordenes", produccion.listarOrdenesDePedido(id));
        model.addAttribute("tieneVenta", ventas.tieneVentaActiva(id));
        model.addAttribute("transiciones", Arrays.stream(EstadoPedido.values())
                .filter(pedido.estado()::puedePasarA).toList());
        return "pedidos/pedido-detalle";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido nuevo, RedirectAttributes flash) {
        try {
            pedidos.cambiarEstado(id, nuevo);
            flash.addFlashAttribute("mensaje", "El pedido " + id + " pasó a " + nuevo + ".");
        } catch (TransicionInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
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
