package pe.confecciones.chinito.ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.confecciones.chinito.ventas.dto.ClienteRequest;
import pe.confecciones.chinito.ventas.modelo.TipoDocumento;
import pe.confecciones.chinito.ventas.service.ClienteService;
import pe.confecciones.chinito.ventas.service.excepcion.ReglaVentaException;

@Controller
@RequestMapping("/ventas/clientes")
public class ClienteController {
    private final ClienteService servicio;

    public ClienteController(ClienteService servicio) { this.servicio = servicio; }

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        model.addAttribute("clientes", servicio.listar(buscar));
        return "ventas/clientes";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return formulario(model, new ClienteRequest(null, null, null, null, null, null), "Nuevo cliente", "/ventas/clientes");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, ClienteRequest.desde(servicio.buscar(id)), "Editar cliente", "/ventas/clientes/" + id);
    }

    @PostMapping
    public String crear(@ModelAttribute("form") ClienteRequest form, Model model) { return guardar(null, form, model); }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute("form") ClienteRequest form, Model model) {
        return guardar(id, form, model);
    }

    private String guardar(Long id, ClienteRequest form, Model model) {
        try {
            servicio.guardar(id, form);
            return "redirect:/ventas/clientes";
        } catch (ReglaVentaException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, form, id == null ? "Nuevo cliente" : "Editar cliente",
                    "/ventas/clientes" + (id == null ? "" : "/" + id));
        }
    }

    private String formulario(Model model, ClienteRequest form, String titulo, String accion) {
        model.addAttribute("form", form);
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        return "ventas/cliente-form";
    }
}
