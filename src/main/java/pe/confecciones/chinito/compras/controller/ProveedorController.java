package pe.confecciones.chinito.compras.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.confecciones.chinito.compras.dto.ProveedorRequest;
import pe.confecciones.chinito.compras.modelo.Proveedor;
import pe.confecciones.chinito.compras.service.ProveedorService;
import pe.confecciones.chinito.compras.service.excepcion.ReglaCompraException;

@Controller
@RequestMapping("/compras/proveedores")
public class ProveedorController {
    private final ProveedorService proveedores;

    public ProveedorController(ProveedorService proveedores) { this.proveedores = proveedores; }

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("proveedores", proveedores.listar(buscar));
        model.addAttribute("buscar", buscar);
        return "compras/proveedores";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        formulario(model, "Nuevo proveedor", "/compras/proveedores", new ProveedorRequest(null, null, null, null, null));
        return "compras/proveedor-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Proveedor p = proveedores.buscar(id);
        formulario(model, "Editar proveedor", "/compras/proveedores/" + id,
                new ProveedorRequest(p.getNombre(), p.getRuc(), p.getTelefono(), p.getContacto(), p.getObservaciones()));
        return "compras/proveedor-form";
    }

    @PostMapping
    public String crear(@ModelAttribute ProveedorRequest datos, Model model) {
        try {
            proveedores.guardar(null, datos);
            return "redirect:/compras/proveedores";
        } catch (ReglaCompraException e) {
            formulario(model, "Nuevo proveedor", "/compras/proveedores", datos);
            model.addAttribute("error", e.getMessage());
            return "compras/proveedor-form";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute ProveedorRequest datos, Model model) {
        try {
            proveedores.guardar(id, datos);
            return "redirect:/compras/proveedores";
        } catch (ReglaCompraException e) {
            formulario(model, "Editar proveedor", "/compras/proveedores/" + id, datos);
            model.addAttribute("error", e.getMessage());
            return "compras/proveedor-form";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            proveedores.eliminar(id);
        } catch (ReglaCompraException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/compras/proveedores";
    }

    private void formulario(Model model, String titulo, String accion, ProveedorRequest form) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        model.addAttribute("form", form);
    }
}
