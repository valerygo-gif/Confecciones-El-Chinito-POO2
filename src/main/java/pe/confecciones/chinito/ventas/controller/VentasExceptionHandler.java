package pe.confecciones.chinito.ventas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pe.confecciones.chinito.ventas.service.excepcion.*;

// Red de seguridad: muestra una página de error con el mensaje en vez de un 500.
// Solo aplica a los controladores de este módulo.
@ControllerAdvice(basePackages = "pe.confecciones.chinito.ventas.controller")
public class VentasExceptionHandler {

    @ExceptionHandler({ClienteNoEncontradoException.class, VentaNoEncontradaException.class,
            PagoNoEncontradoException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RuntimeException e, Model model) {
        model.addAttribute("mensaje", e.getMessage());
        return "ventas/error";
    }

    @ExceptionHandler(ReglaVentaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String reglaIncumplida(ReglaVentaException e, Model model) {
        model.addAttribute("mensaje", e.getMessage());
        return "ventas/error";
    }
}
