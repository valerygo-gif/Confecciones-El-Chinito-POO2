package pe.confecciones.chinito.compras.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.confecciones.chinito.compras.dto.CompraRequest;
import pe.confecciones.chinito.compras.dto.LineaCompraRequest;
import pe.confecciones.chinito.compras.dto.RecepcionRequest;
import pe.confecciones.chinito.compras.modelo.TipoItemCompra;
import pe.confecciones.chinito.compras.service.CompraService;
import pe.confecciones.chinito.compras.service.ProveedorService;
import pe.confecciones.chinito.compras.service.excepcion.ReglaCompraException;
import pe.confecciones.chinito.compras.service.integracion.InventarioPuerto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/compras")
public class CompraController {
    private static final int FILAS = 5;
    private final CompraService compras;
    private final ProveedorService proveedores;
    private final InventarioPuerto inventario;

    public CompraController(CompraService compras, ProveedorService proveedores, InventarioPuerto inventario) {
        this.compras = compras;
        this.proveedores = proveedores;
        this.inventario = inventario;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("compras", compras.listar());
        return "compras/compras";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        cargarFormulario(model);
        return "compras/compra-form";
    }

    @PostMapping
    public String registrar(@RequestParam Long idProveedor, @RequestParam(required = false) String nroDocumento,
                            @RequestParam(required = false) List<String> item,
                            @RequestParam(required = false) List<String> cantidad,
                            @RequestParam(required = false) List<String> precio, Model model) {
        try {
            var creada = compras.registrar(new CompraRequest(idProveedor, nroDocumento,
                    lineas(item, cantidad, precio)));
            return "redirect:/compras/" + creada.id();
        } catch (ReglaCompraException e) {
            cargarFormulario(model);
            model.addAttribute("error", e.getMessage());
            return "compras/compra-form";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("compra", compras.buscar(id));
        model.addAttribute("ubicaciones", inventario.ubicacionesActivas());
        return "compras/compra-detalle";
    }

    @PostMapping("/{id}/recibir")
    public String recibir(@PathVariable Long id, @RequestParam(required = false) Long idUbicacion, RedirectAttributes ra) {
        try {
            compras.recibir(id, new RecepcionRequest(idUbicacion));
        } catch (ReglaCompraException | IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/compras/" + id;
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes ra) {
        try {
            compras.anular(id);
        } catch (ReglaCompraException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/compras/" + id;
    }

    private void cargarFormulario(Model model) {
        model.addAttribute("proveedores", proveedores.listar(null));
        model.addAttribute("materiales", inventario.materiales());
        model.addAttribute("variantes", inventario.variantes());
        model.addAttribute("filas", FILAS);
    }

    // Convierte las filas del formulario ("TIPO:id", cantidad, precio) en líneas; ignora las filas vacías.
    private List<LineaCompraRequest> lineas(List<String> item, List<String> cantidad, List<String> precio) {
        List<LineaCompraRequest> lineas = new ArrayList<>();
        if (item == null) return lineas;
        for (int i = 0; i < item.size(); i++) {
            if (item.get(i) == null || item.get(i).isBlank()) continue;
            try {
                String[] partes = item.get(i).split(":");
                lineas.add(new LineaCompraRequest(TipoItemCompra.valueOf(partes[0]), Long.valueOf(partes[1]),
                        decimal(cantidad, i), decimal(precio, i)));
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                throw new ReglaCompraException("Revisa los datos de la fila " + (i + 1) + ".");
            }
        }
        return lineas;
    }

    private String valor(List<String> lista, int i) { return lista == null || i >= lista.size() ? "" : lista.get(i).trim(); }

    private BigDecimal decimal(List<String> lista, int i) {
        String v = valor(lista, i);
        return v.isEmpty() ? null : new BigDecimal(v);
    }
}
