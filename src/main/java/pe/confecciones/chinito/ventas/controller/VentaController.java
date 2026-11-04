package pe.confecciones.chinito.ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.confecciones.chinito.ventas.modelo.Pago;
import pe.confecciones.chinito.ventas.service.VentaService;
import java.math.BigDecimal;

@Controller
@RequestMapping("/ventas")
public class VentaController {
    private final VentaService servicio;

    public VentaController(VentaService servicio) { this.servicio = servicio; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", servicio.listarVentas());
        return "ventas/ventas";
    }

    @PostMapping
    public String registrar(@RequestParam Long idPedido, RedirectAttributes ra) {
        try {
            servicio.registrarVenta(idPedido);
            return "redirect:/ventas/pedido/" + idPedido;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas";
        }
    }

    @PostMapping("/{idVenta}/anular")
    public String anular(@PathVariable Long idVenta) {
        servicio.anularVenta(idVenta);
        return "redirect:/ventas";
    }

    @GetMapping("/pedido/{idPedido}")
    public String cuenta(@PathVariable Long idPedido, Model model) {
        try {
            BigDecimal total = servicio.total(idPedido);
            BigDecimal pagado = servicio.pagado(idPedido);
            model.addAttribute("total", total);
            model.addAttribute("pagado", pagado);
            model.addAttribute("saldo", total.subtract(pagado));
            model.addAttribute("pagos", servicio.listarPagos(idPedido));
            model.addAttribute("entregas", servicio.listarEntregas(idPedido));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("form", new Pago());
        model.addAttribute("metodos", Pago.Metodo.values());
        model.addAttribute("idPedido", idPedido);
        return "ventas/cuenta-pedido";
    }

    @PostMapping("/pedido/{idPedido}/pagos")
    public String pagar(@PathVariable Long idPedido, @ModelAttribute("form") Pago form, RedirectAttributes ra) {
        try {
            servicio.registrarPago(idPedido, form);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/pedido/" + idPedido;
    }

    @PostMapping("/pagos/{idPago}/anular")
    public String anularPago(@PathVariable Long idPago, @RequestParam Long idPedido) {
        servicio.anularPago(idPago);
        return "redirect:/ventas/pedido/" + idPedido;
    }

    @PostMapping("/pedido/{idPedido}/entregas")
    public String entregar(@PathVariable Long idPedido, @RequestParam(required = false) String observaciones,
                           RedirectAttributes ra) {
        try {
            servicio.registrarEntrega(idPedido, observaciones);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/pedido/" + idPedido;
    }
}
