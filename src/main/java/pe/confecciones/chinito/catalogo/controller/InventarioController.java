package pe.confecciones.chinito.catalogo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import pe.confecciones.chinito.catalogo.modelo.MovimientoInventario;
import pe.confecciones.chinito.catalogo.service.InventarioService;

@Controller
@RequestMapping("/catalogo")
public class InventarioController {
    private final InventarioService inventario;
    public InventarioController(InventarioService inventario) { this.inventario = inventario; }

    @GetMapping("/inventario")
    public String stock(Model model) {
        model.addAttribute("filas", inventario.listarStock());
        model.addAttribute("variantes", inventario.todasVariantes());
        model.addAttribute("ubicaciones", inventario.listarUbicaciones());
        model.addAttribute("movimientos", inventario.ultimosMovimientos());
        return "catalogo/inventario";
    }

    @PostMapping("/inventario/movimientos")
    public String moverProducto(@RequestParam Long varianteId, @RequestParam Long ubicacionId,
                                @RequestParam MovimientoInventario.Tipo tipo,
                                @RequestParam Integer cantidad,
                                @RequestParam(required = false) String motivo, RedirectAttributes flash) {
        try {
            inventario.moverProducto(varianteId, ubicacionId, tipo, cantidad, motivo);
            flash.addFlashAttribute("mensaje", "Movimiento de producto registrado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/inventario";
    }

    @PostMapping("/inventario/traslados")
    public String trasladarProducto(@RequestParam Long varianteId, @RequestParam Long origenId,
                                    @RequestParam Long destinoId, @RequestParam Integer cantidad,
                                    @RequestParam(required = false) String motivo, RedirectAttributes flash) {
        try {
            inventario.trasladarProducto(varianteId, origenId, destinoId, cantidad, motivo);
            flash.addFlashAttribute("mensaje", "Traslado de producto registrado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/inventario";
    }

    @GetMapping("/materiales")
    public String materiales(Model model) {
        model.addAttribute("materiales", inventario.listarMateriales());
        model.addAttribute("resumen", inventario.resumenMateriales());
        model.addAttribute("ubicaciones", inventario.listarUbicaciones());
        model.addAttribute("movimientos", inventario.ultimosMovimientosMaterial());
        return "catalogo/materiales";
    }

    @PostMapping("/materiales")
    public String crearMaterial(@RequestParam String nombre, @RequestParam String unidadMedida,
                                @RequestParam BigDecimal costo, @RequestParam BigDecimal stockMinimo,
                                RedirectAttributes flash) {
        try {
            inventario.crearMaterial(nombre, unidadMedida, costo, stockMinimo);
            flash.addFlashAttribute("mensaje", "Material registrado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/materiales";
    }

    @PostMapping("/materiales/{id}/editar")
    public String editarMaterial(@PathVariable Long id, @RequestParam String nombre,
                                 @RequestParam String unidadMedida, @RequestParam BigDecimal costo,
                                 @RequestParam BigDecimal stockMinimo, RedirectAttributes flash) {
        try {
            inventario.editarMaterial(id, nombre, unidadMedida, costo, stockMinimo);
            flash.addFlashAttribute("mensaje", "Material actualizado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/materiales";
    }

    @PostMapping("/materiales/movimientos")
    public String moverMaterial(@RequestParam Long materialId, @RequestParam Long ubicacionId,
                                @RequestParam MovimientoInventario.Tipo tipo,
                                @RequestParam BigDecimal cantidad,
                                @RequestParam(required = false) String motivo, RedirectAttributes flash) {
        try {
            inventario.moverMaterial(materialId, ubicacionId, tipo, cantidad, motivo);
            flash.addFlashAttribute("mensaje", "Movimiento de material registrado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/materiales";
    }

    @PostMapping("/materiales/traslados")
    public String trasladarMaterial(@RequestParam Long materialId, @RequestParam Long origenId,
                                    @RequestParam Long destinoId, @RequestParam BigDecimal cantidad,
                                    @RequestParam(required = false) String motivo, RedirectAttributes flash) {
        try {
            inventario.trasladarMaterial(materialId, origenId, destinoId, cantidad, motivo);
            flash.addFlashAttribute("mensaje", "Traslado de material registrado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/materiales";
    }

    @GetMapping("/ubicaciones")
    public String ubicaciones(Model model) {
        model.addAttribute("ubicaciones", inventario.listarUbicaciones());
        return "catalogo/ubicaciones";
    }

    @PostMapping("/ubicaciones")
    public String crearUbicacion(@RequestParam String nombre,
                                 @RequestParam(required = false) String descripcion,
                                 RedirectAttributes flash) {
        try {
            inventario.crearUbicacion(nombre, descripcion);
            flash.addFlashAttribute("mensaje", "Ubicación registrada.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/ubicaciones";
    }

    @PostMapping("/ubicaciones/{id}/estado")
    public String cambiarUbicacion(@PathVariable Long id, RedirectAttributes flash) {
        try {
            inventario.cambiarEstadoUbicacion(id);
            flash.addFlashAttribute("mensaje", "Estado de la ubicación actualizado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catalogo/ubicaciones";
    }
}
