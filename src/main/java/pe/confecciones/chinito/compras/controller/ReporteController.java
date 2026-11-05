package pe.confecciones.chinito.compras.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.confecciones.chinito.compras.service.CostoService;
import java.math.BigDecimal;

@Controller
@RequestMapping("/compras/reportes")
public class ReporteController {
    private final CostoService costos;

    public ReporteController(CostoService costos) { this.costos = costos; }

    @GetMapping
    public String reportes(Model model) {
        var proveedores = costos.comprasPorProveedor();
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("totalComprado",
                proveedores.stream().map(p -> p.monto()).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("estimados", costos.costosEstimados());
        model.addAttribute("reales", costos.costosReales());
        return "compras/reportes";
    }
}
