package pe.confecciones.chinito.catalogo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.confecciones.chinito.catalogo.dto.*;
import pe.confecciones.chinito.catalogo.modelo.ProductoVariante;
import pe.confecciones.chinito.catalogo.service.*;

@Controller
@RequestMapping("/catalogo")
public class ProductoController {
    private final CatalogoService catalogo;
    private final MaestroService maestros;

    public ProductoController(CatalogoService catalogo, MaestroService maestros) {
        this.catalogo = catalogo;
        this.maestros = maestros;
    }

    @GetMapping("/productos")
    public String productos(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        model.addAttribute("productos", catalogo.listarProductos(buscar));
        return "catalogo/productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("form", new ProductoForm());
        model.addAttribute("titulo", "Nuevo producto");
        model.addAttribute("accion", "/catalogo/productos");
        cargarOpciones(model);
        return "catalogo/nuevo-producto";
    }

    @PostMapping("/productos")
    public String guardarProducto(@ModelAttribute("form") ProductoForm form, Model model) {
        try {
            var producto = catalogo.crearProducto(form);
            return "redirect:/catalogo/productos/" + producto.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("titulo", "Nuevo producto");
            model.addAttribute("accion", "/catalogo/productos");
            cargarOpciones(model);
            return "catalogo/nuevo-producto";
        }
    }

    @GetMapping("/productos/{id}/editar")
    public String editarProducto(@PathVariable Long id, Model model) {
        model.addAttribute("form", catalogo.datosProducto(id));
        model.addAttribute("titulo", "Editar producto");
        model.addAttribute("accion", "/catalogo/productos/" + id + "/editar");
        cargarOpciones(model);
        return "catalogo/nuevo-producto";
    }

    @PostMapping("/productos/{id}/editar")
    public String guardarEdicion(@PathVariable Long id,
                                 @ModelAttribute("form") ProductoForm form, Model model,
                                 RedirectAttributes flash) {
        try {
            catalogo.editarProducto(id, form);
            flash.addFlashAttribute("mensaje", "Producto actualizado.");
            return "redirect:/catalogo/productos/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("titulo", "Editar producto");
            model.addAttribute("accion", "/catalogo/productos/" + id + "/editar");
            cargarOpciones(model);
            return "catalogo/nuevo-producto";
        }
    }

    @PostMapping("/productos/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes flash) {
        try {
            catalogo.cambiarEstadoProducto(id);
            flash.addFlashAttribute("mensaje", "Estado del producto actualizado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/productos/" + id;
    }

    @GetMapping("/productos/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("producto", catalogo.obtenerProducto(id));
        model.addAttribute("variantes", catalogo.listarVariantes(id));
        model.addAttribute("tallas", maestros.tallas());
        model.addAttribute("varianteForm", new VarianteForm());
        return "catalogo/detalle-producto";
    }

    @PostMapping("/productos/{id}/variantes")
    public String guardarVariante(@PathVariable Long id,
                                  @ModelAttribute VarianteForm varianteForm, RedirectAttributes flash) {
        try {
            catalogo.crearVariante(id, varianteForm);
            flash.addFlashAttribute("mensaje", "Variante registrada.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/productos/" + id;
    }

    @GetMapping("/variantes/{id}/editar")
    public String editarVariante(@PathVariable Long id, Model model) {
        ProductoVariante variante = catalogo.obtenerVariante(id);
        model.addAttribute("variante", variante);
        model.addAttribute("form", catalogo.datosVariante(id));
        model.addAttribute("tallas", maestros.tallas());
        return "catalogo/editar-variante";
    }

    @PostMapping("/variantes/{id}/editar")
    public String guardarVarianteEditada(@PathVariable Long id,
                                         @ModelAttribute("form") VarianteForm form,
                                         Model model, RedirectAttributes flash) {
        ProductoVariante variante = catalogo.obtenerVariante(id);
        try {
            catalogo.editarVariante(id, form);
            flash.addFlashAttribute("mensaje", "Variante actualizada.");
            return "redirect:/catalogo/productos/" + variante.getProducto().getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("variante", variante);
            model.addAttribute("tallas", maestros.tallas());
            return "catalogo/editar-variante";
        }
    }

    @PostMapping("/variantes/{id}/estado")
    public String estadoVariante(@PathVariable Long id, RedirectAttributes flash) {
        ProductoVariante v = catalogo.obtenerVariante(id);
        catalogo.cambiarEstadoVariante(id);
        flash.addFlashAttribute("mensaje", "Estado de la variante actualizado.");
        return "redirect:/catalogo/productos/" + v.getProducto().getId();
    }

    @GetMapping("/maestros")
    public String maestros(Model model) {
        model.addAttribute("categorias", maestros.categorias());
        model.addAttribute("tallas", maestros.tallas());
        model.addAttribute("regiones", maestros.regiones());
        model.addAttribute("danzas", maestros.danzas());
        return "catalogo/maestros";
    }

    @PostMapping("/maestros/categorias")
    public String crearCategoria(@RequestParam String nombre, RedirectAttributes flash) {
        try {
            maestros.crearCategoria(nombre);
            flash.addFlashAttribute("mensaje", "Categoría registrada.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "Revisa la categoría; podría estar repetida.");
        }
        return "redirect:/catalogo/maestros";
    }

    @PostMapping("/maestros/tallas")
    public String crearTalla(@RequestParam String etiqueta,
                             @RequestParam(defaultValue = "0") int orden,
                             RedirectAttributes flash) {
        try {
            maestros.crearTalla(etiqueta, orden);
            flash.addFlashAttribute("mensaje", "Talla registrada.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "Revisa la talla; podría estar repetida.");
        }
        return "redirect:/catalogo/maestros";
    }

    @PostMapping("/maestros/regiones")
    public String crearRegion(@RequestParam String nombre, RedirectAttributes flash) {
        try {
            maestros.crearRegion(nombre);
            flash.addFlashAttribute("mensaje", "Región registrada.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/maestros";
    }

    @PostMapping("/maestros/danzas")
    public String crearDanza(@RequestParam String nombre,
                             @RequestParam(required = false) Long regionId,
                             RedirectAttributes flash) {
        try {
            maestros.crearDanza(nombre, regionId);
            flash.addFlashAttribute("mensaje", "Danza registrada.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/maestros";
    }

    private void cargarOpciones(Model model) {
        model.addAttribute("categorias", maestros.categorias());
        model.addAttribute("danzas", maestros.danzas());
    }

    // Un id inexistente en una pantalla de consulta/edición responde 404 con una página clara, no un 500.
    @ExceptionHandler(IllegalArgumentException.class)
    public String noEncontrado(IllegalArgumentException e, Model model, jakarta.servlet.http.HttpServletResponse respuesta) {
        boolean noExiste = e.getMessage() != null && e.getMessage().contains("no encontrad");
        respuesta.setStatus(noExiste ? 404 : 400);
        model.addAttribute("mensaje", e.getMessage());
        return "catalogo/error";
    }
}
