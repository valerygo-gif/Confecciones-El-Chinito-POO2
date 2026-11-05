package pe.confecciones.chinito.ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.confecciones.chinito.ventas.dto.EntregaRequest;
import pe.confecciones.chinito.ventas.dto.PagoRequest;
import pe.confecciones.chinito.ventas.modelo.MetodoPago;
import pe.confecciones.chinito.ventas.modelo.Pago;
import pe.confecciones.chinito.ventas.service.EntregaService;
import pe.confecciones.chinito.ventas.service.PagoService;
import pe.confecciones.chinito.ventas.service.VentaService;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;

@Controller
@RequestMapping("/ventas")
public class VentaController {
    private final VentaService ventas;
    private final PagoService pagos;
    private final EntregaService entregas;

    public VentaController(VentaService ventas, PagoService pagos, EntregaService entregas) {
        this.ventas = ventas;
        this.pagos = pagos;
        this.entregas = entregas;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", ventas.listar());
        return "ventas/ventas";
    }

    @PostMapping
    public String registrar(@RequestParam Long idPedido, RedirectAttributes ra) {
        try {
            ventas.registrar(idPedido);
            return "redirect:/ventas/pedido/" + idPedido;
        } catch (ReglaVentaException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas";
        }
    }

    @PostMapping("/{idVenta}/anular")
    public String anular(@PathVariable Long idVenta, RedirectAttributes ra) {
        try {
            ventas.anular(idVenta);
        } catch (ReglaVentaException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas";
    }

    @GetMapping("/pedido/{idPedido}")
    public String cuenta(@PathVariable Long idPedido, Model model) {
        try {
            model.addAttribute("cuenta", ventas.cuenta(idPedido));
        } catch (ReglaVentaException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("metodos", MetodoPago.values());
        model.addAttribute("idPedido", idPedido);
        return "ventas/cuenta-pedido";
    }

    @PostMapping("/pedido/{idPedido}/pagos")
    public String pagar(@PathVariable Long idPedido, @ModelAttribute PagoRequest pago, RedirectAttributes ra) {
        try {
            pagos.registrar(idPedido, pago);
        } catch (ReglaVentaException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/pedido/" + idPedido;
    }

    @PostMapping("/pagos/{idPago}/anular")
    public String anularPago(@PathVariable Long idPago) {
        Pago anulado = pagos.anular(idPago);
        return "redirect:/ventas/pedido/" + anulado.getIdPedido();
    }

    @PostMapping("/pedido/{idPedido}/entregas")
    public String entregar(@PathVariable Long idPedido, @ModelAttribute EntregaRequest entrega,
                           RedirectAttributes ra) {
        try {
            entregas.registrar(idPedido, entrega);
        } catch (ReglaVentaException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/pedido/" + idPedido;
    }
}
